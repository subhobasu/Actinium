package ch.ethz.inf.vs.actinium.jscoap;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.mozilla.javascript.Function;

import ch.ethz.inf.vs.actinium.jscoap.jserror.NetworkErrorException;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.ResponseHandler;

/**
 * AsynchronousSender implements the process to send a request assynchronously.
 * Conforms to the CoAPRequest API
 * (http://lantersoft.ch/download/bachelorthesis/CoAPRequest_API.pdf)
 * <p>
 * Only one of the functions onload, ontimeout, onerror gets called (according
 * to the outcome of the request).
 * 
 * @author Martin Lanter
 */

public class AsynchronousSender extends AbstractSender {

	private Timer timer = new Timer();
	
	private CoAPRequest coapRequest;
	
	private Function onready; // onreadystatechange
	private Function ontimeout; // timeout error
	private Function onload;
	private Function onerror; // if a network error occurs
	
	private long timeout;
	
	private final Lock lock = new Lock();
	
	public AsynchronousSender(CoAPRequest coapRequest, Function onready, Function ontimeout, Function onload, Function onerror, long timeout) {
		this.coapRequest = coapRequest;
		this.onready = onready;
		this.ontimeout = ontimeout;
		this.onload = onload;
		this.onerror = onerror;
		this.timeout = timeout;
	}
	
	@Override
	public void send(Request request) {
		request.registerResponseHandler(new ResponseHandler() {
			public void handleResponse(Response response) {
				if (!isAcknowledgement(response)) {
					AsynchronousSender.this.handleAsyncResponse(response);
				}
			}
		});
		
		try {
			request.execute();
		} catch (IOException e) {
			handleError(onerror);
			throw new NetworkErrorException(e.toString());
		}
		
		// TODO use TokenLayer timeout
		if (timeout>0) {
			timer.schedule(new TimerTask() {
				public void run() { // this is always called in a new thread
					boolean istimeout;
					synchronized (lock) {
						if (!lock.receivedresponse)
							lock.timeouted = true;
						istimeout = !lock.receivedresponse && !lock.aborted;
					}
					if (istimeout) {
						handleError(ontimeout);
					}
				}
			}, timeout);
		}
	}
	
	// by ReceiverThread
	private void handleAsyncResponse(Response response) {
		boolean callonready;
		synchronized (lock) {
			callonready = !lock.aborted && !lock.timeouted;
			if (callonready)
				lock.receivedresponse = true;
		}
		if (callonready) {
			synchronized (coapRequest) {
				coapRequest.setResponse(response);
				coapRequest.setReadyState(CoAPRequest.DONE);
			}
			callJavaScriptFunction(onready, coapRequest, response);
			callJavaScriptFunction(onload, coapRequest, response);
		}
	}

	@Override
	public void abort() {
		boolean isabort;
		// Terminate send() algorithm
		synchronized (lock) {
			isabort = !lock.receivedresponse && !lock.timeouted;
			if (isabort)
				lock.aborted = true;
		}
		if (isabort) {
			synchronized (coapRequest) {
				coapRequest.setError(true);
				coapRequest.setReadyState(CoAPRequest.DONE);
				coapRequest.setSend(false);
			}
			callJavaScriptFunction(onready, coapRequest);
			coapRequest.setReadyState(CoAPRequest.UNSENT);
			// no onreadystatechange event is dispatched
		}
	}
	
	private void handleError(Function function) {
		synchronized (coapRequest) {
			coapRequest.setError(true);
			coapRequest.setReadyState(CoAPRequest.DONE);
		}
		callJavaScriptFunction(onready, coapRequest);
		callJavaScriptFunction(function, coapRequest);
	}
	
	private class Lock {
		private boolean aborted = false;
		private boolean receivedresponse = false;
		private boolean timeouted = false;
	}
}

package ch.ethz.inf.vs.actinium.jscoap;

import java.io.IOException;

import org.mozilla.javascript.Function;

import ch.ethz.inf.vs.actinium.jscoap.jserror.AbortErrorException;
import ch.ethz.inf.vs.actinium.jscoap.jserror.NetworkErrorException;
import ch.ethz.inf.vs.actinium.jscoap.jserror.RequestErrorException;
import ch.ethz.inf.vs.actinium.jscoap.jserror.TimeoutErrorException;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.ResponseHandler;

/**
 * SynchronousSender implements the process to send a request synchronously.
 * Conforms to the CoAPRequest API
 * (http://lantersoft.ch/download/bachelorthesis/CoAPRequest_API.pdf)
 * 
 * @author Martin Lanter
 */
public class SynchronousSender extends AbstractSender {

	private CoAPRequest coapRequest;
	
	private Function onready; // onreadystatechange
	private Function ontimeout;
	private Function onload;
	private Function onerror;
	
	private long timeout;
	private long timestamp;
	
	private final Lock lock = new Lock(); // never sync(coapreq) within sync(lock)
	
	public SynchronousSender(CoAPRequest coapRequest, Function onready, Function ontimeout, Function onload, Function onerror, long timeout) {
		this.coapRequest = coapRequest;
		this.onready = onready;
		this.ontimeout = ontimeout;
		this.onload = onload;
		this.onerror = onerror;
		this.timeout = timeout;
	}
	
	// by app's execution thread (must not be app's receiver thread)
	@Override
	public void send(Request request) {
		request.registerResponseHandler(new ResponseHandler() {
			public void handleResponse(Response response) {
				if (!isAcknowledgement(response)) {
					SynchronousSender.this.handleSyncResponse(response);
				}
			}
		});
		
		try {
			synchronized (lock) {
				if (!lock.aborted) { // if not already aborted
					timestamp = System.currentTimeMillis();
					
					request.execute();
					
					if (timeout<=0) {
						waitForResponse();
					} else {
						waitForResponse(timeout);
					}
				}
			}
		} catch (InterruptedException e) {
			throw new RequestErrorException(e);
		} catch (IOException e) {
			handleError(onerror);
			throw new NetworkErrorException(e.toString());
		}
	}
				
	private void waitForResponse() throws InterruptedException {
		while(!lock.receivedresponse && !lock.aborted) {
			lock.wait();
			checkAborted();
			checkTimeout();
		}
		checkAborted(); // check again for aborted in case the loop has not been entered
	}
	
	private void waitForResponse(long timeout) throws InterruptedException {
		while(!lock.receivedresponse && !lock.aborted) {
			long ttw = timestamp + timeout - System.currentTimeMillis(); // time to wait
			if (ttw<=0) checkTimeout();
			lock.wait(ttw); // ttw>0
			checkAborted();
			checkTimeout();
		}
		checkAborted(); // check again for aborted in case the loop has not been entered
	}
	
	// by app's ReceiverThread
	private void handleSyncResponse(Response response) {
		synchronized (lock) {
			lock.receivedresponse = true;
			
			if (!lock.aborted && !lock.timeouted) {
				coapRequest.setResponse(response);
				coapRequest.setReadyState(CoAPRequest.DONE);
				
				/*
				 * While the app's receiver thread executes this function, the
				 * caller of send() is still blocked!
				 */
				callJavaScriptFunction(onready, coapRequest, response);
				callJavaScriptFunction(onload, coapRequest, response);
			}

			lock.notifyAll();
		}
	}
	
	@Override
	public void abort() {
		synchronized (lock) {
			lock.aborted = true;
			lock.notifyAll();
		}
	}
	
	/**
	 * Checks whether a timeout in a synchronized request has occured. If it
	 * has, it calls the JavaScript function ontimeout if defined and then
	 * throws a CoAPTimeoutException. throws a CoAPTimeoutException.
	 * <p>
	 * Must have monitor on lock.
	 * 
	 * @param timestamp milliseconds since 1970.
	 * @param timeout
	 */
	private void checkTimeout() {
		if (isTimeout(timestamp, timeout)) {
			lock.timeouted = true;

			// by app's execution thread (who has called send())
			handleError(ontimeout);
			
			throw new TimeoutErrorException("Timout ("+timeout+") ms");
		}
	}
	
	/**
	 * Checks whether a timeout has occured. 
	 * @param timestamp milliseconds since 1970.
	 * @param timeout
	 */
	private boolean isTimeout(long timestamp, long timeout) {
		long now = System.currentTimeMillis();
		return now >= timestamp + timeout && !lock.aborted && !lock.receivedresponse;
	}
	
	/**
	 * Checks whether the connection has been aborted. If so, throws a
	 * RuntimeException.
	 */
	private void checkAborted() {
		if (lock.aborted && !lock.receivedresponse && !lock.timeouted) {
			synchronized (coapRequest) {
				coapRequest.setError(true);
				coapRequest.setReadyState(CoAPRequest.DONE);
				coapRequest.setSend(false);
			}
			callJavaScriptFunction(onready, coapRequest);
			coapRequest.setReadyState(CoAPRequest.UNSENT);
			throw new AbortErrorException("Connection has been aborted");
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
		private boolean receivedresponse = false;
		private boolean aborted = false;
		private boolean timeouted = false;
	}
}

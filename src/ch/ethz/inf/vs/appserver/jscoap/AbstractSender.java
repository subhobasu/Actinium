package ch.ethz.inf.vs.appserver.jscoap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import coap.Request;
import coap.Response;

/**
 * AbstractSender provides methods for calling JavaScript functions (listeners).
 * 
 * @author Martin Lanter
 */
public abstract class AbstractSender implements Sender {

	@Override
	public abstract void send(Request request);
	
	@Override
	public abstract void abort();

	/**
	 * Calls the specified JavaScript function on the specified this object with
	 * the specified arguments
	 * 
	 * @param function the JavaScript function
	 * @param thisobj the this object
	 * @param args the parameters
	 */
	protected static void callJavaScriptFunction(Function function, CoAPRequest thisobj, Object... args) {
		if (function!=null) {
			try {
				Context cx = Context.enter();
				Scriptable scope = function.getParentScope();
				function.call(cx, scope, Context.toObject(thisobj, scope), args);
			} finally {
				Context.exit();
			}
		}
	}
	
	/**
	 * Returns true, if the specified response only is an empty acknowledgement
	 * @param response the response
	 * @return true, if the specified response only is an empty acknowledgement
	 */
	protected boolean isAcknowledgement(Response response) {
		return response.isEmptyACK();
	}
}
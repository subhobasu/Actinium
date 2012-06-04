package ch.ethz.inf.vs.actinium.jscoap;

import java.util.List;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import ch.ethz.inf.vs.californium.coap.Message.messageType;
import ch.ethz.inf.vs.californium.coap.Option;
import ch.ethz.inf.vs.californium.coap.OptionNumberRegistry;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;

public class JavaScriptCoAPRequest extends ScriptableObject implements CoAPConstants {

	private Request request;
	
	private Response response = new Response();;
	
	/*
	 * Rhino: Needs an empty constructor for ScriptableObjects
	 */
	public JavaScriptCoAPRequest() {
		// do nothing
	}
	
	public JavaScriptCoAPRequest(Request request) {
		this.request = request;
	}
	
	@Override
	public String getClassName() {
		return "JavaScriptCoAPRequest";
	}
	
	/*
	 * Rhino: Needs JavaScript constructor
	 */
    public void jsConstructor() {
    }
	
    // Fields for JavaScript //
	
    public String jsGet_payloadText() {
		return request.getPayloadString();
	}
	
	public long jsGet_startTime() {
		return request.startTime;
	}
	
	// Functions for JavaScript //

	public String jsFunction_getPayloadString() {
		return request.getPayloadString();
	}
	
	public void jsFunction_accept() {
		request.accept();
	}
	
	/*
	 * Rhino: Only one method jsFunction_respond is allowed
	 */
	public void jsFunction_respond(Object jscode, Object jsmessage, Object jscontentType) {
		respond(jscode, jsmessage, jscontentType);
	}
	
	public String jsFunction_getPayload() {
		return request.getPayloadString();
	}
	
	public int jsFunction_payloadSize() {
		return request.payloadSize();
	}
	
	public int jsFunction_getVersion() {
		return request.getVersion();
	}
	
	public int jsFunction_getMID() {
		return request.getMID();
	}
	
	public void jsFunction_setMID(int mid) {
		response.setMID(mid);
	}
	
	public String jsFunction_getUriPath() {
		return request.getUriPath();
	}
	
	public String jsFunction_getQuery() {
		return request.getQuery();
	}
	
	public int jsFunction_getContentType() {
		return request.getContentType();
	}
	
	public String jsFunction_getTokenString() {
		return request.getTokenString();
	}
	
	public int jsFunction_getMaxAge() {
		return request.getMaxAge();
	}
	
	public String jsFunction_getLocationPath() {
		return request.getLocationPath();
	}
	
	public void jsFunction_setLocationPath(String locationPath) {
		response.setLocationPath(locationPath);
	}

	public String jsFunction_key() {
		return request.key();
	}
	
	public String jsFunction_transactionKey() {
		return request.transactionKey();
	}
	
	public String jsFunction_sequenceKey() {
		return request.sequenceKey();
	}
	
	public messageType jsFunction_getType() {
		return request.getType();
	}
	
	public long jsFunction_getTimestamp() {
		return request.getTimestamp();
	}
	
	public boolean jsFunction_isConfirmable() {
		return request.isConfirmable();
	}
	
	public boolean jsFunction_isNonConfirmable() {
		return request.isNonConfirmable();
	}
	
	public boolean jsFunction_isAcknowledgement() {
		return request.isAcknowledgement();
	}

	public boolean jsFunction_isReset() {
		return request.isReset();
	}
	
	public boolean jsFunction_isReply() {
		return request.isReply();
	}
	
	public boolean jsFunction_isEmptyACK() {
		return request.isEmptyACK();
	}
	
	public boolean jsFunction_requiresToken() {
		return request.requiresToken();
	}
	
	public String jsFunction_toString() {
		return request.toString();
	}
	
	public String jsFunction_typeString() {
		return request.typeString();
	}
	
	// options
	public void jsFunction_setResponseHeader(String header, Object value)  {
		if (value instanceof Integer)
			setResponseHeader(header, (Integer) value);
		else if (value instanceof String)
			setResponseHeader(header, (String) value);
		else
			setResponseHeader(header, value.toString());
	}
	
	public String jsFunction_getAllRequestHeaders() {
		return getAllRequestHeaders();
	}
	
	public String jsFunction_getRequestHeader(String header) {
		return getRequestHeader(header);
	}
		
	private void setResponseHeader(String header, String value)  {
		int nr = CoAPConstantsConverter.convertHeaderToInt(header);
		if (nr==OptionNumberRegistry.CONTENT_TYPE) {
			// we also have to parse the value to get it as integer
			int contentType = CoAPConstantsConverter.convertStringToContentType(value);
			response.addOption(new Option(contentType,nr));
		} else if (nr==OptionNumberRegistry.ACCEPT) {
			// we also have to parse the value to get it as integer
			int contentType = CoAPConstantsConverter.convertStringToContentType(value);
			response.addOption(new Option(contentType,nr));
		} else {
			response.addOption(new Option(value, nr));
		}
	}
	
	private void setResponseHeader(String header, int value)  {
		int nr = CoAPConstantsConverter.convertHeaderToInt(header);
		response.addOption(new Option(value, nr));
	}
	
	private String getAllRequestHeaders() {
		final String nl = "\r\n";
		final String col = ": ";
		StringBuffer buffer = new StringBuffer();
		for (Option opt : request.getOptions()) {
			buffer.append(OptionNumberRegistry.toString(opt.getOptionNumber()));
			buffer.append(col);
			buffer.append(opt.toString());
			buffer.append(nl);
		}
		return buffer.toString();
	}
	
	private String getRequestHeader(String header) {
		int nr = CoAPConstantsConverter.convertHeaderToInt(header);
		return getRequestHeader(nr);
	}
	
	private String getRequestHeader(int nr) {
		String col = ": ";
		List<Option> opts = request.getOptions(nr);
		return OptionNumberRegistry.toString(nr)+col+deflat(opts);
	}
	
	private String deflat(List<Option> opts) {
		String sep = ", ";
		StringBuffer buffer = new StringBuffer();
		for (int i=0;i<opts.size();i++) {
			buffer.append(opts.get(i).toString());
			if (i<opts.size()-1)
				buffer.append(sep);
		}
		return buffer.toString();
	}
	
	private void respond(Object jscode, Object jsmessage, Object jscontentType) {
//		System.out.println("respond with jscode "+jscode+" ("+jscode.getClass()+"), " +
//		"jsmsg "+jscode+" ("+(jsmessage!=null?jscode.getClass():"-")+"), " +
//		"jsct "+jscontentType+" ("+(jscontentType!=null?jscontentType.getClass():"-")+")");

		Integer code;
		String message;
		Integer contentType;

		// Parse code (e.g. 69, 2.05 or "Content")
		if (jscode instanceof Integer)
			code = (Integer) jscode;
		else if (jscode instanceof String)
			code = CoAPConstantsConverter.convertStringToCode((String) jscode);
		else if (jscode instanceof Double)
			code = CoAPConstantsConverter.convertNumCodeToCode((Double) jscode);
		else if (jscode instanceof Float)
			code = CoAPConstantsConverter.convertNumCodeToCode(((Float) jscode).doubleValue());
		else
			throw new IllegalArgumentException( "JavaScriptCoAPRequest.respond expects a String, Integer or Double as first argument but got "+jscode);

		// Parse message
		if (jsmessage == null)
			message = "null";
		else if (jsmessage instanceof Undefined)
			message = null; // Either, no jsmessage has been provided or it was an undefined variable
		else
			message = jsmessage.toString();

		// Parse content type (e.g. "text/plain", 0 or nothing)
		if (jscontentType instanceof Integer)
			contentType = (Integer) jscontentType;
		else if (jscontentType instanceof String)
			contentType = CoAPConstantsConverter .convertStringToContentType((String) jscontentType);
		else
			contentType = null;

		// Respond to the request
		response.setCode(code);
		if (message != null)
			response.setPayload(message);
		if (contentType != null)
			response.setContentType(contentType);
		request.respond(response);
	}
	
}

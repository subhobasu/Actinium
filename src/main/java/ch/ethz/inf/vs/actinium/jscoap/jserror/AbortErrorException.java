package ch.ethz.inf.vs.actinium.jscoap.jserror;

public class AbortErrorException extends RequestErrorException {

	private static final long serialVersionUID = -2833273004174819723L;

	public AbortErrorException() {
		super();
	}

	public AbortErrorException(String message) {
		super(message);
	}

	public AbortErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public AbortErrorException(Throwable cause) {
		super(cause);
	}
	
}

package ch.ethz.inf.vs.actinium.jscoap.jserror;


public class NetworkErrorException extends RequestErrorException {

	private static final long serialVersionUID = -1781934300982074011L;

	public NetworkErrorException() {
		super();
	}

	public NetworkErrorException(String message) {
		super(message);
	}

	public NetworkErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public NetworkErrorException(Throwable cause) {
		super(cause);
	}
	
}

package ch.ethz.inf.vs.actinium.jscoap.jserror;

public class TimeoutErrorException extends RequestErrorException {

	private static final long serialVersionUID = 8201481582666993805L;

	public TimeoutErrorException() {
		super();
	}

	public TimeoutErrorException(String message) {
		super(message);
	}

	public TimeoutErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeoutErrorException(Throwable cause) {
		super(cause);
	}
	
}

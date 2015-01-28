package wt.exception;

public class WrongContentException extends Exception {

	private static final String errorMsg = "Wrong content found.";
	
	/**
	 * Serial version UID (serialization).
	 */
	private static final long serialVersionUID = 5772058031567957145L;

	public WrongContentException() {
		super(errorMsg);
	}

	public WrongContentException(String message) {
		super(errorMsg+"\n"+message);
	}

	public WrongContentException(Throwable cause) {
		super(cause);
	}

	public WrongContentException(String message, Throwable cause) {
		super(errorMsg+"\n"+message, cause);
	}

}

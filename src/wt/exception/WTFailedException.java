package wt.exception;

public class WTFailedException extends Exception {

	private static final String errorMsg = "Web transaction failed.";
	
	/**
	 * Serial version UID (serialization).
	 */
	private static final long serialVersionUID = 2574326265737661124L;
	
	
	public WTFailedException() {
		super(errorMsg);
	}

	public WTFailedException(String message) {
		super(errorMsg+"\n"+message);
	}

	public WTFailedException(Throwable cause) {
		super(cause);
	}

	public WTFailedException(String message, Throwable cause) {
		super(errorMsg+"\n"+message, cause);
	}

}
package wt.exception;

public class WTScriptInvalidException extends Exception {

	private static final String errorMsg = "Web transaction script was invalid.";
	
	/**
	 * Serial version UID (serialization).
	 */
	private static final long serialVersionUID = 2574326265737661124L;
	
	
	public WTScriptInvalidException() {
		super(errorMsg);
	}

	public WTScriptInvalidException(String message) {
		super(errorMsg+"\n"+message);
	}

	public WTScriptInvalidException(Throwable cause) {
		super(cause);
	}

	public WTScriptInvalidException(String message, Throwable cause) {
		super(errorMsg+"\n"+message, cause);
	}

}

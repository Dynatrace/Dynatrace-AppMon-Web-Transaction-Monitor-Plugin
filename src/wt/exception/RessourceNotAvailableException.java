package wt.exception;

public class RessourceNotAvailableException extends Exception {

	private static final String errorMsg = "Ressource was not available.";
	
	/**
	 * Serial version UID (serialization).
	 */
	private static final long serialVersionUID = -5588896880309463855L;


	public RessourceNotAvailableException() {
		super(errorMsg);
	}

	public RessourceNotAvailableException(String message) {
		super(errorMsg+"\n"+message);
	}

	public RessourceNotAvailableException(Throwable cause) {
		super(cause);
	}

	public RessourceNotAvailableException(String message, Throwable cause) {
		super(errorMsg+"\n"+message, cause);
	}

}

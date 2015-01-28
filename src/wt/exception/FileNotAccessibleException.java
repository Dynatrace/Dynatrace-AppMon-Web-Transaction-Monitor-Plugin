package wt.exception;

public class FileNotAccessibleException extends Exception {

	private static final String errorMsg = "Necessary file not accessible.";
	
	/**
	 * Serial version UID (serialization).
	 */
	private static final long serialVersionUID = -2039623739681823943L;

	public FileNotAccessibleException() {
		super(errorMsg);
	}

	public FileNotAccessibleException(String message) {
		super(errorMsg+"\n"+message);
	}

	public FileNotAccessibleException(Throwable cause) {
		super(cause);
	}

	public FileNotAccessibleException(String message, Throwable cause) {
		super(errorMsg+"\n"+message, cause);
	}

}

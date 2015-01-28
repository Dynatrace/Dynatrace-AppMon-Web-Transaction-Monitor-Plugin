package wt.exception;

public class InvalidConfigurationException extends Exception {
	
	private static final String errorMsg ="Invalid Configuration.";
	
	/**
	 * Serial version UID (serialization).
	 */
	private static final long serialVersionUID = -7973527511226949390L;
	
	public InvalidConfigurationException() {
		super(errorMsg);
	}

	public InvalidConfigurationException(String message) {
		super(errorMsg+"\n"+message);
	}

	public InvalidConfigurationException(Throwable cause) {
		super(cause);
	}

	public InvalidConfigurationException(String message, Throwable cause) {
		super(errorMsg+"\n"+message, cause);
	}
}

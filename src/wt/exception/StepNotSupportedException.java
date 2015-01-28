package wt.exception;

public class StepNotSupportedException extends Exception {

	private static final String errorMsg = "Step not supported by Web Transaction Monitor.";
	
	/**
	 * Serial version UID (serialization).
	 */
	private static final long serialVersionUID = -7369056810468191574L;
	
	public StepNotSupportedException() {
		super(errorMsg);
	}

	public StepNotSupportedException(String message) {
		super(errorMsg+"\n"+message);
	}

	public StepNotSupportedException(Throwable cause) {
		super(cause);
	}

	public StepNotSupportedException(String message, Throwable cause) {
		super(errorMsg+"\n"+message, cause);
	}
}

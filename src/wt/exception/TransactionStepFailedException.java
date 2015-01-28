package wt.exception;

public class TransactionStepFailedException extends Exception {

	private static final String errorMsg = "TransactionStep failed.";
	
	/**
	 * Serial version UID (serialization).
	 */
	private static final long serialVersionUID = 3810593182456457132L;
	
	public TransactionStepFailedException() {
		super(errorMsg);
	}

	public TransactionStepFailedException(String message) {
		super(errorMsg+"\n"+message);
	}

	public TransactionStepFailedException(Throwable cause) {
		super(cause);
	}

	public TransactionStepFailedException(String message, Throwable cause) {
		super(errorMsg+"\n"+message, cause);
	}

}

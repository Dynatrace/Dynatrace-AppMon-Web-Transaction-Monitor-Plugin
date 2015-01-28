package wt.exception;

public class CouldNotEstablishConnectionException extends Exception {

	private static final String errorMsg = "Could not establish Connection.";
	
	/**
	 * Serial version UID (serialization).
	 */
	private static final long serialVersionUID = 1388029755657153125L;

	public CouldNotEstablishConnectionException(){
		super(errorMsg);
	}

	public CouldNotEstablishConnectionException(String message) {
		super(errorMsg+"\n"+message);
	}

	public CouldNotEstablishConnectionException(Throwable cause) {
		super(cause);
	}

	public CouldNotEstablishConnectionException(String message, Throwable cause) {
		super(errorMsg+"\n"+message, cause);
	}

}

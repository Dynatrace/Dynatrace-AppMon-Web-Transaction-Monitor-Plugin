package wt.exception;

public class HttpProxyNotReachableException extends Exception {

	private static final String errorMsg ="Http proxy not reachable.";
	
	/**
	 * Serial version UID (serialization).
	 */
	private static final long serialVersionUID = -5470715672362998830L;

	public HttpProxyNotReachableException() {
		super(errorMsg);
	}

	public HttpProxyNotReachableException(String message) {
		super(errorMsg+"\n"+message);
	}

	public HttpProxyNotReachableException(Throwable cause) {
		super(cause);
	}

	public HttpProxyNotReachableException(String message, Throwable cause) {
		super(errorMsg+"\n"+message, cause);
	}

}

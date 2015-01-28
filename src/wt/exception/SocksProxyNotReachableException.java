package wt.exception;

public class SocksProxyNotReachableException extends Exception {

	private static final String errorMsg = "Socks proxy not reachable.";
	
	/**
	 * Serial version UID (serialization).
	 */
	private static final long serialVersionUID = -323494634514053893L;

	public SocksProxyNotReachableException() {
		super(errorMsg);
	}

	public SocksProxyNotReachableException(String message) {
		super(errorMsg+"\n"+message);
	}

	public SocksProxyNotReachableException(Throwable cause) {
		super(cause);
	}

	public SocksProxyNotReachableException(String message, Throwable cause) {
		super(errorMsg+"\n"+message, cause);
	}

}

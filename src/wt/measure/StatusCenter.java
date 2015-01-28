package wt.measure;

import com.dynatrace.diagnostics.pdk.Status;
import wt.util.IExceptionHandler;
import wt.util.WTExceptionHandler;

import java.util.LinkedList;

public class StatusCenter implements IStatusCenter{
	
	/** status of plug-in */
	private StringBuilder statusMessage;
	private LinkedList<String> shortMessages;
	private LinkedList<Throwable> exceptions;
	private LinkedList<Integer> statusCodes;
	
	private IExceptionHandler exceptionHandler;
	
	public StatusCenter(){
		exceptionHandler = new WTExceptionHandler(this);
		
		statusMessage = new StringBuilder();
		shortMessages = new LinkedList<String>();
		exceptions = new LinkedList<Throwable>();
		statusCodes = new LinkedList<Integer>();
	}
	
	@Override
	public void clear() {
		
		exceptionHandler.clear();
		
		reset();
	}
	
	@Override
    public void reset() {
		statusMessage = new StringBuilder();
		exceptions.clear();
		shortMessages.clear();
		statusCodes.clear();
	}
	
	@Override
    public void appendShortMessage(String shortMessage) {
		shortMessages.addLast(shortMessage);
	}
	
	@Override
    public void prependStatusMessage(String msg) {
		statusMessage.insert(0, msg);
	}
	
	@Override
    public void appendStatusMessage(String msg) {
		statusMessage.append(msg);
	}

	@Override
    public void appendException(Throwable exception) {
		exceptions.addLast(exception);
	}

	@Override
    public String getShortMessage() {
		if(shortMessages.isEmpty()) {
			return null;
		} else {
			return shortMessages.getFirst();
		}
	}

	@Override
    public String getStatusMessage() {
		return statusMessage.toString();
	}

	@Override
    public Exception getException() {
		if(exceptions.isEmpty()) {
			return null;
		} else {
			return new Exception(exceptions.getFirst());
		}
	}

	@Override
    public boolean hasExceptionOccurred() {
		return !exceptions.isEmpty();
	}

	@Override
	public IExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	@Override
	public void appendStatusCode(Integer code) {
		statusCodes.addLast(code);
	}

	@Override
	public Integer getStatusCode() {
		if(statusCodes.isEmpty()) {
			return null;
		} else {
			return statusCodes.getFirst();
		}
	}
	
	@Override
    public Status getStatus() {
		Status status = new Status();
		
		Integer statusCode = getStatusCode();
		String statusMsg = getStatusMessage();
		String shortMsg = getShortMessage();
		Exception e = getException();
		
		if(statusCode != null ){
			status.setStatusCode(Status.getStatusCode(statusCode));
		}
		
		if(statusMsg != null) {
			status.setMessage(statusMsg);
		}
		
		if(shortMsg != null) {
			status.setShortMessage(shortMsg);
		}
		
		if(e != null) {
			status.setException(e);
		}
		
		return status;
	}
}

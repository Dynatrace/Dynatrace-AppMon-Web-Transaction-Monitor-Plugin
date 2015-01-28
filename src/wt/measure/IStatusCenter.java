package wt.measure;

import wt.util.IExceptionHandler;

import com.dynatrace.diagnostics.pdk.Status;

public interface IStatusCenter {

	void reset();
	
	void clear();

	void appendShortMessage(String shortMessage);
	
	void prependStatusMessage(String msg);
	
	void appendStatusMessage(String msg);

	void appendException(Throwable exception);
	
	void appendStatusCode(Integer code);

	String getShortMessage();

	String getStatusMessage();

	Exception getException();
	
	Integer getStatusCode();

	Status getStatus();
	
	boolean hasExceptionOccurred();
	
	IExceptionHandler getExceptionHandler();
}

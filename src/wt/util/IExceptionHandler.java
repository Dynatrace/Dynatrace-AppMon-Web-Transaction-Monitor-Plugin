package wt.util;

import java.util.logging.Logger;

public interface IExceptionHandler {

	void clear();
	
	boolean isExceptionHandledByContainer(Throwable t);
	
	boolean isExceptionHandledByListener(Throwable t);
	
	void handleException(Throwable t, Logger logger);

}

package wt.util;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.engine.WebTestException;
import com.dynatrace.diagnostics.pdk.Status;
import org.apache.commons.httpclient.ProtocolException;
import org.apache.tools.ant.BuildException;
import org.xml.sax.SAXParseException;
import wt.exception.CouldNotEstablishConnectionException;
import wt.exception.FileNotAccessibleException;
import wt.exception.WTFailedException;
import wt.exception.WTScriptInvalidException;
import wt.measure.IStatusCenter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WTExceptionHandler implements IExceptionHandler {

	private IStatusCenter statusCenter;
	
	public WTExceptionHandler(IStatusCenter statusCenter) {
		this.statusCenter = statusCenter;
	}
	
	@Override
	public void clear(){
		
	}
	
	@Override
	public boolean isExceptionHandledByContainer(Throwable t) {
		boolean result = false;
		
		if(t instanceof ConnectException) result = true;
		if(t instanceof FileNotAccessibleException) result = true;
		if(t instanceof SAXParseException) result = true;
		if(t instanceof BuildException) result = true;
		
		return result;
		
	}
	
	@Override
	public boolean isExceptionHandledByListener(Throwable t) {
		boolean result = false;
		
		if(t instanceof StepFailedException) result = true;
		if(t instanceof StepExecutionException) result = true;
		if(t instanceof WebTestException) result = true;
		if(t instanceof ProtocolException) result = true;
		
		return result;
	}
	
	private void handleContainerException(Throwable t, Logger logger) {
		Throwable exception = null;
		String msg = null;
		Integer code = null;
		
		Throwable cause = t.getCause();
		
		/** exceptions handled by container */
		if(t instanceof ConnectException) {
			msg = t.getClass().getName() + ": check your connection settings.";
			code = Status.StatusCode.ErrorInfrastructure.getCode();
			exception = new CouldNotEstablishConnectionException(msg, t);
		} else if (t instanceof FileNotAccessibleException) {
			exception = t;
			msg = t.getMessage();
			code = Status.StatusCode.ErrorInfrastructure.getCode();
		} else if(t instanceof SAXParseException) {
			msg = t.getMessage();
			exception = new WTScriptInvalidException(msg, t);
			code = Status.StatusCode.ErrorInfrastructure.getCode();
		}else if(t instanceof BuildException) {
			if (cause != null) {
				msg = cause.getMessage();
			} else {
				msg = t.getMessage();
			}
			code = Status.StatusCode.PartialSuccess.getCode();
			exception =  new WTFailedException(msg, t);
		} else {
			handleUnexpectedException(t, logger);
		}
		
		setExceptionStatus(exception, msg, code);
	}
	
	private void setExceptionStatus(Throwable exception, String msg,
			Integer code) {
		
		/** report exception to status center */
		if(exception != null){
			statusCenter.appendException(exception);
			statusCenter.appendShortMessage(exception.getClass().getSimpleName());
		}
		if(msg != null){
			statusCenter.prependStatusMessage(msg);
		}
		if(code != null) {
			statusCenter.appendStatusCode(code);
		}
	}

	private void handleListenerException(Throwable t, Logger logger) {
		Throwable exception = null;
		String msg = null;
		Integer code = null;
		
		Throwable cause = t.getCause();
		
		/** exceptions handled by listener */
		if(t instanceof StepFailedException) {
			msg = "###StepFailedException" + t.getMessage();
		} else if(t instanceof StepExecutionException) {
			if (cause != null && cause instanceof ConnectException){
				msg =  "\nCheck your test script (URLs) or proxy settings: " + cause.getMessage();
			} else if(cause!=null && cause instanceof RuntimeException) {
				msg =  "\nCheck your test script and connectivity: " + cause.getMessage();
			} else {
				msg = t.getMessage();
				code = Status.StatusCode.PartialSuccess.getCode();//Status.StatusCode.ErrorInfrastructure.getCode();
				exception =  t;
			}
		} else if(t instanceof WebTestException){
			msg = "###WebTestException: " + t.getMessage();
		} else if(t instanceof ProtocolException) {
			//ignore
		} else {
			handleUnexpectedException(t, logger);
		}

		setExceptionStatus(exception, msg, code);
	}
	
	private void handleUnexpectedException(Throwable t, Logger logger) {
		statusCenter.appendStatusMessage(WTExceptionHandler.stackTraceToString(t));
		statusCenter.appendShortMessage("Exception occurred: " + t.getMessage());
		statusCenter.appendException(new Exception(t));
		statusCenter.appendStatusCode(Status.StatusCode.ErrorInfrastructure.getCode());
		
		if(logger.isLoggable(Level.FINE)){
			logger.fine("Notified of exception: " + WTExceptionHandler.stackTraceToString(t));
		}
		if(logger.isLoggable(Level.WARNING)){
			logger.warning("Notified of exception: " + t.getMessage());
		}
		
	}
	
	@Override
	public void handleException(Throwable t, Logger logger) {
		if(isExceptionHandledByContainer(t)){
			handleContainerException(t, logger);
		} else if (isExceptionHandledByListener(t)){
			handleListenerException(t, logger);
		} else {
			handleUnexpectedException(t, logger);
		}
	}
	
	 /**returns stacktrace as string, for avoiding e.printStackTrace()
	  * @param throwable
	  * @return
	  * @author hackl
	  */
	public static final String stackTraceToString (Throwable throwable) {
		if (throwable != null) {
			PrintWriter printWriter = null;
			try {
               StringWriter stringWriter = new StringWriter();
               try {
                   printWriter = new PrintWriter(stringWriter);
                   throwable.printStackTrace(printWriter);
                   printWriter.flush();
               } // try
               finally {
               	stringWriter.close();
               } // finally
               return stringWriter.toString();
           } // finally 
			catch (Exception e) {
				// This block should never be entered. In case we cannot write to Dump Stack in
				// memory, catch the exception to prevent main program from failing.
           } // catch
		}
		return ""; // write empty string, if we could not determine the Stacktrace.
	} 
	
	/**
	 * <p>Safe Implementation for getting a printable Message for an Exception.</p>
	 * <p>Prefer this method over t.getMessage() or t.getLocalMessage().</p>
	 * <p>The Implementation tries to get the Message for this Excpeption. If there is no Message
	 * simply the Classname is returned.</p> 
	 * @param t the Throwable to get a Message for
	 * @return The message or the Classname for this String
	 * @author hpw
	 */
	public static final String getMessage(Throwable t) {
		if (null == t) return "";
		if (t.getMessage()!=null) return t.getMessage();
		return t.getClass().getName();
	}



}

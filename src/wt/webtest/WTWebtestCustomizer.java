package wt.webtest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.params.HttpClientParams;

import wt.listener.WTStepExecutionListener;
import wt.listener.WTWebConnectionWrapper;
import wt.measure.IMeasureCenter;
import wt.measure.IStatusCenter;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.interfaces.IWebtestCustomizer;
import com.canoo.webtest.reporting.StepExecutionListener;
import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;

public class WTWebtestCustomizer implements IWebtestCustomizer {

	private static final Logger logger = Logger.getLogger(WTWebtestCustomizer.class.getName());
	private HttpVersion httpProtocolVersion;
	private IStatusCenter statusCenter;
	private IMeasureCenter measureCenter;
	private final boolean logContent;

	public WTWebtestCustomizer(IMeasureCenter measureCenter, IStatusCenter statusCenter, HttpVersion httpProtocolVersion, boolean logContent) {
		this.statusCenter = statusCenter;
		this.measureCenter = measureCenter;
		this.httpProtocolVersion = httpProtocolVersion;
		this.logContent = logContent;
	}

	public StepExecutionListener createExecutionListener(WebtestTask wt) {
		WTStepExecutionListener result = new WTStepExecutionListener(wt.getConfig().getContext(), logContent);
		result.setIMeasureCenter(measureCenter);
		result.setIStatusCenter(statusCenter);
		return result;
	}

	public WebClient customizeWebClient(WebClient wc) {

		if(wc.getWebConnection() instanceof HttpWebConnection) {
			HttpWebConnection httpWebConn = (HttpWebConnection)(wc.getWebConnection());
			Method getHttpClient;
			try {
				getHttpClient = httpWebConn.getClass().getDeclaredMethod("getHttpClient", new Class[]{});
	            if (!getHttpClient.isAccessible()) {
	            	getHttpClient.setAccessible(true);
	            }
	            HttpClient httpClient = (HttpClient)getHttpClient.invoke(httpWebConn, new Object[]{});

	    		httpClient.getParams().setParameter(HttpClientParams.PROTOCOL_VERSION, httpProtocolVersion);

			} catch (SecurityException e) {
				if(logger.isLoggable(Level.SEVERE))
					logger.severe("SecurityException occurred when trying to access HttpClient");
			} catch (IllegalAccessException e) {
				if(logger.isLoggable(Level.SEVERE))
					logger.severe("IllegalAccessException occurred when trying to access HttpClient");
			} catch (IllegalArgumentException e) {
				if(logger.isLoggable(Level.SEVERE))
					logger.severe("IllegalArgumentException occurred when trying to access HttpClient");
			} catch (InvocationTargetException e) {
				if(logger.isLoggable(Level.SEVERE))
					logger.severe("InvocationTargetException occurred when trying to access HttpClient");
			} catch (NoSuchMethodException e) {
				if(logger.isLoggable(Level.SEVERE))
					logger.severe("NoSuchMethodException occurred when trying to access HttpClient");
			}

		} else {
			logger.severe("webclient was no instance of HttpWebConnection: " + wc.getWebConnection().getClass());
		}

		WTWebConnectionWrapper webConnectionWrapper = new WTWebConnectionWrapper(wc);
		webConnectionWrapper.setIMeasureCenter(measureCenter);

		return wc;
	}

}

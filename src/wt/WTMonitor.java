package wt;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import wt.exception.InvalidConfigurationException;
import wt.measure.IMeasureCenter;
import wt.measure.IMeasureCenterKeeper;
import wt.measure.IStatusCenter;
import wt.measure.IStatusCenterKeeper;
import wt.measure.MeasureCenter;
import wt.measure.StatusCenter;
import wt.util.logging.LogLevelMapping;
import wt.util.logging.RedirectAppender;

import com.dynatrace.diagnostics.pdk.Monitor;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Status.StatusCode;

public class WTMonitor implements Monitor, IMeasureCenterKeeper, IStatusCenterKeeper {

	/** static counter of wtm instances */
	private static Object instanceCountLock = new Object();
	private static int instanceCount = 0;

	/** current web transaction (WT) container */
	private WTContainer currentWTContainer;
	/** configuration for current web transaction container */
	private WTContainerConfig config;

	/** MeasureCenter where all measurements are collected */
	private IMeasureCenter measureCenter;
	private IStatusCenter statusCenter;

	/** monitoring environment logger */
	private static final Logger logger = Logger.getLogger(WTMonitor.class.getName());

	private boolean isConfigured;

	static {
		// redirect log4j stuff into java logger
		org.apache.log4j.Logger.getRootLogger().addAppender(new RedirectAppender(logger));
	}

	/**
	 * Method configures monitoring environment logger. Prevents HtmlUnit and
	 * HttpClient to log.
	 *
	 * @param env monitoring environment containing reference to logger
	 */
	private void configureLogger(MonitorEnvironment env){
		Level level = LogLevelMapping.getJavaUtilLogLevel(config.getLogLevel());

		//configure HtmlUnit logging
		Logger.getLogger("com.canoo.").setLevel(level);
		org.apache.log4j.Logger.getLogger("com.canoo.").setLevel(LogLevelMapping.getLog4JLogLevel(level));
		Logger.getLogger("com.gargoylesoftware.").setLevel(level);
		org.apache.log4j.Logger.getLogger("com.gargoylesoftware.").setLevel(LogLevelMapping.getLog4JLogLevel(level));
		Logger.getLogger("org.apache.commons.httpclient").setLevel(level);
		org.apache.log4j.Logger.getLogger("org.apache.commons.httpclient").setLevel(LogLevelMapping.getLog4JLogLevel(level));

		// set log4j log level to info
		org.apache.log4j.Logger.getRootLogger().setLevel(LogLevelMapping.getLog4JLogLevel(level));
	}

	@Override
	public Status setup(MonitorEnvironment env) throws Exception {
		synchronized (instanceCountLock) {
			instanceCount++;
		}

		isConfigured = false;

		if (logger.isLoggable(Level.FINE))
			logger.fine("WTMonitor.setup() has been called.");

		try {

			// create & update WTMonitorConfig (cached)
	    	if(config == null) {
	    		config = new WTContainerConfig();
	    	}
	    	config.readConfig(env);
	    	configureLogger(env);

			if (measureCenter == null) {
				setIMeasureCenter(new MeasureCenter(config.getTransactionLocation(), config.getTransactionName(), config.isDtdTaggingEnabled()));
			} else {
				getIMeasureCenter().clear();
				getIMeasureCenter().setTransactionLocation(config.getTransactionLocation());
				getIMeasureCenter().setTransactionName(config.getTransactionName());
				getIMeasureCenter().setTaggingEnabled(config.isDtdTaggingEnabled());
			}

			if (statusCenter == null) {
				setIStatusCenter(new StatusCenter());
			} else {
				getIStatusCenter().clear();
			}

			if(logger.isLoggable(Level.FINEST))
	    		logger.finest(config.toString());


	    	//create & update WTContainer (cached)
	    	if(currentWTContainer == null){
	    		currentWTContainer = new WTContainer();
	    		currentWTContainer.setIMeasureCenter(measureCenter);
	    		currentWTContainer.setIStatusCenter(statusCenter);
			}
	    	currentWTContainer.setConfig(config);

	    	if(currentWTContainer.setupTransactions()) {

	    		isConfigured = true;

	    		if(logger.isLoggable(Level.FINEST))
	    			logger.finest("Configuring transactions was successful.");
	    	} else {
	    		statusCenter.getExceptionHandler().handleException(new Exception("Setup was not successful."), logger);
	    	}

		} catch (Throwable t) {
			if(statusCenter!=null) {
				statusCenter.getExceptionHandler().handleException(t, logger);
			}
			else {
				logger.log(Level.SEVERE, "Plugin Internal Error", t);
				return new Status(StatusCode.ErrorInternalException);
			}
		}

		if(logger.isLoggable(Level.FINE))
			logger.fine("WTMonitor.setup() is done.");

		Status status = statusCenter.getStatus();
		if(!Status.StatusCode.Success.equals(status.getStatusCode()) && !Status.StatusCode.PartialSuccess.equals(status.getStatusCode())) {
			synchronized (instanceCountLock) {
				instanceCount--;
			}
		}

		return status;
	}

	@Override
	public void teardown(MonitorEnvironment env) {

		try{
			if (logger.isLoggable(Level.FINE))
                logger.fine("WTMonitor.teardown() has been called.");

			currentWTContainer.teardownTransactions();

			currentWTContainer = null;

			measureCenter = null;

		}catch(Throwable t) {

			statusCenter.getExceptionHandler().handleException(t, logger);
		}

		if (logger.isLoggable(Level.FINE))
            logger.fine("WTMonitor.teardown() done.");

		synchronized(instanceCountLock) {
			instanceCount--;

			if(instanceCount <= 0 ) {
				MultiThreadedHttpConnectionManager.shutdownAll();
			}
		}

	}

	@Override
	public Status execute(MonitorEnvironment env) throws Exception {
		Status status = new Status();

		if (logger.isLoggable(Level.FINE))
			logger.fine("WTMonitor.execute() has been called.");

		if(!isConfigured) {
            status.setStatusCode(Status.StatusCode.ErrorInfrastructure);
			status.setException(new InvalidConfigurationException());
			status.setShortMessage(InvalidConfigurationException.class.getName());
			status.setMessage("Can't execute Plugin as long as it is not configured properly.");
			return status;
		}

		try {

			measureCenter.reset();
			statusCenter.reset();


			if(currentWTContainer != null) {
				if(currentWTContainer.runTransactions()){
					if(!statusCenter.hasExceptionOccurred()) {
						status.setStatusCode(Status.StatusCode.Success);
					} else {
						status.setStatusCode(Status.StatusCode.ErrorInternal);
						status.setException(new Exception(statusCenter.getException()));
					}
					status.setMessage(statusCenter.getStatusMessage());
					status.setShortMessage(statusCenter.getShortMessage());
				} else {
                    status.setStatusCode(Status.StatusCode.ErrorInternal);
					status.setException(new Exception(statusCenter.getException()));
					status.setMessage(statusCenter.getStatusMessage());
					status.setShortMessage(statusCenter.getShortMessage());
				}
			} else {
                status.setStatusCode(Status.StatusCode.ErrorInternal);
				status.setMessage("WTMonitor was not set up correctly before run.");
			}

			measureCenter.setMeasurements(env);

		} catch (Throwable t) {
			statusCenter.getExceptionHandler().handleException(t, logger);
		}

		if(logger.isLoggable(Level.FINE)) {
			logger.fine("WTMonitor.execute() done.");
		}

		return statusCenter.getStatus();
	}

	@Override
	public IMeasureCenter getIMeasureCenter() {
		return measureCenter;
	}

	@Override
	public void setIMeasureCenter(IMeasureCenter measureCenter) {
		this.measureCenter = measureCenter;
	}

	@Override
	public IStatusCenter getIStatusCenter() {
		return statusCenter;
	}

	@Override
	public void setIStatusCenter(IStatusCenter statusCenter) {
		this.statusCenter = statusCenter;
	}
}

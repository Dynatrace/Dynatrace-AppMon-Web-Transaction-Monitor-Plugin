package wt.listener;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Task;

import wt.measure.IMeasureCenter;
import wt.measure.IMeasureCenterKeeper;
import wt.measure.IStatusCenter;
import wt.measure.IStatusCenterKeeper;
import wt.measure.MeasureCenter;
import wt.measure.MeasureValue;
import wt.util.IExceptionHandler;
import wt.util.WebtestHelper;

import com.canoo.webtest.ant.TestStepSequence;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.reporting.RootStepResult;
import com.canoo.webtest.reporting.StepExecutionListener;
import com.canoo.webtest.reporting.StepResult;
import com.gargoylesoftware.htmlunit.Page;

public class WTStepExecutionListener extends StepExecutionListener implements IMeasureCenterKeeper, IStatusCenterKeeper {

	private IMeasureCenter measureCenter;
	private IStatusCenter statusCenter;
	private StepExecutionListenerConfig listenerConfig;

	private Context context;

	private static final Logger logger = Logger.getLogger(WTStepExecutionListener.class.getName());
	private final boolean logContent;


	/**
	 * CTor.
	 *
	 * @param context WebTest context
	 * @param logContent
	 */
	public WTStepExecutionListener(Context context, boolean logContent) {
		super(context);
		this.context = context;
		this.logContent = logContent;
	}

	@Override
	public void taskStarted(final BuildEvent event) {
		Task task = event.getTask();

		final Map<?,?> attributeMap = task.getRuntimeConfigurableWrapper().getAttributeMap();
		String description = (attributeMap.get("description") == null ? "" : (String)attributeMap.get("description"));
		measureCenter.setTimerName(description);

		super.taskStarted(event);
	}

	@Override
	public void taskFinished(BuildEvent event) {
		super.taskFinished(event);
		if (logContent && event.getException() != null && !(event.getTask() instanceof TestStepSequence)) {
			Page page = context.getCurrentResponse();
			if (page != null && page.getWebResponse() != null) {
				logger.info("Step failed, last received page was:\n" + page.getWebResponse().getContentAsString());
			}
		}
	}

	/**
	 *
	 * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
	 */
	@Override
	public void webtestFinished()
	{
		try {
			RootStepResult rootResult = getRootResult();

			boolean wtSuccessful = (rootResult.getFailingTaskResult()==null);
			long wtDuration = rootResult.getDuration();
			long thinkTime = WebtestHelper.getThinkTimeRecursive(rootResult);

			String summaryReport = WebtestHelper.getStepResultSummary(measureCenter.getTransactionName(), rootResult);
			statusCenter.appendStatusMessage(summaryReport);

			if(logger.isLoggable(Level.FINE))
				logger.fine(summaryReport);

			String detailReport = getStepResultRecursive(rootResult, "");
			statusCenter.appendStatusMessage(detailReport);

			if(logger.isLoggable(Level.FINE))
				logger.fine(detailReport);

			measureCenter.setMeasureValue(MeasureCenter.KEY_TA_SUCCESSFUL, new MeasureValue(wtSuccessful ? 1 : 0));
			measureCenter.setMeasureValue(MeasureCenter.KEY_TA_DURATION, new MeasureValue(wtDuration-thinkTime));

			//exception
			handleRootResultException(rootResult.getException());

			if(wtSuccessful) {
				statusCenter.appendShortMessage("Web Transaction was successful.");
			}

		} catch (Throwable t) {

			IExceptionHandler exceptionHandler = statusCenter.getExceptionHandler();
			if(exceptionHandler.isExceptionHandledByListener(t) || !exceptionHandler.isExceptionHandledByContainer(t)){
				exceptionHandler.handleException(t, logger);
			}
		}
	}



	private void handleRootResultException(Throwable t) {
		if(t != null) {

			IExceptionHandler exceptionHandler = statusCenter.getExceptionHandler();
			if(exceptionHandler.isExceptionHandledByListener(t) || !exceptionHandler.isExceptionHandledByContainer(t)){
				exceptionHandler.handleException(t, logger);
			}

			if(logger.isLoggable(Level.FINEST)){
				logger.finest("Exception occurred, check details.");
			}

		} else {
			if(logger.isLoggable(Level.FINEST)){
				logger.finest("No exception occurred.");
			}
		}
	}



	private String getStepResultRecursive(StepResult stepResult, String prefix) {
		StringBuilder result = new StringBuilder();
		String status;
		int i = 0;
		int nrOfSteps = stepResult.getChildren().size();

		for(Object tmp : stepResult.getChildren()) {

			StepResult tmpResult = (StepResult)tmp;

			if(!tmpResult.isCompleted()){
				status = "skipped";
			} else if(tmpResult.isSuccessful()) {
				status = "ok";
			} else {
				status = "failed";
			}

			result.append(WebtestHelper.printStep(++i, nrOfSteps, prefix, tmpResult.getTaskDescription(), tmpResult.getTaskName(), status));

			if(!tmpResult.getChildren().isEmpty()) {

				String str = (prefix.length() > 0) ? prefix + "." + i : "" + i ;
				result.append(getStepResultRecursive(tmpResult, str));

			}
		}

		return result.toString();
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

	public StepExecutionListenerConfig getStepExecutionListenerConfig() {
		return this.listenerConfig;
	}

	public void setStepExecutionListenerConfig(StepExecutionListenerConfig listenerConfig) {
		this.listenerConfig = listenerConfig;
	}

}

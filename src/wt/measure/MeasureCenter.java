package wt.measure;

import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.apache.commons.httpclient.NameValuePair;
import wt.WTContainerConfig;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * <p></p>
 *
 * @author ardeshir.arfaian
 * @date 17.07.2008
 *
 */
public class MeasureCenter implements IMeasureCenter {

	HashMap<String, MeasureValue> measures;
	
	public final static String KEY_NUM_OF_REQUESTS		= "NumOfRequests";
	public final static String KEY_REQUEST_BYTES 		= "RequestBytes";
	public final static String KEY_RESPONSE_BYTES		= "ResponseBytes";
	public final static String KEY_AGGREGATED_RTT		= "AggregatedRoundTripTime";
	public final static String KEY_TA_DURATION 			= "TransactionDuration";
	public final static String KEY_TA_SUCCESSFUL		= "TransactionSuccessful";
	
	//dynaTrace tagging information
	private String transactionLocation;
	private String transactionName;
	private int transactionId;
    private String timerName;
    private boolean taggingEnabled;
	
    private static final Logger logger = Logger.getLogger(MeasureCenter.class.getName());
	
    /**
	 * CTor. 
	 */
	public MeasureCenter(String transactionLocation, String transactionName, boolean taggingEnabled) {
		this.transactionLocation = transactionLocation;
		this.transactionName = transactionName;
		this.taggingEnabled = taggingEnabled;
		
		clear();
	}
	
	public void setTaggingEnabled(boolean taggingEnabled) {
		this.taggingEnabled = taggingEnabled;
	}
	
	public void setTransactionLocation(String transactionLocation) {
		this.transactionLocation = transactionLocation;
	}

	public void setTransactionName(String transactionName) {
		this.transactionName = transactionName;
	}

	public final void clear() {
		transactionId = -1;
		measures = new HashMap<String, MeasureValue>();
		reset();
	}
	
	
	/**
	 * Reset MeasureCenter.
	 */
	public void reset() {
		
		transactionId++;
		timerName = "";
		
		measures.put(KEY_NUM_OF_REQUESTS, new MeasureValue());
		measures.put(KEY_REQUEST_BYTES, new MeasureValue());
		measures.put(KEY_RESPONSE_BYTES, new MeasureValue());
		measures.put(KEY_AGGREGATED_RTT, new MeasureValue());
		measures.put(KEY_TA_DURATION, new MeasureValue());
		measures.put(KEY_TA_SUCCESSFUL, new MeasureValue());
	}
	
	public MeasureValue getMeasureValue(String key) {
		return measures.get(key);
	}
	
	public void setMeasureValue(String key, MeasureValue value) {
		measures.put(key, value);
	}	
	
	public void increaseMeasureValue(String key, MeasureValue value) {
		MeasureValue mv = getMeasureValue(key);
		mv.setIntValue(mv.getIntValue() + value.getIntValue());
		mv.setLongValue(mv.getLongValue() + value.getLongValue());
	}

	public static long countRequestBytes(final WebRequestSettings webRequestSettings) {
		long requestBytes = 0;
		
		//calculate header bytes
    	for (final Map.Entry<String, String> entry : webRequestSettings.getAdditionalHeaders().entrySet()) {
            requestBytes += entry.getKey().length() + entry.getValue().length();
        }
    	
    	//calculate request body bytes
    	String requestBody = webRequestSettings.getRequestBody();
    	if(requestBody != null) {
    		requestBytes += requestBody.length();
    	}
    	
        //calculate request parameter bytes
    	List<NameValuePair> requestParameter = webRequestSettings.getRequestParameters();
    	if(requestParameter != null) {
	        NameValuePair[] pairs = new NameValuePair[requestParameter.size()];
	        webRequestSettings.getRequestParameters().toArray(pairs);
	        for(NameValuePair nvp : pairs){
	        	requestBytes += nvp.getName().length() + nvp.getValue().length();
	        }
    	}
        
        //calculate url bytes
    	URL url = webRequestSettings.getUrl();
    	if(url != null) {
    		requestBytes += url.toString().length();
    	}
    	
    	return requestBytes;
	}

	public static long countResponseBytes(WebResponse response) {
		long responseBytes = 0;
		
		//calculate status line bytes
		String statusMessage = response.getStatusMessage();
		responseBytes += (response.getStatusCode() + "").length();
		if(statusMessage != null)
			responseBytes += statusMessage.length();
		
		//calculate response header bytes
		List<NameValuePair> responseHeaders = response.getResponseHeaders();
		if(responseHeaders != null) {
			for(NameValuePair nvp : responseHeaders) {
				responseBytes += nvp.getName().length() + nvp.getValue().length();
			}
		}
	
		//calculate response body bytes
		byte[] responseBody = response.getContentAsBytes();
		if(responseBody != null){
			responseBytes += responseBody.length;
		}
		
		return responseBytes;
	}
	
	public void setMeasurements(MonitorEnvironment env) {
		// retrieve and set the measurements
		Collection<MonitorMeasure> measures;
		
		if ((measures = env.getMonitorMeasures(WTContainerConfig.METRIC_GROUP, WTContainerConfig.MSR_TRANSACTION_DURATION)) != null) {
			long transactionDuration = getMeasureValue(MeasureCenter.KEY_TA_DURATION).getLongValue();
			if(logger.isLoggable(Level.FINEST))
				logger.finest("Setting transaction duration measure to " + transactionDuration + " ms");
			for (MonitorMeasure measure : measures)
				measure.setValue(transactionDuration);
		}
		if ((measures = env.getMonitorMeasures(WTContainerConfig.METRIC_GROUP, WTContainerConfig.MSR_TRANSACTION_SUCCESSFUL)) != null) {
			int transactionSuccessful = getMeasureValue(MeasureCenter.KEY_TA_SUCCESSFUL).getIntValue();
			if(logger.isLoggable(Level.FINEST))
				logger.finest("Setting transaction successful measure to " + transactionSuccessful + " yes/no");
			for (MonitorMeasure measure : measures)
				measure.setValue(transactionSuccessful);
		}
		if ((measures = env.getMonitorMeasures(WTContainerConfig.METRIC_GROUP, WTContainerConfig.MSR_AGGREGATED_RTT)) != null) {
			long aggregatedRTT = getMeasureValue(MeasureCenter.KEY_AGGREGATED_RTT).getLongValue();
			if(logger.isLoggable(Level.FINEST))
				logger.finest("Setting round trip time measure to " + aggregatedRTT + " ms");
			for (MonitorMeasure measure : measures)
				measure.setValue(aggregatedRTT);
		}
		if ((measures = env.getMonitorMeasures(WTContainerConfig.METRIC_GROUP, WTContainerConfig.MSR_TRANSACTION_BYTES_SENT)) != null) {
			long requestBytes = getMeasureValue(MeasureCenter.KEY_REQUEST_BYTES).getLongValue();
			if(logger.isLoggable(Level.FINEST))
				logger.finest("Setting transaction bytes sent measure to " + requestBytes + " bytes");
			for (MonitorMeasure measure : measures)
				measure.setValue(requestBytes);
		}
		if ((measures = env.getMonitorMeasures(WTContainerConfig.METRIC_GROUP, WTContainerConfig.MSR_TRANSACTION_BYTES_RECEIVED)) != null) {
			long responseBytes = getMeasureValue(MeasureCenter.KEY_RESPONSE_BYTES).getLongValue();
			if(logger.isLoggable(Level.FINEST))
				logger.finest("Setting transaction bytes received measure to " + responseBytes + " bytes");
			for (MonitorMeasure measure : measures)
				measure.setValue(responseBytes);
		}
		if ((measures = env.getMonitorMeasures(WTContainerConfig.METRIC_GROUP, WTContainerConfig.MSR_NUMBER_OF_REQUESTS)) != null) {
			int numOfRequests = getMeasureValue(MeasureCenter.KEY_NUM_OF_REQUESTS).getIntValue();
			if(logger.isLoggable(Level.FINEST))
				logger.finest("Setting number of requests measure to " + numOfRequests + " number");
			for (MonitorMeasure measure : measures)
				measure.setValue(numOfRequests);
		}
	}

	@Override
	public String getTransactionName() {
		return transactionName;
	}

	@Override
	public void setTimerName(String timerName) {
		if(timerName != null && !"null".equals(timerName)) {
			this.timerName = timerName;
		} else {
			this.timerName = "";
		}
	}
	
	@Override
	public String getTimerName() {
		return timerName;
	}

	@Override
	public int getTransactionId() {
		return transactionId;
	}

	@Override
	public boolean isDtdTaggingEnabled() {
		return taggingEnabled;
	}

	@Override
	public String getTransactionLocation() {
		return transactionLocation;
	}
}

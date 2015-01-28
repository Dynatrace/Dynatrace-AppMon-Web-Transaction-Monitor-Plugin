package wt.measure;

import com.dynatrace.diagnostics.pdk.MonitorEnvironment;

public interface IMeasureCenter {
	
	void reset();
	
	void clear();
	
	MeasureValue getMeasureValue(String key);
	
	void setMeasureValue(String key, MeasureValue value);

	void increaseMeasureValue(String key, MeasureValue value);
	
	void setMeasurements(MonitorEnvironment env);
	
	String getTransactionLocation();
	
	String getTransactionName();
	
	int getTransactionId();
	
	void setTimerName(String timerName);
	
	String getTimerName();
	
	boolean isDtdTaggingEnabled();

	void setTaggingEnabled(boolean taggingEnabled);
	
	void setTransactionLocation(String transactionLocation);
	
	void setTransactionName(String transactionName);
}

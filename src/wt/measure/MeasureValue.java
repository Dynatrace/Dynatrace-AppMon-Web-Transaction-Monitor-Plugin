package wt.measure;

public class MeasureValue {

	private int intValue;
	private long longValue;
	
	public MeasureValue() {
		intValue = 0;
		longValue = 0;
	}
	
	public MeasureValue(int value) {
		intValue = value;
	}
	
	public MeasureValue(long value) {
		longValue = value;
	}
	
	public void setIntValue(int value) {
		intValue = value;
	}
	
	public int getIntValue() {
		return intValue;
	}
	
	public void setLongValue(long value) {
		longValue = value;
	}
	
	public long getLongValue() {
		return longValue;
	}
}

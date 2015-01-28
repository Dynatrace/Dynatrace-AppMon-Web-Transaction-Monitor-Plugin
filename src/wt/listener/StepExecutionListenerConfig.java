package wt.listener;

import org.apache.commons.httpclient.HttpVersion;

public class StepExecutionListenerConfig {

	private HttpVersion httpProtocolVersion;
	
	public StepExecutionListenerConfig() {
	}
	
	public void setHttpProtocolVersion(HttpVersion httpProtocolVersion) {
		this.httpProtocolVersion = httpProtocolVersion;
	}
	
	public HttpVersion getHttpProtocolVersion() {
		return httpProtocolVersion;
	}

}

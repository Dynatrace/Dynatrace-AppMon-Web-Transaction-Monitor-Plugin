package wt.webtest;

import java.util.HashMap;

import wt.WTContainerConfig;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.engine.Configuration;
import com.canoo.webtest.engine.Header;
import com.canoo.webtest.engine.Option;

public class WebTestConfigWrapper {
	
	private Configuration configuration;
	
	/**
	 * Ctor.
	 * 
	 * @param configuration {@link WebtestTask} Configuration to wrap 
	 */
	public WebTestConfigWrapper(Configuration configuration){
		this.configuration = configuration;
		clearHeaders();
	}
	
	/**
	 * Adapts {@link WebtestTask} configuration to Web Transaction Monitor configuration.
	 * 
	 * @param protocol
	 * @param autoRefresh
	 * @param haltOnError
	 * @param haltOnFailure
	 * @param port
	 * @param printSummary
	 * @param timeout
	 */
	public void adaptConfiguration(WTContainerConfig wtMonConf) {
		Option option = new Option();
		option.setName("ThrowExceptionOnFailingStatusCode");
		option.setValue("false");
		
		configuration.setAutoRefresh(wtMonConf.getAutorefresh());
		configuration.setHaltonerror(wtMonConf.isHaltOnError());
		configuration.setHaltonfailure(wtMonConf.isHaltOnFailure());
		configuration.setPort(80);
		configuration.setSummary(wtMonConf.isPrintSummary());
		configuration.setTimeout(wtMonConf.getConnectionTimeout());
		configuration.setSaveresponse(false);
		configuration.setBrowser("Firefox2");
		configuration.addOption(option);
		
		configuration.getRuntimeConfigurableWrapper().setAttribute("haltonfailure", ""+wtMonConf.isHaltOnFailure());
		configuration.getRuntimeConfigurableWrapper().setAttribute("haltonerror", ""+wtMonConf.isHaltOnError());
		configuration.getRuntimeConfigurableWrapper().setAttribute("summary", ""+wtMonConf.isPrintSummary());
		configuration.getRuntimeConfigurableWrapper().setAttribute("saveresponse", "false");
		configuration.getRuntimeConfigurableWrapper().setAttribute("browser", "Firefox2");
		
		//TODO (maybe) if addOption does not work add the xml...
		//configuration.getRuntimeConfigurableWrapper().addChild();
	}
	
	/**
	 * Adds Header to wrapped configuration.
	 * 
	 * @param name header parameter name
	 * @param value header parameter value
	 */
	public void addHeader(String name, String value) {
		Header header = new Header();
		
		header.setName(name);
		header.setValue(value);
		
		configuration.addHeader(header);
	}

	/**
	 * Clears preset headers.
	 */
	private void clearHeaders() {
		configuration.getHeaderList().clear();
	}
	
	/**
	 * Configures WebtestTask and sets headers. See <config> Tag in canoo webtest manual.
	 * 
	 * @param wtMonConf configuration of webtransaction monitor
	 */
	public void configureWebtestTask(WTContainerConfig wtMonConf){
		adaptConfiguration(wtMonConf);

		addHeader("User-Agent", wtMonConf.getUserAgentName());
		//TODO (nice to have) make the accept header customizable
		//addHeader("Accept", "image/jpeg, */*");
		addHeader("Accept-Language", wtMonConf.getUserAgentLanguage());
		HashMap<String,String> customHeaders = wtMonConf.getCustomHeaders();
		for(String headerkey : customHeaders.keySet()){
			String headerValue = customHeaders.get(headerkey);
			addHeader(headerkey, headerValue);
		}
	}
	

}

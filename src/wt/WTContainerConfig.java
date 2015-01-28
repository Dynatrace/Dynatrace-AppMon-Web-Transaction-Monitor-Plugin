package wt;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpVersion;
import org.apache.tools.ant.taskdefs.optional.net.SetProxy;

import wt.ant.AntFacade;

import com.dynatrace.diagnostics.pdk.MonitorEnvironment;

public class WTContainerConfig {

	// configuration files & directories
	private File basedir;
	private File scriptdir;
	private File webtestxml;

	// configuration constants
	private static final String LOG_CONTENT = "logContent";
	private static final String CONFIG_SCRIPT = "script";
	//private static final String CONFIG_HOST = "host";
	private static final String CONFIG_PORT = "port";
	private static final String CONFIG_CUSTOM_HEADERS = "customHeaders";
	private static final String CONFIG_USER_AGENT = "userAgent";
	private static final String CONFIG_USER_AGENT_ALTERNATE = "alternateUserAgent";
	private static final String CONFIG_USER_AGENT_LANGUAGE="userAgentLanguage";
	private static final String CONFIG_CONN_TIMEOUT = "connectionTimeout";
	private static final String CONFIG_DT_TAGGING = "dtTagging";
	private static final String CONFIG_MONITOR_NAME = "transactionName";
	private static final String CONFIG_MONITOR_LOCATION = "transactionLocation";
	private static final String CONFIG_AUTO_REFRESH = "autoRefresh";
	private static final String CONFIG_LOG_LEVEL = "loglevel";
	private static final String CONFIG_PROTOCOL = "protocol";
	private static final String CONFIG_USE_PROXY = "useProxy";
	private static final String CONFIG_HTTP_PROXY_HOST = "httpProxyHost";
	private static final String CONFIG_HTTP_PROXY_PORT = "httpProxyPort";
	private static final String CONFIG_HTTP_PROXY_AUTH = "httpProxyAuth";
	private static final String CONFIG_HTTP_PROXY_USERNAME = "httpProxyUsername";
	private static final String CONFIG_HTTP_PROXY_PASSWORD = "httpProxyPassword";

	// measure constants
	public static final String METRIC_GROUP = "Web Transaction Monitor";
	public static final String MSR_TRANSACTION_DURATION = "TransactionDuration";
	public static final String MSR_TRANSACTION_SUCCESSFUL = "TransactionSuccessful";
	public static final String MSR_AGGREGATED_RTT = "AggregatedRoundTripTime";
	public static final String MSR_TRANSACTION_BYTES_SENT = "TransactionBytesSent";
	public static final String MSR_TRANSACTION_BYTES_RECEIVED = "TransactionBytesReceived";
	public static final String MSR_NUMBER_OF_REQUESTS = "NumberOfRequests";

	public static final String HTTP_PROTOCOL_VERSION_0_9="HTTP/0.9";
	public static final String HTTP_PROTOCOL_VERSION_1_0="HTTP/1.0";
	public static final String HTTP_PROTOCOL_VERSION_1_1="HTTP/1.1";

	//http proxy settings
	private boolean	hasProxy = false;
	private String 	httpProxyHost = "127.0.0.1";
	private int		httpProxyPort = 80;
	private boolean proxyAuth = false;
	private String 	httpProxyUsername = "";
	private String 	httpProxyPassword = "";

	//webtest settings
	private HttpVersion	protocol = HttpVersion.HTTP_1_1;
	private String		autorefresh = "true";
	private boolean		haltOnError = false;
	private boolean		haltOnFailure = false;
	private boolean		printSummary = false;

	//user agent settings
	private HashMap<String,String> customHeaders = null ;
	private String	userAgentLanguage = "en";
	private String	userAgentName = "Mozilla/4.0";

	//dynaTrace tagging
	private String	dtdTaggingTag = "VU=1;PC=.1;ID=4;NA=SearchPage";
	private boolean	dtdTaggingEnabled = true;
	private String	transactionName = "";
	private String	transactionLocation = "";

	//connection timeout
	private int		connectionTimeout = 60;

	//logging level
	private String loglevel = "INFO";

	private String 	script = "";
	private String	host = "";
	private long	port = 80;

	private SetProxy setProxy;
	private boolean logContent;

	public WTContainerConfig()
	{
		customHeaders = new HashMap<String,String>() ;
	}

	/**
	 * Method fits monitoring environment configuration into
	 * web transaction (WT) container configuration.
	 *
	 * @param env monitoring environment containing configuration to use
	 */
	public void readConfig(MonitorEnvironment env) {

		this.script = env.getConfigString(CONFIG_SCRIPT);
		this.host = env.getHost().getAddress();
		this.port = env.getConfigLong(CONFIG_PORT);
		this.setLogContent(env.getConfigBoolean(LOG_CONTENT));

		String customHeaderString = env.getConfigString(CONFIG_CUSTOM_HEADERS) ;
		this.customHeaders = parseCustomHeaderString(customHeaderString) ;

		this.userAgentName = env.getConfigString(CONFIG_USER_AGENT);
		if("other...".equals(this.userAgentName)) {
			this.userAgentName = env.getConfigString(CONFIG_USER_AGENT_ALTERNATE);
		}

		this.connectionTimeout = env.getConfigLong(CONFIG_CONN_TIMEOUT) == null ? 60 : env.getConfigLong(CONFIG_CONN_TIMEOUT).intValue();
		this.dtdTaggingEnabled = env.getConfigBoolean(CONFIG_DT_TAGGING) == null ? false : env.getConfigBoolean(CONFIG_DT_TAGGING);
		if(this.dtdTaggingEnabled){
			this.transactionName = env.getConfigString(CONFIG_MONITOR_NAME) == null ? "" : env.getConfigString(CONFIG_MONITOR_NAME);
			this.transactionLocation = env.getConfigString(CONFIG_MONITOR_LOCATION) == null ? "" : env.getConfigString(CONFIG_MONITOR_LOCATION);
		}

	    hasProxy = env.getConfigBoolean(CONFIG_USE_PROXY);
		if(hasProxy){
			this.httpProxyHost = env.getConfigString(CONFIG_HTTP_PROXY_HOST) == null ? "localhost" : env.getConfigString(CONFIG_HTTP_PROXY_HOST);
			this.httpProxyPort = env.getConfigLong(CONFIG_HTTP_PROXY_PORT) == null ? 80 : env.getConfigLong(CONFIG_HTTP_PROXY_PORT).intValue();
			this.proxyAuth = env.getConfigBoolean(CONFIG_HTTP_PROXY_AUTH) == null ? false : env.getConfigBoolean(CONFIG_HTTP_PROXY_AUTH);
			if (this.proxyAuth) {
				this.httpProxyUsername = env.getConfigString(CONFIG_HTTP_PROXY_USERNAME);
				this.httpProxyPassword = env.getConfigPassword(CONFIG_HTTP_PROXY_PASSWORD);
			}
		} else {
			this.httpProxyHost = "";
			this.httpProxyPort = 80;
			this.httpProxyUsername = "";
			this.httpProxyPassword = "";
		}

		this.autorefresh = "" + (env.getConfigBoolean(CONFIG_AUTO_REFRESH) == null ? true : env.getConfigBoolean(CONFIG_AUTO_REFRESH));
		if(HTTP_PROTOCOL_VERSION_0_9.equals(env.getConfigString(CONFIG_PROTOCOL))) {
			protocol = HttpVersion.HTTP_0_9;
		} else if (HTTP_PROTOCOL_VERSION_1_0.equals(env.getConfigString(CONFIG_PROTOCOL))) {
			protocol = HttpVersion.HTTP_1_0;
		} else if (HTTP_PROTOCOL_VERSION_1_1.equals(env.getConfigString(CONFIG_PROTOCOL))) {
			protocol = HttpVersion.HTTP_1_1;
		}
		this.userAgentLanguage = env.getConfigString(CONFIG_USER_AGENT_LANGUAGE);

		this.loglevel = env.getConfigString(CONFIG_LOG_LEVEL);
	}


	private boolean isValidHeaderDefinition(String header, String value)
	{
		return header != null && value != null
			   && header.length()>0 && value.length()>0 ;
	}

	private HashMap<String, String> parseCustomHeaderString(
			String customHeaderString) {
		Matcher match = Pattern.compile("(.*?)=(.*)").matcher(customHeaderString) ;
		HashMap<String,String> headerList = new HashMap<String,String>() ;
		while (match.find())
		{
			String header = match.group(1) ;
			String headerValue = match.group(2) ;
			if (isValidHeaderDefinition(header, headerValue))
			{
				headerList.put(header, headerValue) ;
			}
		}
		return headerList;
	}

	/**
	 * Method adjusts proxy configuration for current web transaction (WT)
	 * container.
	 *
	 * @param antFacade facade to ant
	 */
	public void configureProxy(AntFacade antFacade) {
		if(hasProxy()){
			setProxy = null;
			if(isProxyAuth()){
				setProxy = antFacade.createSetProxyHttp(
					getHttpProxyHost(),
					getHttpProxyPort(),
					getHttpProxyUsername(),
					getHttpProxyPassword());
			} else {
				setProxy = antFacade.createSetProxyHttp(
					getHttpProxyHost(),
					getHttpProxyPort());
			}
			setProxy.execute();
		} else {
			// reset proxy settings, if a proxy has been set before
			if (setProxy != null) {
				setProxy.setProxyHost("");				
				setProxy.execute();
				setProxy = null;
			}
		}
	}

	public String getHttpProxyHost() {
		return httpProxyHost;
	}

	public void setHttpProxyHost(String proxyHost) {
		this.httpProxyHost = proxyHost;
	}

	public int getHttpProxyPort() {
		return httpProxyPort;
	}

	public void setHttpProxyPort(int proxyPort) {
		this.httpProxyPort = proxyPort;
	}

	public String getHttpProxyUsername() {
		return httpProxyUsername;
	}

	public void setHttpProxyUsername(String proxyUsername) {
		this.httpProxyUsername = proxyUsername;
	}

	public String getHttpProxyPassword() {
		return httpProxyPassword;
	}

	public void setHttpProxyPassword(String proxyPassword) {
		this.httpProxyPassword = proxyPassword;
	}

	public HttpVersion getProtocol() {
		return protocol;
	}

	public void setProtocol(HttpVersion protocol) {
		this.protocol = protocol;
	}

	public String getAutorefresh() {
		return autorefresh;
	}

	public void setAutorefresh(String autorefresh) {
		this.autorefresh = autorefresh;
	}

	public boolean isHaltOnError() {
		return haltOnError;
	}

	public void setHaltOnError(boolean haltOnError) {
		this.haltOnError = haltOnError;
	}

	public boolean isHaltOnFailure() {
		return haltOnFailure;
	}

	public void setHaltOnFailure(boolean haltOnFailure) {
		this.haltOnFailure = haltOnFailure;
	}

	public boolean isPrintSummary() {
		return printSummary;
	}

	public void setPrintSummary(boolean printSummary) {
		this.printSummary = printSummary;
	}

	public final HashMap<String, String> getCustomHeaders() {
		return customHeaders;
	}

	public String getUserAgentLanguage() {
		return userAgentLanguage;
	}

	public void setUserAgentLanguage(String userAgentLanguage) {
		this.userAgentLanguage = userAgentLanguage;
	}

	public String getUserAgentName() {
		return userAgentName;
	}

	public void setUserAgentName(String userAgentName) {
		this.userAgentName = userAgentName;
	}

	public String getDtdTaggingTag() {
		return dtdTaggingTag;
	}

	public void setDtdTaggingTag(String dtdTaggingTag) {
		this.dtdTaggingTag = dtdTaggingTag;
	}

	public boolean isDtdTaggingEnabled() {
		return dtdTaggingEnabled;
	}

	public void setDtdTaggingEnabled(boolean dtdTaggingEnabled) {
		this.dtdTaggingEnabled = dtdTaggingEnabled;
	}

	public String getTransactionLocation() {
		return this.transactionLocation;
	}

	public void setTransactionLocation(String transactionLocation) {
		this.transactionLocation = transactionLocation;
	}

	public String getTransactionName() {
		return this.transactionName;
	}

	public void setTransactionName(String transactionName){
		this.transactionName = transactionName;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public boolean hasProxy() {
		return hasProxy;
	}

	public void setHasProxy(boolean hasProxy) {
		this.hasProxy = hasProxy;
	}

	public boolean isProxyAuth() {
		return proxyAuth;
	}

	public void setProxyAuth(boolean proxyAuth) {
		this.proxyAuth = proxyAuth;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public long getPort() {
		return port;
	}

	public void setPort(long port) {
		this.port = port;
	}

	public File getBasedir() {
		return basedir;
	}

	public void setBasedir(File basedir) {
		this.basedir = basedir;
	}

	public File getScriptdir() {
		return scriptdir;
	}

	public void setScriptdir(File scriptdir) {
		this.scriptdir = scriptdir;
	}

	public File getWebtestxml() {
		return webtestxml;
	}

	public void setWebtestxml(File webtestxml) {
		this.webtestxml = webtestxml;
	}

	public String getLogLevel() {
		return loglevel;
	}

	public void setLogLevel(String loglevel) {
		this.loglevel = loglevel;
	}


	public void setLogContent(boolean logContent) {
		this.logContent = logContent;
	}

	public boolean isLogContent() {
		return logContent;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("_______________________\n");
		sb.append("WTMonitorConfig Begin:\n");
		sb.append("-----------------------\n");

		sb.append("useProxy=");
		sb.append(hasProxy);
		sb.append("\nhttpProxyHost=");
		sb.append(httpProxyHost);
		sb.append("\nhttpProxyPort=");
		sb.append(httpProxyPort);
		sb.append("\nproxyAuth=");
		sb.append(proxyAuth);
		sb.append("\nhttpProxyUsername=");
		sb.append(httpProxyUsername);
		sb.append("\nhttpProxyPassword=");
		sb.append(httpProxyPassword);
		sb.append("\nprotocol=");
		sb.append(protocol);
		sb.append("\nautorefresh=");
		sb.append(autorefresh);
		sb.append("\nhaltOnError=");
		sb.append(haltOnError);
		sb.append("\nhaltOnFailure=");
		sb.append(haltOnFailure);
		sb.append("\nprintSummary=");
		sb.append(printSummary);
		sb.append("\nuserAgentLanguage=");
		sb.append(userAgentLanguage);
		sb.append("\nuserAgentName=");
		sb.append(userAgentName);
		sb.append("\ndtdTaggingTag=");
		sb.append(dtdTaggingTag);
		sb.append("\ndtdTaggingEnabled=");
		sb.append(dtdTaggingEnabled);
		sb.append("\ntransactionLocation=");
		sb.append(transactionLocation);
		sb.append("\ntransactionName=");
		sb.append(transactionName);
		sb.append("\nloglevel=");
		sb.append(loglevel);

		sb.append("\nconnectionTimeout=");
		sb.append(connectionTimeout);
		sb.append("\nscript=");
		sb.append(script);
		sb.append("\nhost=");
		sb.append(host);
		sb.append("\n");

		sb.append("_______________________\n");
		sb.append("WTMonitorConfig End\n");
		sb.append("-----------------------\n");

		return sb.toString();
	}
}

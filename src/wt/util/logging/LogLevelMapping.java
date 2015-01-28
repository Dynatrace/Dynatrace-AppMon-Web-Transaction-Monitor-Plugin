package wt.util.logging;

import java.util.logging.Level;

public class LogLevelMapping {
	/**
	 * log4j log levels are:
	 * 	OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL
	 * 
	 * javaUtil log levels are:
	 *  OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL 
	 * 
	 * @param level
	 * @return
	 */
	public static org.apache.log4j.Level getLog4JLogLevel(java.util.logging.Level level) {
		if(java.util.logging.Level.OFF.equals(level)) {
			return org.apache.log4j.Level.OFF;
		} else if (java.util.logging.Level.SEVERE.equals(level)){
			return org.apache.log4j.Level.FATAL;
		} else if (java.util.logging.Level.WARNING.equals(level)) {
			return org.apache.log4j.Level.WARN;
		} else if (java.util.logging.Level.INFO.equals(level)) {
			return org.apache.log4j.Level.INFO;
		} else if (java.util.logging.Level.CONFIG.equals(level)) {
			return org.apache.log4j.Level.INFO;
		} else if (java.util.logging.Level.FINE.equals(level)) {
			return org.apache.log4j.Level.DEBUG;
		} else if (java.util.logging.Level.FINER.equals(level)) {
			return org.apache.log4j.Level.DEBUG;
		} else if (java.util.logging.Level.FINEST.equals(level)) {
			return org.apache.log4j.Level.TRACE;
		} else if (java.util.logging.Level.ALL.equals(level)) {
			return org.apache.log4j.Level.ALL;
		} else {
			return org.apache.log4j.Level.INFO;
		}
	}
	
	public static java.util.logging.Level getJavaUtilLogLevel(org.apache.log4j.Level level) {
		if(org.apache.log4j.Level.OFF.equals(level)) {
			return java.util.logging.Level.OFF;
		} else if (org.apache.log4j.Level.FATAL.equals(level)){
			return java.util.logging.Level.SEVERE;
		} else if (org.apache.log4j.Level.WARN.equals(level)) {
			return java.util.logging.Level.WARNING;
		} else if (org.apache.log4j.Level.INFO.equals(level)) {
			return java.util.logging.Level.INFO;
		} else if (org.apache.log4j.Level.DEBUG.equals(level)) {
			return java.util.logging.Level.FINER;
		} else if (org.apache.log4j.Level.TRACE.equals(level)) {
			return java.util.logging.Level.FINEST;
		} else if (org.apache.log4j.Level.ALL.equals(level)) {
			return java.util.logging.Level.ALL;
		} else {
			return java.util.logging.Level.INFO;
		}
	}

	public static Level getJavaUtilLogLevel(String loglevel) {
		//OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL
		
		if("OFF".equals(loglevel)) {
			return java.util.logging.Level.OFF;
		} else if ("SEVERE".equals(loglevel)) {
			return java.util.logging.Level.SEVERE;
		} else if ("WARNING".equals(loglevel)) {
			return java.util.logging.Level.WARNING;
		} else if ("INFO".equals(loglevel)) {
			return java.util.logging.Level.INFO;
		} else if ("CONFIG".equals(loglevel)) {
			return java.util.logging.Level.CONFIG;
		} else if ("FINE".equals(loglevel)) {
			return java.util.logging.Level.FINE;
		} else if ("FINER".equals(loglevel)) {
			return java.util.logging.Level.FINER;
		} else if ("FINEST".equals(loglevel)) {
			return java.util.logging.Level.FINEST;
		} else if ("ALL".equals(loglevel)) {
			return java.util.logging.Level.ALL;
		} else {
			return java.util.logging.Level.INFO;
		}
	}
}

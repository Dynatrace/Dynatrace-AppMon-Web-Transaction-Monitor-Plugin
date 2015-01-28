package wt.util.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * An implementation of log4j {@link org.apache.log4j.Appender} which 
 * redirects its logging messages to {@link java.util.Logger}.
 * 
 * @author markus.poechtrager
 */
public final class RedirectAppender extends AppenderSkeleton {

	private Logger log;

	private Map<String, Level> cache;
	
	/**
	 * Creates a new {@link org.apache.log4j.Appender} instance 
	 * which logs its messages into a {@link java.util.Logger}.
	 * 
	 * @param log a java.util.Logger to log into
	 */
	public RedirectAppender(Logger log) {
		if (log == null)
			throw new IllegalArgumentException("'log' is null."); //$NON-NLS-1$
		this.log = log;
		this.cache = new HashMap<String, Level>();
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	public void close() {}

	@Override
	protected void append(LoggingEvent arg0) {
		org.apache.log4j.Level level = arg0.getLevel();		
		Level lvl = convertLevel(level);
	
		LogRecord record = new LogRecord(lvl, arg0.getRenderedMessage()); 
		record.setLoggerName(arg0.getLoggerName());
		record.setMillis(arg0.timeStamp);

		if (arg0.getThrowableInformation() != null) {					
			record.setThrown(arg0.getThrowableInformation().getThrowable());
		}	
		
		log.log(record);
	}
	
	
	private static class CustomLevel extends Level {

		private static final long serialVersionUID = 1L;

		public CustomLevel(String name, int value) {
			super(name, value);
		}	
	}
	
	
	private Level convertLevel(org.apache.log4j.Level log4jLevel) {
		Level javaUtilLogLevel = LogLevelMapping.getJavaUtilLogLevel(log4jLevel);
		final String levelString = javaUtilLogLevel.toString();
		Level cachedJavaLevel = cache.get(levelString);	
		if (cachedJavaLevel == null) {
			cachedJavaLevel = new CustomLevel(levelString, javaUtilLogLevel.intValue());
			cache.put(levelString, cachedJavaLevel);
		}
		return cachedJavaLevel;
	}

}
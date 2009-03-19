package org.eclipse.e4.core.services;

/**
 * Logging warnings, errors, information, as well as capturing debug and trace
 * information. Everything done through this interface is not meant for normal
 * end users. Strings are not expected to be translated.
 * 
 * @see IStatusHandler
 */
public interface ILogger {
	public boolean isErrorEnabled();

	public void error(Throwable t);

	public void error(Throwable t, String message);

	public void error(String message);

	public void error(String format, Object arg);

	public void error(String format, Object arg1, Object arg2);

	public void error(String format, Object[] args);

	public boolean isWarnEnabled();

	public void warn(Throwable t);

	public void warn(Throwable t, String message);

	public void warn(String message);

	public void warn(String format, Object arg);

	public void warn(String format, Object arg1, Object arg2);

	public void warn(String format, Object[] args);

	public boolean isInfoEnabled();

	public void info(Throwable t);

	public void info(Throwable t, String message);

	public void info(String message);

	public void info(String format, Object arg);

	public void info(String format, Object arg1, Object arg2);

	public void info(String format, Object[] args);

	public boolean isTraceEnabled();

	public void trace(Throwable t);

	public void trace(Throwable t, String message);

	public void trace(String message);

	public void trace(String format, Object arg);

	public void trace(String format, Object arg1, Object arg2);

	public void trace(String format, Object[] args);

	public boolean isDebugEnabled();

	public void debug(Throwable t);

	public void debug(Throwable t, String message);

	public void debug(String message);

	public void debug(String format, Object arg);

	public void debug(String format, Object arg1, Object arg2);

	public void debug(String format, Object[] args);
}

package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.services.ILogger;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.eclipse.osgi.util.NLS;

/**
 *
 */
final class WorkbenchLogger implements ILogger {
	private DebugTrace trace;
	private ILog log;

	public void debug(Throwable t) {
		debug(t, null);
	}

	public void debug(Throwable t, String message) {
		getTrace().trace(null, message, t);
	}

	private synchronized DebugTrace getTrace() {
		if (trace != null) {
			trace = Activator.getDefault().getDebugOptions().newDebugTrace(
					Activator.getDefault().getBundle().getSymbolicName());
		}
		return trace;
	}

	public void debug(String message) {
		debug((Throwable) null, message);
	}

	public void debug(String format, Object arg) {
		debug(NLS.bind(format, arg));
	}

	public void debug(String format, Object arg1, Object arg2) {
		debug(NLS.bind(format, arg1, arg2));
	}

	public void debug(String format, Object[] args) {
		debug(NLS.bind(format, args));
	}

	public void error(Throwable t) {
		error(t, null);
	}

	public void error(Throwable t, String message) {
		getLog().log(
				new Status(IStatus.ERROR, Activator.getDefault().getBundle()
						.getSymbolicName(), message, t));
	}

	/**
	 * @return
	 * 
	 */
	private synchronized ILog getLog() {
		if (log == null) {
			log = Platform.getLog(Activator.getDefault().getBundle());
		}
		return log;
	}

	public void error(String message) {
		error((Throwable) null, message);
	}

	public void error(String format, Object arg) {
		error(NLS.bind(format, arg));
	}

	public void error(String format, Object arg1, Object arg2) {
		error(NLS.bind(format, arg1, arg2));
	}

	public void error(String format, Object[] args) {
		error(NLS.bind(format, args));
	}

	public void info(Throwable t) {
		info(t, null);
	}

	public void info(Throwable t, String message) {
		getLog().log(
				new Status(IStatus.INFO, Activator.getDefault().getBundle()
						.getSymbolicName(), message, t));
	}

	public void info(String message) {
		info((Throwable) null, message);
	}

	public void info(String format, Object arg) {
		error(NLS.bind(format, arg));
	}

	public void info(String format, Object arg1, Object arg2) {
		error(NLS.bind(format, arg1, arg2));
	}

	public void info(String format, Object[] args) {
		error(NLS.bind(format, args));
	}

	public boolean isDebugEnabled() {
		return false;
	}

	public boolean isErrorEnabled() {
		return true;
	}

	public boolean isInfoEnabled() {
		return true;
	}

	public boolean isTraceEnabled() {
		return false;
	}

	public boolean isWarnEnabled() {
		return true;
	}

	public void trace(Throwable t) {
		trace(t, null);
	}

	public void trace(Throwable t, String message) {
		getTrace().trace(null, message, t);
	}

	public void trace(String message) {
		trace((Throwable) null, message);
	}

	public void trace(String format, Object arg) {
		trace(NLS.bind(format, arg));
	}

	public void trace(String format, Object arg1, Object arg2) {
		trace(NLS.bind(format, arg1, arg2));
	}

	public void trace(String format, Object[] args) {
		trace(NLS.bind(format, args));
	}

	public void warn(Throwable t) {
		warn(t, null);
	}

	public void warn(Throwable t, String message) {
		getLog().log(
				new Status(IStatus.WARNING, Activator.getDefault().getBundle()
						.getSymbolicName(), message, t));
	}

	public void warn(String message) {
		warn((Throwable) null, message);
	}

	public void warn(String format, Object arg) {
		warn(NLS.bind(format, arg));
	}

	public void warn(String format, Object arg1, Object arg2) {
		warn(NLS.bind(format, arg1, arg2));
	}

	public void warn(String format, Object[] args) {
		warn(NLS.bind(format, args));
	}
}
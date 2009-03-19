package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.osgi.service.debug.DebugTrace;

/**
 *
 */
public final class WorkbenchLogger extends Logger {
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

	public void info(Throwable t, String message) {
		getLog().log(
				new Status(IStatus.INFO, Activator.getDefault().getBundle()
						.getSymbolicName(), message, t));
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

	public void trace(Throwable t, String message) {
		getTrace().trace(null, message, t);
	}

	public void warn(Throwable t, String message) {
		getLog().log(
				new Status(IStatus.WARNING, Activator.getDefault().getBundle()
						.getSymbolicName(), message, t));
	}
}
package org.eclipse.e4.tools.compat.internal;

import java.util.ArrayList;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * The workbench implementation of the logger service.
 */
@SuppressWarnings("restriction")
public final class WorkbenchLogger extends Logger {
	protected DebugTrace trace;
	protected FrameworkLog log;
	private final Bundle bundle = FrameworkUtil.getBundle(WorkbenchLogger.class);

	/**
	 * Creates a new workbench logger
	 */
	public WorkbenchLogger() {
		super();
	}

	@Override
	public void debug(Throwable t) {
		debug(t, null);
	}

	@Override
	public void debug(Throwable t, String message) {
		trace(t, message);
	}

	@Override
	public void error(Throwable t, String message) {
		log(new Status(IStatus.ERROR, bundle.getSymbolicName(),
			message, t));
	}

	/**
	 * Copied from PlatformLogWriter in core runtime.
	 */
	private static FrameworkLogEntry getLog(IStatus status) {
		final Throwable t = status.getException();
		final ArrayList<FrameworkLogEntry> childlist = new ArrayList<FrameworkLogEntry>();

		final int stackCode = t instanceof CoreException ? 1 : 0;
		// ensure a substatus inside a CoreException is properly logged
		if (stackCode == 1) {
			final IStatus coreStatus = ((CoreException) t).getStatus();
			if (coreStatus != null) {
				childlist.add(getLog(coreStatus));
			}
		}

		if (status.isMultiStatus()) {
			final IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				childlist.add(getLog(children[i]));
			}
		}

		final FrameworkLogEntry[] children = childlist.size() == 0 ? null
			: childlist.toArray(new FrameworkLogEntry[childlist.size()]);

		return new FrameworkLogEntry(status.getPlugin(), status.getSeverity(), status.getCode(),
			status.getMessage(), stackCode, t, children);
	}

	@Override
	public void info(Throwable t, String message) {
		log(new Status(IStatus.INFO, bundle.getSymbolicName(), message,
			t));
	}

	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public boolean isErrorEnabled() {
		return true;
	}

	@Override
	public boolean isInfoEnabled() {
		return true;
	}

	@Override
	public boolean isTraceEnabled() {
		return false;
	}

	@Override
	public boolean isWarnEnabled() {
		return true;
	}

	private void log(IStatus status) {
		if (log != null) {
			log.log(getLog(status));
		} else {
			System.out.println(status.getMessage());
			if (status.getException() != null) {
				status.getException().printStackTrace();
			}
		}
	}

	/**
	 * Sets the debug options service for this logger.
	 *
	 * @param options
	 *            The debug options to be used by this logger
	 */
	@Inject
	public void setDebugOptions(DebugOptions options) {
		if (options != null) {
			trace = options.newDebugTrace(bundle.getSymbolicName(), WorkbenchLogger.class);
		}
	}

	/**
	 * @param log
	 */
	@Inject
	public void setFrameworkLog(FrameworkLog log) {
		this.log = log;
	}

	@Override
	public void trace(Throwable t, String message) {
		if (trace != null) {
			trace.trace(null, message, t);
		} else {
			System.out.println(message);
			t.printStackTrace();
		}
	}

	@Override
	public void warn(Throwable t, String message) {
		log(new Status(IStatus.WARNING, bundle.getSymbolicName(),
			message, t));
	}
}
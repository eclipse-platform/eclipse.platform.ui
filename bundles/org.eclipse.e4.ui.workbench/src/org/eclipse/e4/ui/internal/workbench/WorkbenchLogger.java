/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugTrace;

/**
 * The workbench implementation of the logger service.
 */
public final class WorkbenchLogger extends Logger {
	protected DebugTrace trace;
	protected FrameworkLog log;
	private String bundleName;

	/**
	 * Creates a new workbench logger
	 */
	@Inject
	public WorkbenchLogger(@Optional @Named("logger.bundlename") String bundleName) {
		super();
		this.bundleName = bundleName == null ? Activator.PI_WORKBENCH : bundleName;
	}

	@Override
	public void debug(Throwable t) {
		debug(t, null);
	}

	@Override
	public void debug(Throwable t, String message) {
		if (!isDebugEnabled()) {
			return;
		}
		trace(Policy.DEBUG_FLAG, t, message);
	}

	@Override
	public void error(Throwable t, String message) {
		log(new Status(IStatus.ERROR, bundleName, message, t));
	}

	/**
	 * Copied from PlatformLogWriter in core runtime.
	 */
	private static FrameworkLogEntry getLog(IStatus status) {
		Throwable t = status.getException();
		ArrayList<FrameworkLogEntry> childlist = new ArrayList<>();

		int stackCode = t instanceof CoreException ? 1 : 0;
		// ensure a substatus inside a CoreException is properly logged
		if (stackCode == 1) {
			IStatus coreStatus = ((CoreException) t).getStatus();
			if (coreStatus != null) {
				childlist.add(getLog(coreStatus));
			}
		}

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (IStatus element : children) {
				childlist.add(getLog(element));
			}
		}

		FrameworkLogEntry[] children = childlist.isEmpty() ? null
				: childlist.toArray(new FrameworkLogEntry[childlist.size()]);

		return new FrameworkLogEntry(status.getPlugin(), status.getSeverity(), status.getCode(),
				status.getMessage(), stackCode, t, children);
	}

	@Override
	public void info(Throwable t, String message) {
		log(new Status(IStatus.INFO, bundleName, message, t));
	}

	@Override
	public boolean isDebugEnabled() {
		return Policy.DEBUG;
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
		return Policy.TRACE;
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
			if (status.getException() != null)
				status.getException().printStackTrace();
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
			this.trace = options.newDebugTrace(bundleName, WorkbenchLogger.class);
		}
	}

	@Inject
	public void setFrameworkLog(FrameworkLog log) {
		this.log = log;
	}

	@Override
	public void trace(Throwable t, String message) {
		trace(Policy.TRACE_FLAG, t, message);
	}

	private void trace(String flag, Throwable t, String message) {
		if (trace != null) {
			trace.trace(flag, message, t);
		} else {
			System.out.println(message);
			if (t != null)
				t.printStackTrace();
		}
	}

	@Override
	public void warn(Throwable t, String message) {
		log(new Status(IStatus.WARNING, bundleName, message, t));
	}
}
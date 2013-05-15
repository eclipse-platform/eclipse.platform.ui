/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Named;
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

	public void debug(Throwable t) {
		debug(t, null);
	}

	public void debug(Throwable t, String message) {
		trace(t, message);
	}

	public void error(Throwable t, String message) {
		log(new Status(IStatus.ERROR, bundleName, message, t));
	}

	/**
	 * Copied from PlatformLogWriter in core runtime.
	 */
	private static FrameworkLogEntry getLog(IStatus status) {
		Throwable t = status.getException();
		ArrayList childlist = new ArrayList();

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
			for (int i = 0; i < children.length; i++) {
				childlist.add(getLog(children[i]));
			}
		}

		FrameworkLogEntry[] children = (FrameworkLogEntry[]) (childlist.size() == 0 ? null
				: childlist.toArray(new FrameworkLogEntry[childlist.size()]));

		return new FrameworkLogEntry(status.getPlugin(), status.getSeverity(), status.getCode(),
				status.getMessage(), stackCode, t, children);
	}

	public void info(Throwable t, String message) {
		log(new Status(IStatus.INFO, bundleName, message, t));
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

	/**
	 * @param log
	 */
	@Inject
	public void setFrameworkLog(FrameworkLog log) {
		this.log = log;
	}

	public void trace(Throwable t, String message) {
		if (trace != null) {
			trace.trace(null, message, t);
		} else {
			System.out.println(message);
			if (t != null)
				t.printStackTrace();
		}
	}

	public void warn(Throwable t, String message) {
		log(new Status(IStatus.WARNING, bundleName, message, t));
	}
}
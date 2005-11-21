/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

/**
 * 
 */
public class Log implements ILog {
	Bundle bundle;
	Set logListeners = new HashSet(5);

	public Log(Bundle plugin) {
		this.bundle = plugin;
	}

	/**
	 * Adds the given log listener to this log.  Subsequently the log listener will
	 * receive notification of all log events passing through this log.
	 *
	 * @see Platform#addLogListener(ILogListener)
	 */
	public void addLogListener(ILogListener listener) {
		synchronized (logListeners) {
			logListeners.add(listener);
		}
	}

	/**
	 * Returns the plug-in with which this log is associated.
	 */
	public Bundle getBundle() {
		return bundle;
	}

	/**
	 * Logs the given status.  The status is distributed to the log listeners
	 * installed on this log and then to the log listeners installed on the platform.
	 *
	 * @see Plugin#getLog()
	 */
	public void log(final IStatus status) {
		// Log to the platform log first in case a listener throws an error.
		InternalPlatform.getDefault().log(status);
		// create array to avoid concurrent access
		ILogListener[] listeners;
		synchronized (logListeners) {
			listeners = (ILogListener[]) logListeners.toArray(new ILogListener[logListeners.size()]);
		}
		for (int i = 0; i < listeners.length; i++) {
			final ILogListener listener = listeners[i];
			ISafeRunnable code = new ISafeRunnable() {
				public void run() throws Exception {
					listener.logging(status, bundle.getSymbolicName());
				}

				public void handleException(Throwable e) {
					//Ignore
				}
			};
			SafeRunner.run(code);
		}
	}

	/**
	 * Removes the given log listener to this log.  Subsequently the log listener will
	 * no longer receive notification of log events passing through this log.
	 *
	 * @see Platform#removeLogListener(ILogListener)
	 */
	public void removeLogListener(ILogListener listener) {
		synchronized (logListeners) {
			logListeners.remove(listener);
		}
	}
}

package org.eclipse.core.internal.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import java.util.*;

/**
 * 
 */
public class Log implements ILog{
	Plugin plugin;
	Set logListeners = new HashSet(5);
Log(Plugin plugin) {
	this.plugin = plugin;
}
/**
 * Adds the given log listener to this log.  Subsequently the log listener will
 * receive notification of all log events passing through this log.
 *
 * @see Platform#addLogListener
 */
public void addLogListener(ILogListener listener) {
	synchronized (logListeners) {
		logListeners.add(listener);
	}
}
/**
 * Returns the plug-in with which this log is associated.
 */
public Plugin getPlugin() {
	return plugin;
}
/**
 * Logs the given status.  The status is distributed to the log listeners
 * installed on this log and then to the log listeners installed on the platform.
 *
 * @see Plugin#getLogMask
 */
public void log(final IStatus status) {
	// create array to avoid concurrent access
	ILogListener[] listeners;
	synchronized (logListeners) {
		listeners = (ILogListener[]) logListeners.toArray(new ILogListener[logListeners.size()]);
	}
	for (int i = 0; i < listeners.length; i++) {
		final ILogListener listener = listeners[i];
		ISafeRunnable code = new ISafeRunnable() {
			public void run() throws Exception {
				listener.logging(status, plugin.getDescriptor().getUniqueIdentifier());
			}
			public void handleException(Throwable e) {
			}
		};
		InternalPlatform.run(code);
	}
	InternalPlatform.log(status);
}
/**
 * Removes the given log listener to this log.  Subsequently the log listener will
 * no longer receive notification of log events passing through this log.
 *
 * @see Platform#removeLogListener
 */
public void removeLogListener(ILogListener listener) {
	synchronized (logListeners) {
		logListeners.remove(listener);
	}
}
}

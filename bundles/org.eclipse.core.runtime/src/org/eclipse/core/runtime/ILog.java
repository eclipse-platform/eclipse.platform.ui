package org.eclipse.core.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A log to which status events can be written.  Logs appear on individual
 * plug-ins and on the platform itself.  Clients can register log listeners which
 * will receive notification of all log events as the come in.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface ILog {
/**
 * Adds the given log listener to this log.  Subsequently the log listener will
 * receive notification of all log events passing through this log.
 * This method has no affect if the identical listener is already registered on this log.
 *
 * @param listener the listener to add to this log
 * @see Platform#addLogListener
 */
public void addLogListener(ILogListener listener);
/**
 * Returns the plug-in with which this log is associated.
 *
 * @return the plug-in with which this log is associated
 */
public Plugin getPlugin();
/**
 * Logs the given status.  The status is distributed to the log listeners
 * installed on this log and then to the log listeners installed on the platform.
 *
 * @param status the status to log
 */
public void log(IStatus status);
/**
 * Removes the given log listener to this log.  Subsequently the log listener will
 * no longer receive notification of log events passing through this log.  
 * This method has no affect if the identical listener is not registered on this log.
 *
 * @see Platform#removeLogListener
 */
public void removeLogListener(ILogListener listener);
}

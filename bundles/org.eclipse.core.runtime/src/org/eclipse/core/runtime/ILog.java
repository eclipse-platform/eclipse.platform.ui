/**********************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.runtime;

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

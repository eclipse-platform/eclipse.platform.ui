package org.eclipse.core.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.EventListener;

/**
 * A log listener is notified of entries added to a plug-in's log.
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see ILog#addLogListener
 * @see Platform#addLogListener
 */
public interface ILogListener extends EventListener {
/**
 * Notifies this listener that given status has been logged by
 * a plug-in.  The listener is free to retain or ignore this status.
 * 
 * @param status the status being logged
 * @param plugin the plugin of the log which generated this event
 */
public void logging(IStatus status, String plugin);
}

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
package org.eclipse.core.runtime;

import java.util.EventListener;

/**
 * A log listener is notified of entries added to a plug-in's log.
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see ILog#addLogListener(ILogListener)
 * @see Platform#addLogListener(ILogListener)
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

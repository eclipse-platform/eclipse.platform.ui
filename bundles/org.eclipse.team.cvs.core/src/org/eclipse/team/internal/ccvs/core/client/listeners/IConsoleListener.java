/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client.listeners;

import org.eclipse.core.runtime.IStatus;

public interface IConsoleListener {
	/**
	 * Called when a command is invoked.
	 * @param line the command invocation string
	 */
	public void commandInvoked(String line);
	
	/**
	 * Called when a line of message text has been received.
	 * @param line the line of text
	 */
	public void messageLineReceived(String line);
	
	/**
	 * Called when a line of error text has been received.
	 * @param line the line of text
	 */
	public void errorLineReceived(String line);
	
	/**
	 * Called when a command has been completed.
	 * @param status the status code, or null if not applicable
	 * @param exception an exception, or null if not applicable
	 */
	public void commandCompleted(IStatus status, Exception exception);
}

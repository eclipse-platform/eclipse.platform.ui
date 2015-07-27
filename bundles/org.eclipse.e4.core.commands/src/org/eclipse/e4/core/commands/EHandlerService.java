/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.commands;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 * @noimplement
 */
public interface EHandlerService {

	public void activateHandler(String commandId, Object handler);

	public void deactivateHandler(String commandId, Object handler);

	public Object executeHandler(ParameterizedCommand command);

	public boolean canExecute(ParameterizedCommand command);

	/**
	 * Execute a handler for the command.
	 *
	 * @param command
	 *            Must not be <code>null</code>
	 * @param staticContext
	 *            Must not be <code>null</code>. You must dispose your context when you are done.
	 * @return the command return value.
	 */
	public Object executeHandler(ParameterizedCommand command, IEclipseContext staticContext);

	/**
	 * Check if a command can be executed.
	 *
	 * @param command
	 *            Must not be <code>null</code>.
	 * @param staticContext
	 *            Must not be <code>null</code>. You must dispose your context when you are done.
	 * @return true if the command can be executed.
	 */
	public boolean canExecute(ParameterizedCommand command, IEclipseContext staticContext);
}

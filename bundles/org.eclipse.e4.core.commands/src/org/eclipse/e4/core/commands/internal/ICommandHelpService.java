/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.commands.internal;

import org.eclipse.core.commands.IHandler;
import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 * Provides services related to the command context help.
 */
public interface ICommandHelpService {

	/**
	 * Calculates the active help context for the command and returns the ID of the help context.
	 *
	 * @param commandId
	 *            the ID of the command for which the help context ID is calculated
	 * @param context
	 *            the Eclipse context in which handlers of the command will be sought
	 * @return the ID of help context which is active for the command
	 */
	public String getHelpContextId(String commandId, IEclipseContext context);

	/**
	 * Assigns the help context ID to the command handler.
	 *
	 * @param handler
	 *            the command handler to which the help context ID will be assigned
	 * @param contextId
	 *            the help context ID to assign
	 */
	public void setHelpContextId(IHandler handler, String contextId);
}

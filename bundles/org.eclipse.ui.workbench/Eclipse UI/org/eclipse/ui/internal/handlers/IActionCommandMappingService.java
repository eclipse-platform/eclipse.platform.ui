/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

/**
 * <p>
 * A service which holds mappings between retarget action identifiers and
 * command identifiers (aka: action definition ids).
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public interface IActionCommandMappingService {

	/**
	 * Returns the command identifier corresponding to the given action
	 * identifier, if any.
	 * 
	 * @param actionId
	 *            The identifier of the retarget action for which the command
	 *            identifier should be retrieved; must not be <code>null</code>.
	 * @return The identifier of the corresponding command; <code>null</code>
	 *         if none.
	 */
	public String getCommandId(String actionId);

	/**
	 * Maps an action identifier to a command identifier. This is used for
	 * retarget action, so that global action handlers can be registered with
	 * the correct command.
	 * 
	 * @param actionId
	 *            The identifier of the retarget action; must not be
	 *            <code>null</code>.
	 * @param commandId
	 *            The identifier of the command; must not be <code>null</code>
	 */
	public void map(String actionId, String commandId);
	
	public String getGeneratedCommandId(String targetId, String actionId);
}


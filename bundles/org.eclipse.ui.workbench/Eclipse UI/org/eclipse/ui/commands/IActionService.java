/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.commands;

import org.eclipse.jface.action.IAction;

/**
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * @since 3.0
 */
public interface IActionService {
	
	/**
	 * Registers an action with a command.
	 * 
	 * @param action the action to be registered with a command.
	 */
	void registerAction(IAction action)
		throws IllegalArgumentException;
			
	/**
	 * Unregisters an action with a command.
	 * 
	 * @param action the action to be unregistered with a command.
	 */	
	void unregisterAction(IAction action)
		throws IllegalArgumentException;
}

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

import java.util.SortedMap;

/**
 * <p>
 * JAVADOC
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface IActionService {

	/**
	 * JAVADOC
	 *
	 * @param commandId
	 * @param action
	 * @throws NullPointerException
	 */	
	void addAction(String commandId, IAction action);

	/**
	 * Registers an IActionServiceListener instance with this action service.
	 *
	 * @param actionServiceListener the IActionServiceListener instance to register.
	 * @throws NullPointerException
	 */	
	void addActionServiceListener(IActionServiceListener actionServiceListener);
		
	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedMap getActionsById();

	/**
	 * JAVADOC
	 *
	 * @param commandId
	 * @throws NullPointerException
	 */	
	void removeAction(String commandId);
	
	/**
	 * Unregisters an IActionServiceListener instance with this action service.
	 *
	 * @param actionServiceListener the IActionServiceListener instance to unregister.
	 * @throws NullPointerException
	 */
	void removeActionServiceListener(IActionServiceListener actionServiceListener);
}
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

import java.util.SortedSet;

/**
 * <p>
 * TODO javadoc
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
public interface ICommandActivationService {

	/**
	 * TODO javadoc
	 *
	 * @param commandId
	 * @throws IllegalArgumentException
	 */	
	void activateCommand(String commandId)
		throws IllegalArgumentException;

	/**
	 * Registers an ICommandActivationServiceListener instance with this command activation service.
	 *
	 * @param commandActivationServiceListener the ICommandActivationServiceListener instance to register.
	 * @throws IllegalArgumentException
	 */	
	void addCommandActivationServiceListener(ICommandActivationServiceListener commandActivationServiceListener)
		throws IllegalArgumentException;

	/**
	 * TODO javadoc
	 *
	 * @param commandId
	 * @throws IllegalArgumentException
	 */	
	void deactivateCommand(String commandId)
		throws IllegalArgumentException;
		
	/**
	 * TODO javadoc
	 *
	 * @return
	 */
	SortedSet getActiveCommandIds();
	
	/**
	 * Unregisters an ICommandActivationServiceListener instance with this command activation services.
	 *
	 * @param commandActivationServiceListener the ICommandActivationServiceListener instance to unregister.
	 * @throws IllegalArgumentException
	 */
	void removeCommandActivationServiceListener(ICommandActivationServiceListener commandActivationServiceListener)
		throws IllegalArgumentException;
}

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

import org.eclipse.ui.commands.old.ICommandHandler;

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
public interface ICommandHandlerService {

	/**
	 * TODO javadoc
	 *
	 * @param commandId
	 * @param commandHandler
	 * @throws IllegalArgumentException
	 */	
	void addCommandHandler(String commandId, ICommandHandler commandHandler)
		throws IllegalArgumentException;

	/**
	 * Registers an ICommandHandlerServiceListener instance with this command handler service.
	 *
	 * @param commandHandlerServiceListener the ICommandHandlerServiceListener instance to register.
	 * @throws IllegalArgumentException
	 */	
	void addCommandHandlerServiceListener(ICommandHandlerServiceListener commandHandlerServiceListener)
		throws IllegalArgumentException;
		
	/**
	 * TODO javadoc
	 *
	 * @return
	 */
	SortedMap getCommandHandlersById();

	/**
	 * TODO javadoc
	 *
	 * @param commandId
	 * @throws IllegalArgumentException
	 */	
	void removeCommandHandler(String commandId)
		throws IllegalArgumentException;
	
	/**
	 * Unregisters an ICommandHandlerServiceListener instance with this command handler services.
	 *
	 * @param commandHandlerServiceListener the ICommandHandlerServiceListener instance to unregister.
	 * @throws IllegalArgumentException
	 */
	void removeCommandHandlerServiceListener(ICommandHandlerServiceListener commandHandlerServiceListener)
		throws IllegalArgumentException;
}

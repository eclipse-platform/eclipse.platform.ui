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
import java.util.SortedSet;

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
public interface ICommandManager {

	/**
	 * Registers an ICommandManagerListener instance with this command manager.
	 *
	 * @param commandManagerListener the ICommandManagerListener instance to register.
	 * @throws IllegalArgumentException
	 */	
	void addCommandManagerListener(ICommandManagerListener commandManagerListener)
		throws IllegalArgumentException;

	/**
	 * JAVADOC
	 *
	 * @param commandId
	 * @return
	 * @throws IllegalArgumentException
	 */	
	ICommand getCommand(String commandId)
		throws IllegalArgumentException;

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedMap getCommandHandlersById();

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedSet getDefinedCommandIds();
	
	/**
	 * Unregisters an ICommandManagerListener instance with this command manager.
	 *
	 * @param commandManagerListener the ICommandManagerListener instance to unregister.
	 * @throws IllegalArgumentException
	 */
	void removeCommandManagerListener(ICommandManagerListener commandManagerListener)
		throws IllegalArgumentException;
}

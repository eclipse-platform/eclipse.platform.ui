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
public interface ICommand {

	/**
	 * Registers an ICommandListener instance with this command.
	 *
	 * @param commandListener the ICommandListener instance to register.
	 * @throws IllegalArgumentException
	 */	
	void addCommandListener(ICommandListener commandListener)
		throws IllegalArgumentException;

	/**
	 * JAVADOC
	 * 
	 * @return
	 * @throws NotDefinedException
	 */	
	String getDescription()
		throws NotDefinedException;

	/**
	 * JAVADOC
	 * 
	 * @return
	 * @throws NotHandledException
	 */	
	ICommandHandler getCommandHandler()
		throws NotHandledException;

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	SortedSet getContextBindings();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	SortedSet getGestureBindings();
		
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getId();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	SortedSet getImageBindings();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	SortedSet getKeyBindings();
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 * @throws NotDefinedException
	 */	
	String getName()
		throws NotDefinedException;	

	/**
	 * JAVADOC
	 * 
	 * @return
	 * @throws NotDefinedException
	 */	
	String getParentId()
		throws NotDefinedException;
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 * @throws NotDefinedException
	 */	
	String getPluginId()
		throws NotDefinedException;

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	boolean isDefined();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	boolean isHandled();
	
	/**
	 * Unregisters an ICommandListener instance with this command.
	 *
	 * @param commandListener the ICommandListener instance to unregister.
	 * @throws IllegalArgumentException
	 */
	void removeCommandListener(ICommandListener commandListener)
		throws IllegalArgumentException;
}

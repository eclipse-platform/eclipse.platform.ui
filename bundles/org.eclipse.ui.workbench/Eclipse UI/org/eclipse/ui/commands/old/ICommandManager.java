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

package org.eclipse.ui.commands.old;

import java.util.SortedSet;

import org.eclipse.ui.commands.ICommandManagerListener;

/**
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface ICommandManager {

	/**
	 * Registers an IManagerListener instance with this manager.
	 *
	 * @param managerListener the IManagerListener instance to register.
	 */	
	void addManagerListener(ICommandManagerListener managerListener);

	/**
	 * TODO javadoc
	 *
	 * @param id
	 * @return
	 */	
	ICommand getCommand(String id);

	/**
	 * TODO javadoc
	 *
	 * @param id
	 * @return
	 */
	SortedSet getCommandIds();
	
	/**
	 * Unregisters an IManagerListener instance with this manager.
	 *
	 * @param managerListener the IManagerListener instance to unregister.
	 */
	void removeManagerListener(ICommandManagerListener managerListener);
}

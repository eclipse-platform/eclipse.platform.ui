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

import org.eclipse.ui.handles.IHandle;

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
	 * @throws NullPointerException
	 */	
	void addCommandManagerListener(ICommandManagerListener commandManagerListener);

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedSet getActiveCommandIds();

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedSet getActiveContextIds();

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	String getActiveKeyConfigurationId();

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	String getActiveLocale();
	
	/**
	 * JAVADOC
	 *
	 * @return
	 */
	String getActivePlatform();

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedMap getCategoriesById();
	
	/**
	 * JAVADOC
	 *
	 * @param categoryId
	 * @return
	 * @throws NullPointerException
	 */	
	IHandle getCategoryHandle(String categoryId);

	/**
	 * JAVADOC
	 *
	 * @param commandId
	 * @return
	 * @throws NullPointerException
	 */	
	IHandle getCommandHandle(String commandId);

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedMap getCommandsById();

	/**
	 * JAVADOC
	 *
	 * @param keyConfigurationId
	 * @return
	 * @throws NullPointerException
	 */	
	IHandle getKeyConfigurationHandle(String keyConfigurationId);

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedMap getKeyConfigurationsById();
	
	/**
	 * JAVADOC
	 *
	 * @return
	 */
	ICommandRegistry getPluginCommandRegistry();
	
	/**
	 * JAVADOC
	 *
	 * @return
	 */
	ICommandRegistry getPreferenceCommandRegistry();		
		
	/**
	 * Unregisters an ICommandManagerListener instance with this command manager.
	 *
	 * @param commandManagerListener the ICommandManagerListener instance to unregister.
	 * @throws NullPointerException
	 */
	void removeCommandManagerListener(ICommandManagerListener commandManagerListener);
}

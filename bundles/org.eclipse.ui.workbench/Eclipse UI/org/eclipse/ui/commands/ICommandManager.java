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
	 * @throws NullPointerException
	 */	
	void addCommandManagerListener(ICommandManagerListener commandManagerListener);

	/**
	 * JAVADOC
	 *
	 * @param categoryId
	 * @return
	 * @throws NullPointerException
	 */	
	ICategoryHandle getCategoryHandle(String categoryId);

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedMap getCommandDelegatesById();

	/**
	 * JAVADOC
	 *
	 * @param commandId
	 * @return
	 * @throws NullPointerException
	 */	
	ICommandHandle getCommandHandle(String commandId);

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedSet getDefinedCategoryIds();

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedSet getDefinedCommandIds();
	
	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedSet getDefinedGestureConfigurationIds();	

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedSet getDefinedKeyConfigurationIds();	

	/**
	 * JAVADOC
	 *
	 * @param gestureConfigurationId
	 * @return
	 * @throws NullPointerException
	 */	
	IGestureConfigurationHandle getGestureConfigurationHandle(String gestureConfigurationId);

	/**
	 * JAVADOC
	 *
	 * @param keyConfigurationId
	 * @return
	 * @throws NullPointerException
	 */	
	IKeyConfigurationHandle getKeyConfigurationHandle(String keyConfigurationId);
	
	/**
	 * Unregisters an ICommandManagerListener instance with this command manager.
	 *
	 * @param commandManagerListener the ICommandManagerListener instance to unregister.
	 * @throws NullPointerException
	 */
	void removeCommandManagerListener(ICommandManagerListener commandManagerListener);
}

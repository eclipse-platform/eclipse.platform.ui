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
public interface ICommandRegistry {

	/**
	 * Registers an ICommandRegistryListener instance with this command registry.
	 *
	 * @param commandRegistryListener the ICommandRegistryListener instance to register.
	 * @throws NullPointerException
	 */	
	void addCommandRegistryListener(ICommandRegistryListener commandRegistryListener);

	/**
	 * JAVADOC
	 *
	 * @param categoryDefinitionId
	 * @return
	 * @throws NullPointerException
	 */	
	ICategoryDefinitionHandle getCategoryDefinitionHandle(String categoryDefinitionId);

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedMap getCategoryDefinitionsById();

	/**
	 * JAVADOC
	 *
	 * @param commandDefinitionId
	 * @return
	 * @throws NullPointerException
	 */	
	ICommandDefinitionHandle getCommandDefinitionHandle(String commandDefinitionId);
	
	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedMap getCommandDefinitionsById();	

	/**
	 * JAVADOC
	 *
	 * @param keyConfigurationDefinitionId
	 * @return
	 * @throws NullPointerException
	 */	
	IKeyConfigurationDefinitionHandle getKeyConfigurationDefinitionHandle(String keyConfigurationDefinitionId);
	
	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedMap getKeyConfigurationDefinitionsById();	
		
	/**
	 * Unregisters an ICommandRegistryListener instance with this command registry.
	 *
	 * @param commandRegistryListener the ICommandRegistryListener instance to unregister.
	 * @throws NullPointerException
	 */
	void removeCommandRegistryListener(ICommandRegistryListener commandRegistryListener);
}

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

	/*
	ICommandManager
		
		get/setActiveContextIds();
		get/setActiveCommandIds();		
		get/setActiveKeyConfigurationId();
		get/setLocale();
		get/setPlatform();
		
		ICommandHandle getCommandHandle(String commandId);
		SortedMap getIdToCommandMap();

	
	ICommand

		getCommandDefinition();

		getContextBindings(); 
			// IContextBinding 1:1 with IContextBindingDefinition
		
		getImageBindings();
			// IImageBinding for those IImageBindingDefinitions matching platform and locale
			// just image style, image uri, match value (for ordering)		
		
		getKeyBindings(); 
			// IKeyBinding for those IKeyBindingDefinitions matching active context, active command, active key configuration, platform, and locale
			// just key sequence and match value (for ordering)

		isActive();		
		isContext();

	
	ICommandRegistry
		
		SortedSet getContextBindingDefinitions();
		SortedSet getImageBindingsDefinitions();
		SortedSet getKeyBindingDefinitions();

	
	IContextManager

		get/setActiveContextIds();
		
		IContextHandle getContextHandle(String contextId);
		SortedMap getIdToContextMap();
		
	
	IContext
	
		IContextDefinition getContextDefinition();
		
		isActive();		

	*/			
	/*
		// also solved, directly taken from above (except perhaps 'List getKeySequences', which needs to encapsulate matching order..)
		List getKeySequences();
		Map getImageUrisByStyle();
		SortedSet getContextIds();
		
		boolean inContext(); ids of getContextBindings() in activeContextIds?		
	*/	

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
	SortedMap getCategoriesById();
	
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
	SortedMap getCommandsById();	

	/**
	 * JAVADOC
	 *
	 * @param keyConfigurationId
	 * @return
	 * @throws NullPointerException
	 */	
	IKeyConfigurationHandle getKeyConfigurationHandle(String keyConfigurationId);
	
	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedMap getKeyConfigurationsById();	
		
	/**
	 * Unregisters an ICommandManagerListener instance with this command manager.
	 *
	 * @param commandManagerListener the ICommandManagerListener instance to unregister.
	 * @throws NullPointerException
	 */
	void removeCommandManagerListener(ICommandManagerListener commandManagerListener);
}

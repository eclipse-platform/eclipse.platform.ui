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

		// also solved, directly taken from above (except perhaps 'List getKeySequences', which needs to encapsulate matching order..)
		List getKeySequences();
		Map getImageUrisByStyle();
		SortedSet getContextIds();
	
	ICommandRegistry
		
		SortedSet getContextBindingDefinitions();
		SortedSet getImageBindingsDefinitions();
		SortedSet getKeyBindingDefinitions();
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
	ICommandRegistry getCommandRegistry();

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedMap getCommandsById();
		
	/**
	 * Unregisters an ICommandManagerListener instance with this command manager.
	 *
	 * @param commandManagerListener the ICommandManagerListener instance to unregister.
	 * @throws NullPointerException
	 */
	void removeCommandManagerListener(ICommandManagerListener commandManagerListener);
}

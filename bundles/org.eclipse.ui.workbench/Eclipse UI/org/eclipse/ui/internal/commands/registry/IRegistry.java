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

package org.eclipse.ui.internal.commands.registry;

import java.io.IOException;
import java.util.List;

/**
 * TODO javadoc
 * 
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface IRegistry {

	/**
	 * TODO javadoc
	 * 
	 * @param registryListener
	 */	
	void addRegistryListener(IRegistryListener registryListener);
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	List getActiveGestureConfigurations();

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	List getActiveKeyConfigurations();

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */
	List getCategories();
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	List getCommands();

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	List getContextBindings();

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	List getContexts();

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	List getGestureBindings();

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	List getGestureConfigurations();
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	List getKeyBindings();
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	List getKeyConfigurations();
	
	/**
	 * TODO javadoc
	 * 
	 * @throws IOException
	 */	
	void load()
		throws IOException;
	
	/**
	 * TODO javadoc
	 * 
	 * @param registryListener
	 */	
	void removeRegistryListener(IRegistryListener registryListener);
}	

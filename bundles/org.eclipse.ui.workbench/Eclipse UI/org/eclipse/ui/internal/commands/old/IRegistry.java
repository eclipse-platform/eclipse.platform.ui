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

package org.eclipse.ui.internal.commands.old;

import java.io.IOException;
import java.util.List;

/**
 * JAVADOC
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
	 * JAVADOC
	 * 
	 * @param registryListener
	 */	
	void addRegistryListener(IRegistryListener registryListener);
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	List getActiveGestureConfigurations();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	List getActiveKeyConfigurations();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */
	List getCategories();
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	List getCommands();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	List getContextBindings();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	List getContexts();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	List getGestureBindings();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	List getGestureConfigurations();
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	List getKeyBindings();
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	List getKeyConfigurations();
	
	/**
	 * JAVADOC
	 * 
	 * @throws IOException
	 */	
	void load()
		throws IOException;
	
	/**
	 * JAVADOC
	 * 
	 * @param registryListener
	 */	
	void removeRegistryListener(IRegistryListener registryListener);
}	

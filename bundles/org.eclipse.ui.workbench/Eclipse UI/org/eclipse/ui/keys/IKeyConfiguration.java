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

package org.eclipse.ui.keys;

import org.eclipse.ui.handles.NotDefinedException;

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
public interface IKeyConfiguration {

	/**
	 * Registers an IKeyConfigurationListener instance with this keyConfiguration.
	 *
	 * @param keyConfigurationListener the IKeyConfigurationListener instance to register.
	 * @throws IllegalArgumentException
	 */	
	void addKeyConfigurationListener(IKeyConfigurationListener keyConfigurationListener)
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
	 */	
	String getId();
	
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
	boolean isActive();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	boolean isDefined();
	
	/**
	 * Unregisters an IKeyConfigurationListener instance with this keyConfiguration.
	 *
	 * @param keyConfigurationListener the IKeyConfigurationListener instance to unregister.
	 * @throws IllegalArgumentException
	 */
	void removeKeyConfigurationListener(IKeyConfigurationListener keyConfigurationListener)
		throws IllegalArgumentException;
}

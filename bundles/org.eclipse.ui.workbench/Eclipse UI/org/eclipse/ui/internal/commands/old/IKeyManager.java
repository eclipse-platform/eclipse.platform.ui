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

import java.util.SortedSet;

import org.eclipse.ui.commands.IKeyConfiguration;

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
public interface IKeyManager {

	/**
	 * Registers an IKeyManagerListener instance with this key manager.
	 *
	 * @param keyManagerListener the IKeyManagerListener instance to register.
	 * @throws IllegalArgumentException
	 */	
	//void addKeyManagerListener(IKeyManagerListener keyManagerListener)
	//	throws IllegalArgumentException;

	/**
	 * JAVADOC
	 *
	 * @param keyConfigurationId
	 * @return
	 * @throws IllegalArgumentException
	 */	
	IKeyConfiguration getKeyConfiguration(String keyConfigurationId)
		throws IllegalArgumentException;

	/**
	 * Unregisters an IKeyManagerListener instance with this key manager.
	 *
	 * @param keyManagerListener the IKeyManagerListener instance to unregister.
	 * @throws IllegalArgumentException
	 */
	//void removeKeyManagerListener(IKeyManagerListener keyManagerListener)
	//	throws IllegalArgumentException;

	String getApplicationActiveKeyConfigurationId();
	String getUserActiveKeyConfigurationId();
	void setUserActiveKeyConfigurationId(String userActiveKeyConfigurationId);
	String getActiveKeyConfigurationId();

	SortedSet getApplicationKeyBindings();
	SortedSet getUserKeyBindings();
	void setUserKeyBindings(SortedSet userKeyBindings);
	SortedSet getKeyBindings();

	SortedSet getApplicationKeyConfigurations();
	SortedSet getUserKeyConfigurations();
	void setUserKeyConfigurations(SortedSet userKeyConfigurations);
	SortedSet getKeyConfigurations();	
}

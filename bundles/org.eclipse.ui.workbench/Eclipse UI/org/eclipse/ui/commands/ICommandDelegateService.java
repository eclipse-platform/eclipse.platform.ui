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
public interface ICommandDelegateService {

	/**
	 * JAVADOC
	 *
	 * @param commandId
	 * @param commandDelegate
	 * @throws NullPointerException
	 */	
	void addCommandDelegate(String commandId, ICommandDelegate commandDelegate);

	/**
	 * Registers an ICommandDelegateServiceListener instance with this command delegate service.
	 *
	 * @param commandDelegateServiceListener the ICommandDelegateServiceListener instance to register.
	 * @throws NullPointerException
	 */	
	void addCommandDelegateServiceListener(ICommandDelegateServiceListener commandDelegateServiceListener);
		
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
	 * @throws NullPointerException
	 */	
	void removeCommandDelegate(String commandId);
	
	/**
	 * Unregisters an ICommandDelegateServiceListener instance with this command delegate services.
	 *
	 * @param commandDelegateServiceListener the ICommandDelegateServiceListener instance to unregister.
	 * @throws NullPointerException
	 */
	void removeCommandDelegateServiceListener(ICommandDelegateServiceListener commandDelegateServiceListener);
}
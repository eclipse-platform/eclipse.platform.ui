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

package org.eclipse.ui.internal.commands.api;

import java.util.Set;

/**
 * <p>
 * TODO javadoc
 * </p> 
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see ICommandServiceListener
 */
public interface ICommandService {

	/**
	 * Registers an instance of <code>ICommandServiceListener</code> to listen 
	 * for changes to attributes of this instance.
	 *
	 * @param commandServiceListener the instance of 
	 *                               <code>ICommandServiceListener</code> to 
	 * 						         register. Must not be <code>null</code>. 
	 *                               If an attempt is made to register an 
	 *                               instance of 
	 *                               <code>ICommandServiceListener</code> 
	 *                               which is already registered with this 
	 *                               instance, no operation is performed.
	 */	
	void addCommandServiceListener(ICommandServiceListener commandServiceListener);
		
	/**
	 * <p>
	 * Returns the set of identifiers to active commands.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 *
	 * @return the set of identifiers to active commands. This set may be 
	 *         empty, but is guaranteed not to be <code>null</code>. If this set 
	 *         is not empty, it is guaranteed to only contain instances of 
	 *         <code>String</code>.
	 */	
	Set getActiveCommandIds();

	/**
	 * Sets the set of identifiers to active commands. 
	 *
	 * @param activeCommandIds the set of identifiers to active commands. 
	 *                         This set may be empty, but it must not be 
	 *                         <code>null</code>. If this set is not empty, it 
	 *                         must only contain instances of 
	 *                         <code>String</code>.	
	 */
	void setActiveCommandIds(Set activeCommandIds);
	
	/**
	 * Unregisters an instance of <code>ICommandServiceListener</code> 
	 * listening for changes to attributes of this instance.
	 *
	 * @param commandServiceListener the instance of 
	 *                               <code>ICommandServiceListener</code> to 
	 * 						         unregister. Must not be <code>null</code>. 
	 *                               If an attempt is made to unregister an 
	 *                               instance of 
	 *                               <code>ICommandServiceListener</code> 
	 *                               which is not already registered with this 
	 *                               instance, no operation is performed.
	 */
	void removeCommandServiceListener(ICommandServiceListener commandServiceListener);
}
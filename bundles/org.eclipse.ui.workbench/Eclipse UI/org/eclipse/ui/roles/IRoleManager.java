/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.roles;

import java.util.SortedSet;

/**
 * <p>
 * An instance of <code>IRoleManager</code> can be used to obtain instances of 
 * <code>IRole</code>.
 * </p> 
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IRole
 * @see IRoleManagerListener
 * @see RoleManagerFactory
 */
public interface IRoleManager {

	/**
	 * Registers an instance of <code>IRoleManagerListener</code> to listen for 
	 * changes to attributes of this instance.
	 *
	 * @param roleManagerListener the instance of 
	 *                            <code>IRoleManagerListener</code> to register. 
	 *                            Must not be <code>null</code>. If an attempt 
	 *                            is made to register an instance of  
	 *                            <code>IRoleManagerListener</code> which is 
	 *                            already registered with this instance, no 
	 *                            operation is performed.
	 */	
	void addRoleManagerListener(IRoleManagerListener roleManagerListener);

	/**
	 * Returns a handle to an role given an identifier.
	 *
	 * @param  roleId an identifier. Must not be <code>null</code>
	 * @return        a handle to an role.
	 */	
	IRole getRole(String roleId);

	/**
	 * <p>
	 * Returns the set of identifiers to defined roles.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 *
	 * @return the set of identifiers to defined roles. This set may be empty, 
	 *         but is guaranteed not to be <code>null</code>. If this set is not 
	 *         empty, it is guaranteed to only contain instances of 
	 *         <code>String</code>.
	 */	
	SortedSet getDefinedRoleIds();
	
	/**
	 * Unregisters an instance of <code>IRoleManagerListener</code> listening 
	 * for changes to attributes of this instance.
	 *
	 * @param roleManagerListener the instance of 
	 *                            <code>IRoleManagerListener</code> to 
	 *                            unregister. Must not be <code>null</code>. If 
	 *                            an attempt is made to unregister an instance 
	 *                            of <code>IRoleManagerListener</code> which is 
	 *                            not already registered with this instance, no 
	 *                            operation is performed.
	 */
	void removeRoleManagerListener(IRoleManagerListener roleManagerListener);
}

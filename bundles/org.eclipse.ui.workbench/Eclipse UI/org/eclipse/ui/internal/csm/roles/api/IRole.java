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

package org.eclipse.ui.internal.csm.roles.api;

import java.util.Set;

/**
 * <p>
 * An instance of <code>IRole</code> is a handle representing an role as defined 
 * by the extension point <code>org.eclipse.ui.roles</code>. The identifier of 
 * the handle is identifier of the role being represented.
 * </p>
 * <p>
 * An instance of <code>IRole</code> can be obtained from an instance of 
 * <code>IRoleManager</code> for any identifier, whether or not the role 
 * represented by this handle is defined in the plugin registry.
 * </p>
 * <p>
 * The handle-based nature of this API allows it to work well with runtime 
 * plugin activation and deactivation, which causes dynamic changes to the 
 * plugin registry, and therefore, potentially, dynamic changes to the set of 
 * role definitions.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IRoleListener
 * @see IRoleManager
 */
public interface IRole extends Comparable {

	/**
	 * Registers an instance of <code>IRoleListener</code> to listen for changes 
	 * to attributes of this instance.
	 *
	 * @param roleListener the instance of <code>IRoleListener</code> to 
	 *                     register. Must not be <code>null</code>. If an 
	 *                     attempt is made to register an instance of 
	 *                     <code>IRoleListener</code> which is already 
	 *                     registered with this instance, no operation is 
	 *                     performed.
	 */	
	void addRoleListener(IRoleListener roleListener);

	/**
	 * <p>
	 * Returns the set of activity bindings for this handle. This method will
	 * return all activity bindings for this handle's identifier, whether or not 
	 * the role represented by this handle is defined. 
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 *
	 * @return the set of activity bindings. This set may be empty, but is 
	 * 		   guaranteed not to be <code>null</code>. If this set is not empty, 
	 *         it is guaranteed to only contain instances of 
	 *         <code>IActivityBinding</code>.
	 */	
	Set getActivityBindings();

	/**
	 * <p>
	 * Returns the description of the role represented by this handle, suitable 
	 * for display to the user.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 * 
	 * @return the description of the role represented by this handle. 
	 *         Guaranteed not to be <code>null</code>.
	 * @throws RoleNotDefinedException if the role represented by this handle is 
	 *                                 not defined.
	 */	
	String getDescription()
		throws RoleNotDefinedException;
	
	/**
	 * Returns the identifier of this handle.
	 * 
	 * @return the identifier of this handle. Guaranteed not to be 
	 *         <code>null</code>.
	 */	
	String getId();
	
	/**
	 * <p>
	 * Returns the name of the role represented by this handle, suitable for 
	 * display to the user.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 *  
	 * @return the name of the role represented by this handle. Guaranteed 
	 *         not to be <code>null</code>.
	 * @throws RoleNotDefinedException if the role represented by this handle is 
	 *                                 not defined.
	 */	
	String getName()
		throws RoleNotDefinedException;

	/**
	 * <p>
	 * Returns whether or not the role represented by this handle is defined. 
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 * 
	 * @return <code>true</code>, iff the role represented by this handle is 
	 *         defined. 
	 */	
	boolean isDefined();

	/**
	 * Unregisters an instance of <code>IRoleListener</code> listening for
	 * changes to attributes of this instance.
	 *
	 * @param roleListener the instance of <code>IRoleListener</code> to 
	 * 	 				   unregister. Must not be <code>null</code>. If an 
	 *                     attempt is made to unregister an instance of 
	 *                     <code>IRoleListener</code> which is not already 
	 *                     registered with this instance, no operation is 
	 *                     performed.
	 */
	void removeRoleListener(IRoleListener roleListener);
}

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

package org.eclipse.ui.activities;

import java.util.Set;

/**
 * <p>
 * An instance of <code>IActivity</code> is a handle representing an activity 
 * as defined by the extension point <code>org.eclipse.ui.activities</code>. The
 * identifier of the handle is identifier of the activity being represented.
 * </p>
 * <p>
 * An instance of <code>IActivity</code> can be obtained from an instance of 
 * <code>IActivityManager</code> for any identifier, whether or not an 
 * activity with that identifier defined in the plugin registry.
 * </p>
 * <p>
 * The handle-based nature of this API allows it to work well with runtime 
 * plugin activation and deactivation, which causes dynamic changes to the 
 * plugin registry, and therefore, potentially, dynamic changes to the set of 
 * activity definitions.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IActivityListener
 * @see IActivityManager
 * @see IPatternBinding
 */
public interface IActivity extends Comparable {

	/**
	 * Registers an instance of <code>IActivityListener</code> to listen for
	 * changes to attributes of this instance.
	 *
	 * @param activityListener the instance of <code>IActivityListener</code> to 
	 * 						   register. Must not be <code>null</code>. If an 
	 * 						   attempt is made to register an instance of 
	 *                         <code>IActivityListener</code> which is already 
	 *                         registered with this instance, no operation is 
	 *                         performed.
	 */	
	void addActivityListener(IActivityListener activityListener);

	/**
	 * <p>
	 * Returns the description of the activity represented by this handle, 
	 * suitable for display to the user.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 * 
	 * @return the description of the activity represented by this handle. 
	 *         Guaranteed not to be <code>null</code>.
	 * @throws ActivityNotDefinedException if the activity represented by this 
	 *                                     handle is not defined.
	 */	
	String getDescription()
		throws ActivityNotDefinedException;
	
	/**
	 * Returns the identifier of this handle.
	 * 
	 * @return the identifier of this handle. Guaranteed not to be 
	 *         <code>null</code>.
	 */	
	String getId();
	
	/**
	 * <p>
	 * Returns the name of the activity represented by this handle, suitable for 
	 * display to the user.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 *  
	 * @return the name of the activity represented by this handle. Guaranteed 
	 *         not to be <code>null</code>.
	 * @throws ActivityNotDefinedException if the activity represented by this 
	 *                                     handle is not defined.
	 */	
	String getName()
		throws ActivityNotDefinedException;

	/**
	 * <p>
	 * Returns the set of pattern bindings for this handle. This method will
	 * return all pattern bindings for this handle's identifier, whether or not 
	 * the activity represented by this handle is defined. 
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 *
	 * @return the set of pattern bindings. This set may be empty, but is 
	 * 		   guaranteed not to be <code>null</code>. If this set is not empty, 
	 *         it is guaranteed to only contain instances of 
	 *         <code>IPatternBinding</code>.
	 */	
	Set getPatternBindings();

	/**
	 * <p>
	 * Returns whether or not this activity is active. Instances of 
	 * <code>IActivity</code> are activated and deactivated by the instance of
	 * <code>IActivityManager</code> from whence they were brokered.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 * 
	 * @return <code>true</code>, iff the activity is active. 
	 */	
	boolean isActive();
	
	/**
	 * <p>
	 * Returns whether or not the activity represented by this handle is 
	 * defined. 
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 * 
	 * @return <code>true</code>, iff the activity represented by this handle is 
	 *         defined. 
	 */	
	boolean isDefined();
	
	/**
	 * <p>
	 * Returns whether or not this activity is enabled. Instances of 
	 * <code>IActivity</code> are enabled and disabled by the instance of
	 * <code>IActivityManager</code> from whence they were brokered.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 * 
	 * @return <code>true</code>, iff the activity is active. 
	 */	
	boolean isEnabled();	

	/**
	 * Unregisters an instance of <code>IActivityListener</code> listening for
	 * changes to attributes of this instance.
	 *
	 * @param activityListener the instance of <code>IActivityListener</code> to 
	 * 						   unregister. Must not be <code>null</code>. If an 
	 * 						   attempt is made to unregister an instance of 
	 *                         <code>IActivityListener</code> which is not 
	 *                         already registered with this instance, no 
	 *                         operation is performed.
	 */
	void removeActivityListener(IActivityListener activityListener);
}

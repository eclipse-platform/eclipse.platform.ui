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
import java.util.SortedSet;

/**
 * <p>
 * An instance of <code>IActivityManager</code> can be used to obtain instances
 * of <code>IActivity</code>, as well as manage whether or not those instances
 * are active or inactive, enabled or disabled.
 * </p> 
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see ActivityManagerFactory
 * @see IActivity
 * @see IActivityManagerListener
 */
public interface IActivityManager {

	/**
	 * Registers an instance of <code>IActivityManagerListener</code> to listen 
	 * for changes to attributes of this instance.
	 *
	 * @param activityManagerListener the instance of 
	 *                                <code>IActivityManagerListener</code> to 
	 * 						          register. Must not be <code>null</code>. 
	 *                                If an attempt is made to register an 
	 *                                instance of 
	 *                                <code>IActivityManagerListener</code> 
	 *                                which is already registered with this 
	 *                                instance, no operation is performed.
	 */		
	void addActivityManagerListener(IActivityManagerListener activityManagerListener);

	/**
	 * <p>
	 * Returns the set of identifiers to active activities. This set is not 
	 * necessarily a subset of the set of identifiers to defined activities.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 *
	 * @return the set of identifiers to active activities. This set may be 
	 *         empty, but is guaranteed not to be <code>null</code>. If this set 
	 *         is not empty, it is guaranteed to only contain instances of 
	 *         <code>String</code>.
	 */	
	Set getActiveActivityIds();

	/**
	 * Returns a handle to an activity given an identifier.
	 *
	 * @param activityId an identifier. Must not be <code>null</code>
	 * @return           a handle to an activity.
	 */	
	IActivity getActivity(String activityId);

	/**
	 * <p>
	 * Returns the set of identifiers to defined activities.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 *
	 * @return the set of identifiers to defined activities. This set may be 
	 *         empty, but is guaranteed not to be <code>null</code>. If this set 
	 *         is not empty, it is guaranteed to only contain instances of 
	 *         <code>String</code>.
	 */	
	SortedSet getDefinedActivityIds();

	/**
	 * <p>
	 * Returns the set of identifiers to enabled activities. This set is not 
	 * necessarily a subset of the set of identifiers to defined activities.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 *
	 * @return the set of identifiers to enabled activities. This set may be 
	 *         empty, but is guaranteed not to be <code>null</code>. If this set 
	 *         is not empty, it is guaranteed to only contain instances of 
	 *         <code>String</code>.
	 */	
	Set getEnabledActivityIds();	
	
	/**
	 * Unregisters an instance of <code>IActivityManagerListener</code> 
	 * listening for changes to attributes of this instance.
	 *
	 * @param activityManagerListener the instance of 
	 *                                <code>IActivityManagerListener</code> to 
	 * 						          unregister. Must not be <code>null</code>. 
	 *                                If an attempt is made to unregister an 
	 *                                instance of 
	 *                                <code>IActivityManagerListener</code> 
	 *                                which is not already registered with this 
	 *                                instance, no operation is performed.
	 */
	void removeActivityManagerListener(IActivityManagerListener activityManagerListener);

	/**
	 * Sets the set of identifiers to active activities. 
	 *
	 * @param activeActivityIds the set of identifiers to active activities. 
	 *                          This set may be empty, but it must not be 
	 *                          <code>null</code>. If this set is not empty, it 
	 *                          must only contain instances of 
	 *                          <code>String</code>.	
	 */
	void setActiveActivityIds(Set activeActivityIds);
	
	/**
	 * Sets the set of identifiers to enabled activities. 
	 *
	 * @param enabledActivityIds the set of identifiers to enabled activities. 
	 *                           This set may be empty, but it must not be 
	 *                           <code>null</code>. If this set is not empty, it 
	 *                           must only contain instances of 
	 *                           <code>String</code>.	
	 */
	void setEnabledActivityIds(Set enabledActivityIds);		
}

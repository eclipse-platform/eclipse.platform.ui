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
 * An instance of <code>IActivityManager</code> can be used to obtain
 * instances of <code>IActivity</code> and <code>ICategory</code>.
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
 * @see ICategory
 */
public interface IActivityManager {

	/**
	 * Registers an instance of <code>IActivityManagerListener</code> to
	 * listen for changes to attributes of this instance.
	 * 
	 * @param activityManagerListener
	 *            the instance of <code>IActivityManagerListener</code> to
	 *            register. Must not be <code>null</code>. If an attempt is
	 *            made to register an instance of <code>IActivityManagerListener</code>
	 *            which is already registered with this instance, no operation
	 *            is performed.
	 */
	void addActivityManagerListener(IActivityManagerListener activityManagerListener);

	/**
	 * Returns a handle to an activity given an identifier.
	 * 
	 * @param activityId
	 *            an identifier. Must not be <code>null</code>
	 * @return a handle to an activity.
	 */
	IActivity getActivity(String activityId);

	/**
	 * Returns a handle to an category given an identifier.
	 * 
	 * @param categoryId
	 *            an identifier. Must not be <code>null</code>
	 * @return a handle to an category.
	 */
	ICategory getCategory(String categoryId);

	/**
	 * <p>
	 * Returns the set of identifiers to defined activities.
	 * </p>
	 * <p>
	 * Notification is sent to all registered listeners if this attribute
	 * changes.
	 * </p>
	 * 
	 * @return the set of identifiers to defined activities. This set may be
	 *         empty, but is guaranteed not to be <code>null</code>. If this
	 *         set is not empty, it is guaranteed to only contain instances of
	 *         <code>String</code>.
	 */
	Set getDefinedActivityIds();

	/**
	 * <p>
	 * Returns the set of identifiers to defined categories.
	 * </p>
	 * <p>
	 * Notification is sent to all registered listeners if this attribute
	 * changes.
	 * </p>
	 * 
	 * @return the set of identifiers to defined categories. This set may be
	 *         empty, but is guaranteed not to be <code>null</code>. If this
	 *         set is not empty, it is guaranteed to only contain instances of
	 *         <code>String</code>.
	 */
	Set getDefinedCategoryIds();

	/**
	 * <p>
	 * Returns the set of identifiers to enabled activities. This set is not
	 * necessarily a subset of the set of identifiers to defined activities.
	 * </p>
	 * <p>
	 * Notification is sent to all registered listeners if this attribute
	 * changes.
	 * </p>
	 * 
	 * @return the set of identifiers to enabled activities. This set may be
	 *         empty, but is guaranteed not to be <code>null</code>. If this
	 *         set is not empty, it is guaranteed to only contain instances of
	 *         <code>String</code>.
	 */
	Set getEnabledActivityIds();

	/**
	 * <p>
	 * Returns the set of identifiers to enabled categories. This set is not
	 * necessarily a subset of the set of identifiers to defined categories.
	 * </p>
	 * <p>
	 * Notification is sent to all registered listeners if this attribute
	 * changes.
	 * </p>
	 * 
	 * @return the set of identifiers to enabled categories. This set may be
	 *         empty, but is guaranteed not to be <code>null</code>. If this
	 *         set is not empty, it is guaranteed to only contain instances of
	 *         <code>String</code>.
	 */
	Set getEnabledCategoryIds();

	/**
	 * TODO javadoc
	 */
	Set getMatches(String string, Set activityIds);

	/**
	 * TODO javadoc
	 */
	boolean isMatch(String string, Set activityIds);

	/**
	 * TODO javadoc
	 * @deprecated use isMatch(String, Set);
	 */
	boolean match(String string, Set activityIds);
	
	/**
	 * Unregisters an instance of <code>IActivityManagerListener</code>
	 * listening for changes to attributes of this instance.
	 * 
	 * @param activityManagerListener
	 *            the instance of <code>IActivityManagerListener</code> to
	 *            unregister. Must not be <code>null</code>. If an attempt
	 *            is made to unregister an instance of <code>IActivityManagerListener</code>
	 *            which is not already registered with this instance, no
	 *            operation is performed.
	 */
	void removeActivityManagerListener(IActivityManagerListener activityManagerListener);
}

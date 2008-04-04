/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.activities;

import java.util.Set;

/**
 * An instance of this interface allows clients to manage activities, as
 * defined by the extension point <code>org.eclipse.ui.activities</code>.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IActivityManager {

    /**
     * Registers an instance of <code>IActivityManagerListener</code> to
     * listen for changes to properties of this instance.
     * 
     * @param activityManagerListener
     *            the instance to register. Must not be <code>null</code>.
     *            If an attempt is made to register an instance which is
     *            already registered with this instance, no operation is
     *            performed.
     */
    void addActivityManagerListener(
            IActivityManagerListener activityManagerListener);

    /**
     * Returns an instance of <code>IActivity</code> given an identifier.
     * 
     * @param activityId
     *            an identifier. Must not be <code>null</code>
     * @return an instance of <code>IActivity</code>.
     */
    IActivity getActivity(String activityId);

    /**
     * Returns an instance of <code>ICategory</code> given an identifier.
     * 
     * @param categoryId
     *            an identifier. Must not be <code>null</code>
     * @return an instance of <code>ICategory</code>.
     */
    ICategory getCategory(String categoryId);

    /**
     * Returns the set of identifiers to defined activities.
     * <p>
     * Notification is sent to all registered listeners if this property
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
     * Returns the set of identifiers to defined categories.
     * <p>
     * Notification is sent to all registered listeners if this property
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
     * Returns the set of identifiers to enabled activities.
     * <p>
     * Notification is sent to all registered listeners if this property
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
     * Returns an instance of <code>IIdentifier</code> given an identifier.
     * 
     * @param identifierId
     *            an identifier. Must not be <code>null</code>
     * @return an instance of <code>IIdentifier</code>.
     */
    IIdentifier getIdentifier(String identifierId);

    /**
     * Removes an instance of <code>IActivityManagerListener</code>
     * listening for changes to properties of this instance.
     * 
     * @param activityManagerListener
     *            the instance to remove. Must not be <code>null</code>.
     *            If an attempt is made to remove an instance which is not
     *            already registered with this instance, no operation is
     *            performed.
     */
    void removeActivityManagerListener(
            IActivityManagerListener activityManagerListener);
}

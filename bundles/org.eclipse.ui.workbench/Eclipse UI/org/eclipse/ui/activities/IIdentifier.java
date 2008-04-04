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
 * An instance of this interface can be obtained from an instance of 
 * <code>IActivityManager</code>for any identifier.
 * <p>
 * An <code>IIdentifier</code> is an object that offers an easy means to 
 * determine if a given string matches the pattern bindings of any IActivity 
 * objects.  Additionaly, one may query if an identifier is enabled.  An 
 * identifier is always considered enabled unless it matches only disabled activities. 
 * </p>
 * <p>
 * The handle-based nature of this API allows it to work well with runtime
 * plugin activation and deactivation, which can cause dynamic changes to the
 * extension registry.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see IActivityManager#getIdentifier(String)
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IIdentifier extends Comparable {

    /**
     * Registers an instance of <code>IIdentifierListener</code> to listen
     * for changes to properties of this instance.
     * 
     * @param identifierListener
     *            the instance to register. Must not be <code>null</code>.
     *            If an attempt is made to register an instance which is
     *            already registered with this instance, no operation is
     *            performed.
     */
    void addIdentifierListener(IIdentifierListener identifierListener);

    /**
     * Returns the set of activity ids that this instance matches.  Each 
     * activity in this set will have at least one pattern binding that matches
     * the string returned by {@link #getId()}.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the set of activity ids that this instance matches. This set may
     *         be empty, but is guaranteed not to be <code>null</code>. If
     *         this set is not empty, it is guaranteed to only contain
     *         instances of <code>String</code>.
     */
    Set getActivityIds();

    /**
     * Returns the identifier of this instance.
     * 
     * @return the identifier of this instance. Guaranteed not to be <code>null</code>.
     */
    String getId();

    /**
     * Returns whether or not this instance is enabled.  An identifier is always
     * considered enabled unless it matches only disabled activities.
     * 
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return <code>true</code>, iff this instance is enabled.
     */
    boolean isEnabled();

    /**
     * Removes an instance of <code>IIdentifierListener</code> listening
     * for changes to properties of this instance.
     * 
     * @param identifierListener
     *            the instance to remove. Must not be <code>null</code>.
     *            If an attempt is made to remove an instance which is not
     *            already registered with this instance, no operation is
     *            performed.
     */
    void removeIdentifierListener(IIdentifierListener identifierListener);
}

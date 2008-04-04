/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import org.eclipse.core.expressions.Expression;

/**
 * An instance of this interface is an activity as defined by the extension
 * point <code>org.eclipse.ui.activities</code>.
 * 
 * <p>
 * An instance of this interface can be obtained from an instance of 
 * <code>IActivityManager</code> for any identifier, whether or not an activity 
 * with that identifier is defined in the extension registry.
 * </p>
 * 
 * <p>
 * The handle-based nature of this API allows it to work well with runtime
 * plugin activation and deactivation, which can cause dynamic changes to the
 * extension registry.  A client may get an <code>IActivity</code> handle that 
 * is currently undefined ({@link #isDefined()} equals <code>false</code>) and 
 * listen for it to become defined.
 * </p>
 * 
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see IActivityManager
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IActivity extends Comparable {

    /**
     * Registers an instance of <code>IActivityListener</code> to listen for
     * changes to properties of this instance.
     * 
     * @param activityListener
     *            the instance to register. Must not be <code>null</code>.
     *            If an attempt is made to register an instance which is
     *            already registered with this instance, no operation is
     *            performed.
     */
    void addActivityListener(IActivityListener activityListener);

    /**
     * Returns the set of activity requirement bindings for this instance.
     * <p>
     * This method will return all activity requirement bindings for this
     * instance, whether or not this instance is defined.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the set of activity requirement bindings. This set may be empty,
     *         but is guaranteed not to be <code>null</code>. If this set is
     *         not empty, it is guaranteed to only contain instances of 
     * 		   <code>IActivityRequirementBinding</code>.
     * @see IActivityRequirementBinding
     */
    Set getActivityRequirementBindings();

    /**
     * Returns the set of activity pattern bindings for this instance.
     * <p>
     * This method will return all activity pattern bindings for this instance,
     * whether or not this instance is defined.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the set of activity pattern bindings. This set may be empty, but
     *         is guaranteed not to be <code>null</code>. If this set is not
     *        empty, it is guaranteed to only contain instances of <code>IActivityPatternBinding</code>.
     * @see IActivityPatternBinding
     */
    Set getActivityPatternBindings();

    /**
     * Returns the identifier of this instance.
     * 
     * @return the identifier of this instance. Guaranteed not to be <code>null</code>.
     */
    String getId();

    /**
     * Returns the name of this instance suitable for display to the user.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the name of this instance. Guaranteed not to be <code>null</code>.
     * @throws NotDefinedException
     *             if this instance is not defined.
     */
    String getName() throws NotDefinedException;

    /**
     * Returns the description of this instance suitable for display to the user.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the description of this instance. Guaranteed not to be <code>null</code>.
     * @throws NotDefinedException
     *             if this instance is not defined.
     */
    String getDescription() throws NotDefinedException;

    /**
     * Returns whether or not this instance is defined.  A defined activity
     * may have a name, description and bindings (both pattern and relational).
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return <code>true</code>, iff this instance is defined.
     */
    boolean isDefined();

    /**
     * Returns whether or not this instance is enabled.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return <code>true</code>, iff this instance is enabled.
     */
    boolean isEnabled();
    
    /**
     * Returns whether or not this instance should be enabled by default.
     * 
     * @return <code>true</code>, iff this instance should be enabled by default.
     * @throws NotDefinedException
     *             if this instance is not defined. 
     * @since 3.1 
     */
    boolean isDefaultEnabled() throws NotDefinedException;

    /**
     * Removes an instance of <code>IActivityListener</code> listening
     * for changes to properties of this instance.
     * 
     * @param activityListener
     *            the instance to remove. Must not be <code>null</code>.
     *            If an attempt is made to remove an instance which is not
     *            already registered with this instance, no operation is
     *            performed.
     */
    void removeActivityListener(IActivityListener activityListener);
    
    /**
	 * Return an expression used to enable and disable this activity. If the
	 * expression is not <code>null</code>, this activity will be entirely
	 * controlled by the expression state.
	 * 
	 * @return The core expression, or <code>null</code>
	 * @since 3.4
	 */
    Expression getExpression();
}

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
 * An instance of this interface is a category as defined by the extension
 * point <code>org.eclipse.ui.activities</code>.
 * <p>
 * An instance of this interface can be obtained from an instance of 
 * <code>IActivityManager</code> for any identifier, whether or not a category 
 * with that identifier is defined in the extension registry.
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
 * @see IActivityManager
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICategory extends Comparable {

    /**
     * Registers an instance of <code>ICategoryListener</code> to listen for
     * changes to properties of this instance.
     * 
     * @param categoryListener
     *            the instance to register. Must not be <code>null</code>.
     *            If an attempt is made to register an instance which is
     *            already registered with this instance, no operation is
     *            performed.
     */
    void addCategoryListener(ICategoryListener categoryListener);

    /**
     * Returns the set of category activity bindings for this instance.
     * <p>
     * This method will return all category activity bindings for this
     * instance, whether or not this instance is defined.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the set of category activity bindings. This set may be empty,
     *         but is guaranteed not to be <code>null</code>. If this set is
     *         not empty, it is guaranteed to only contain instances of <code>ICategoryActivityBinding</code>.
     * @see ICategoryActivityBinding
     */
    Set getCategoryActivityBindings();

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
     * Returns whether or not this instance is defined.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return <code>true</code>, iff this instance is defined.
     */
    boolean isDefined();

    /**
     * Removes an instance of <code>ICategoryListener</code> listening
     * for changes to properties of this instance.
     * 
     * @param categoryListener
     *            the instance to remove. Must not be <code>null</code>.
     *            If an attempt is made to remove an instance which is not
     *            already registered with this instance, no operation is
     *            performed.
     */
    void removeCategoryListener(ICategoryListener categoryListener);
}

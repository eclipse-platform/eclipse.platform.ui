/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.contexts;

/**
 * An instance of this interface is an context as defined by the extension point
 * <code>org.eclipse.ui.contexts</code>.
 * <p>
 * An instance of this interface can be obtained from an instance of
 * <code>IContextManager</code> for any identifier, whether or not an context
 * with that identifier is defined in the extension registry.
 * </p>
 * <p>
 * The handle-based nature of this API allows it to work well with runtime
 * plugin activation and deactivation. If a context is defined, that means that
 * its corresponding plug-in is active. If the plug-in is then deactivated, the
 * context will still exist but it will be undefined. An attempts to use an
 * undefined context will result in a <code>NotDefinedException</code> being
 * thrown.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see org.eclipse.ui.contexts.IContextManager
 * @see org.eclipse.core.commands.contexts.Context
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IContext extends Comparable {

    /**
     * Registers an instance of <code>IContextListener</code> to listen for
     * changes to properties of this instance.
     * 
     * @param contextListener
     *            the instance to register. Must not be <code>null</code>. If
     *            an attempt is made to register an instance which is already
     *            registered with this instance, no operation is performed.
     */
    void addContextListener(IContextListener contextListener);

    /**
     * Returns the identifier of this instance.
     * 
     * @return the identifier of this instance. Guaranteed not to be
     *         <code>null</code>.
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
     * Returns the identifier of the parent of this instance.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the identifier of the parent of this instance. May be
     *         <code>null</code>.
     * @throws NotDefinedException
     *             if this instance is not defined.
     */
    String getParentId() throws NotDefinedException;

    /**
     * Returns whether or not this instance is defined.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return true, iff this instance is defined.
     */
    boolean isDefined();

    /**
     * Returns whether or not this instance is enabled.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return true, iff this instance is enabled.
     */
    boolean isEnabled();

    /**
     * Unregisters an instance of <code>IContextListener</code> listening for
     * changes to properties of this instance.
     * 
     * @param contextListener
     *            the instance to unregister. Must not be <code>null</code>.
     *            If an attempt is made to unregister an instance which is not
     *            already registered with this instance, no operation is
     *            performed.
     */
    void removeContextListener(IContextListener contextListener);
}

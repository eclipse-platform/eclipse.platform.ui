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

package org.eclipse.ui.contexts;

import java.util.Set;

import org.eclipse.ui.internal.contexts.ContextManagerFactory;

/**
 * An instance of this interface allows clients to manage contexts, as defined
 * by the extension point <code>org.eclipse.ui.contexts</code>.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see ContextManagerFactory
 */
public interface IContextManager {

    /**
     * Registers an instance of <code>IContextManagerListener</code> to listen
     * for changes to properties of this instance.
     * 
     * @param contextManagerListener
     *            the instance to register. Must not be <code>null</code>. If
     *            an attempt is made to register an instance which is already
     *            registered with this instance, no operation is performed.
     */
    void addContextManagerListener(
            IContextManagerListener contextManagerListener);

    /**
     * Returns an instance of <code>IContext</code> given an identifier.
     * 
     * @param contextId
     *            an identifier. Must not be <code>null</code>
     * @return an instance of <code>IContext</code>.
     */
    IContext getContext(String contextId);

    /**
     * Returns the set of identifiers to defined contexts.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the set of identifiers to defined contexts. This set may be
     *         empty, but is guaranteed not to be <code>null</code>. If this
     *         set is not empty, it is guaranteed to only contain instances of
     *         <code>String</code>.
     */
    Set getDefinedContextIds();

    /**
     * Returns the set of identifiers to enabled contexts.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the set of identifiers to enabled contexts. This set may be
     *         empty, but is guaranteed not to be <code>null</code>. If this
     *         set is not empty, it is guaranteed to only contain instances of
     *         <code>String</code>.
     */
    Set getEnabledContextIds();

    /**
     * Unregisters an instance of <code>IContextManagerListener</code>
     * listening for changes to properties of this instance.
     * 
     * @param contextManagerListener
     *            the instance to unregister. Must not be <code>null</code>.
     *            If an attempt is made to unregister an instance which is not
     *            already registered with this instance, no operation is
     *            performed.
     */
    void removeContextManagerListener(
            IContextManagerListener contextManagerListener);
}
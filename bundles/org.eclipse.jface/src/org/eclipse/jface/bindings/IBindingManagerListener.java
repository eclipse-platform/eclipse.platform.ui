/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.bindings;

/**
 * <p>
 * An instance of <code>ISchemeListener</code> can be used by clients to
 * receive notification of changes to one or more instances of
 * <code>IScheme</code>.
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * 
 * @since 3.1
 * @see BindingManager#addBindingManagerListener(IBindingManagerListener)
 * @see org.eclipse.jface.bindings.BindingManager#addBindingManagerListener(IBindingManagerListener)
 * @see BindingManagerEvent
 */
public interface IBindingManagerListener {

    /**
     * Notifies that the set of defined or active scheme or bindings has changed
     * in the binding manager.
     * 
     * @param event
     *            the scheme event. Guaranteed not to be <code>null</code>.
     */
    void bindingManagerChanged(BindingManagerEvent event);
}

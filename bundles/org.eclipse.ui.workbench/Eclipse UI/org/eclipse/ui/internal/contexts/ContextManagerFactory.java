/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.contexts;

import org.eclipse.commands.contexts.ContextManager;

/**
 * This class allows clients to broker instances of <code>IContextManager</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 */
public final class ContextManagerFactory {

    /**
     * Creates a new instance of <code>IMutableContextManager</code>.
     * 
     * @param contextManager
     *            The context manager that this mutable context manager should
     *            wrapper; must not be <code>null</code>.
     * @return a new instance of <code>IMutableContextManager</code>. Clients
     *         should not make assumptions about the concrete implementation
     *         outside the contract of the interface. Guaranteed not to be
     *         <code>null</code>.
     */
    public static IMutableContextManager getMutableContextManager(
            final ContextManager contextManager) {
        return new MutableContextManager(contextManager);
    }

    private ContextManagerFactory() {
    }
}
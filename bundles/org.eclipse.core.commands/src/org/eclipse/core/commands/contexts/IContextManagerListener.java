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

package org.eclipse.core.commands.contexts;

/**
 * An instance of this interface can be used by clients to receive notification
 * of changes to one or more instances of <code>IContextManager</code>.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * 
 * @since 3.1
 * @see ContextManager#addContextManagerListener(IContextManagerListener)
 * @see ContextManager#removeContextManagerListener(IContextManagerListener)
 */
public interface IContextManagerListener {

    /**
     * Notifies that one or more properties of an instance of
     * <code>IContextManager</code> have changed. Specific details are
     * described in the <code>ContextManagerEvent</code>.
     * 
     * @param contextManagerEvent
     *            the context manager event. Guaranteed not to be
     *            <code>null</code>.
     */
    void contextManagerChanged(ContextManagerEvent contextManagerEvent);
}

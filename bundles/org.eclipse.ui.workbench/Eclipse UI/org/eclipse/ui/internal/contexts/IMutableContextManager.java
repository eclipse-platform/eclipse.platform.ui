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

import java.util.Set;

import org.eclipse.ui.contexts.IContextManager;

/**
 * <p>
 * A context manager is a type of context manager that allows its enabled
 * context identifiers to be modified. The list can only be modified wholesale --
 * individual addition and removal is not permitted.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see ContextManagerFactory
 */
public interface IMutableContextManager extends IContextManager {

    /**
     * Sets the set of identifiers to enabled contexts. If there are any
     * ancestors of these contexts that are not enabled, they should be enabled
     * as well.
     * 
     * @param enabledContextIds
     *            the set of identifiers to enabled contexts. This set may be
     *            empty, but it must not be <code>null</code>. If this set is
     *            not empty, it must only contain instances of
     *            <code>String</code>.
     */
    void setEnabledContextIds(Set enabledContextIds);
}
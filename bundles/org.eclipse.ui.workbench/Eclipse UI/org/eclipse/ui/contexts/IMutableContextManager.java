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
public interface IMutableContextManager extends IContextManager {

    /**
     * Sets the set of identifiers to enabled contexts.
     * 
     * @param enabledContextIds
     *            the set of identifiers to enabled contexts. This set may be
     *            empty, but it must not be <code>null</code>. If this set is
     *            not empty, it must only contain instances of
     *            <code>String</code>.
     */
    void setEnabledContextIds(Set enabledContextIds);
}
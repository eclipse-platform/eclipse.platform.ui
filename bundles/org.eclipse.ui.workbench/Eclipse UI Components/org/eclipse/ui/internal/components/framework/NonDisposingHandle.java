/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.components.framework;

import org.eclipse.ui.internal.components.NullDisposable;

/**
 * A component handle that does not dispose the object it points to.
 * This is typically used for:
 * 
 * <ul>
 * <li>Handles to singleton objects, where the owner of the handle does not own the component</li>
 * <li>Handles to temporary objects, where the object does not require any explicit disposal and could safely
 * be discarded and recreated if necessary</li>
 * </ul>
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public final class NonDisposingHandle extends ComponentHandle {

    /**
     * Creates a handle that will reference the given component but will
     * not clean up after it.
     * 
     * @param component component being referenced
     */
    public NonDisposingHandle(Object component) {
        super(component);
    }
    
    /* (non-javadoc)
     * 
     */
    public IDisposable getDisposable() {
        return NullDisposable.instance;
    }
    
    /* (non-javadoc)
     * 
     */
    public boolean requiresDisposal() {
        return false;
    }
}

/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.multiplexer;

import org.eclipse.ui.components.IServiceProvider;

/**
 * An object implementing this interface will be made available as 
 * a service to delegating components. Simple, infrequently
 * used components may wish to use this service to dynamically query 
 * for the interface to delegate to rather than implementing 
 * <code>IDelegatingComponent</code>.
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public interface IDelegatingContext {
    /**
     * Returns the active service provider for this context, or null
     * if this context is delegating nowhere. This pointer will change
     * dynamically, and should not be cached.
     *
     * @return the active service provider for this context, or null
     * if this context is delegating nowhere
     */
	public IServiceProvider getActive();
}

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

import org.eclipse.core.components.IServiceProvider;

/**
 * Service made available to nested components. Provides the nested
 * components with access to shared services.
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public interface ISharedContext {
    /**
     * Returns the shared components. This is a constant, and may
     * be cached.  
     *
     * @return the components shared between all nested components
     */
    IServiceProvider getSharedComponents();
}

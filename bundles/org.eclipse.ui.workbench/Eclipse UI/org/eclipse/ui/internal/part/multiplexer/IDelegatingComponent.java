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
 * Interface implemented by components capable of dynamically delegating to 
 * another component.
 *
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public interface IDelegatingComponent {
    /**
     * Sets the service provider that this component should delegate to, or null
     * if none.
     *
     * @param target service provider that this component should delegate to.
     */
    public void setActive(IServiceProvider target);
}

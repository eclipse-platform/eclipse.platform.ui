/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.memory;

import org.eclipse.core.runtime.CoreException;

/**
 * Represents a type of memory rendering contributed via the <code>memoryRenderings</code>
 * extension point.
 * <p>
 * Clients contributing a rendering usually
 * implement {@link org.eclipse.debug.ui.memory.IMemoryRenderingTypeDelegate}
 * and {@link org.eclipse.debug.ui.memory.IMemoryRendering}. Clients providing
 * dynamic rendering bindings via an 
 * {@link org.eclipse.debug.ui.memory.IMemoryRenderingBindingsProvider}
 * may implement this interface.
 * </p>
 * @since 3.1
 */
public interface IMemoryRenderingType {
    
    /**
     * Returns a label for this type of memory rendering.
     * 
     * @return a label for this type of memory rendering
     */
    public String getLabel();
    
    /**
     * Returns the unique identifier for this rendering type.
     * 
     * @return the unique identifier for this rendering type
     */
    public String getId();
    
    /**
     * Creates and returns a new rendering of this type or <code>null</code>
     * if none.
     * 
     * @return a new rendering of this type
     * @exception CoreException if an exception occurs creating
     *  the rendering
     */
    public IMemoryRendering createRendering() throws CoreException;

}

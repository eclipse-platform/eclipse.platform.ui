/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
 * Clients are not intended to implement this interface. Instead, clients contributing a rendering
 * implement <code>IMemoryRenderingTypeDelegate</code> and <code>IMemoryRendering</code>.
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
     * Creates and returns a new rendering of this type.
     * Return null if the rendering is not to be created.  Creation of a memory
     * has been canceled.  No error message will be displayed.
     * Throws an exception if an error has occurred.
     * 
     * @return a new rendering of this type
     * @exception CoreException if unable to create the rendering
     */
    public IMemoryRendering createRendering() throws CoreException;

}

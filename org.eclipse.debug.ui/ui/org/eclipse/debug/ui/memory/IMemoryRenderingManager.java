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
package org.eclipse.debug.ui.memory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IMemoryBlock;


/**
 * Manager for memory renderings. Provides facilities for creating
 * renderings.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @since 3.1
 */
public interface IMemoryRenderingManager {
    		
    /**
     * Creates and returns a rendering specified by the given identifier,
     * or <code>null</code> if none.
     * 
     * @param id identifier of the rendering type to create
     * @return specified rendering or <code>null</code> if none
     * @exception CoreException if unable to create the specified rendering
     */
    public IMemoryRendering createRendering(String id) throws CoreException;
    
    /**
     * Returns all contributed memory rendering types.
     * 
     * @return all contributed memory rendering types
     */
    public IMemoryRenderingType[] getRenderingTypes();
    
    /**
     * Returns the memory rendering type with the given identifier, or
     * <code>null</code> if none.
     * 
     * @param id memory rendering type identifier
     * @return the memory rendering type with the given identifier, or
     * <code>null</code> if none
     */
    public IMemoryRenderingType getRenderingType(String id);
    
    /**
     * Returns a collection of rendering types that are defined as the default
     * rendering types for the given memory block, possibly empty. If a primary
     * rendering was specified as part of a memory block's default bindings,
     * it will appear first in the list.
     *  
     * @param block memory block
     * @return a collection of rendering types that are defined as the default
     * rendering types for the given memory block, possibly empty
     */
    public IMemoryRenderingType[] getDefaultRenderingTypes(IMemoryBlock block);
    
    /**
     * Returns the default rendering type specified as the primary rendering
     * type for a memory block, or <code>null</code> if none.
     * 
     * @param block memory block
     * @return the default rendering type specified as the primary rendering
     * type for a memory block, or <code>null</code> if none
     */
    public IMemoryRenderingType getPrimaryRenderingType(IMemoryBlock block);
    
    /**
     * Returns a collection of all rendering types that are bound to
     * the given memory block, possibly empty. This includes default
     * and dynamic rendering types bound to the memory block.
     *  
     * @param block memory block
     * @return a collection of all rendering types that are bound to
     * the given memory block, possibly empty
     */
    public IMemoryRenderingType[] getRenderingTypes(IMemoryBlock block);
    
}



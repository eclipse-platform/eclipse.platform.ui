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


/**
 * Manager for memory renderings. Provides facilities for creating
 * renderings and retrieving memory rendering bindings.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @since 3.1
 */
public interface IMemoryRenderingManager extends IMemoryRenderingBindingsProvider {
    		
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
    
}



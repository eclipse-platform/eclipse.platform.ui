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
 * A factory that creates memory renderings.
 * <p>
 * Clients contributing a memory rendering type are intended to implement this
 * interface. This factory will be used to create renderings. 
 * </p>
 * @since 3.1
 * @see org.eclipse.debug.ui.memory.IMemoryRendering
 */
public interface IMemoryRenderingTypeDelegate {
    
    /**
     * Creates and returns a rendering of the specified type, or <code>null</code>
     * if none.
     * 
     * @param id unique identifier of a memory rendering type
     * @return a new rendering of the given type or <code>null</code>
     * @exception CoreException if unable to create the rendering
     */
    public IMemoryRendering createRendering(String id) throws CoreException;

}

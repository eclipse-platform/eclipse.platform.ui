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

/**
 * A factory that creates memory renderings.
 * <p>
 * Clients contributing a memory rendering type are intended to implement this
 * interface. The factory will be used to create renderings. 
 * </p>
 * @since 3.1
 */
public interface IMemoryRenderingFactory {
    
    /**
     * Creates a rendering of the given type.
     * Return null if the rendering is not to be created.  Creation of a memory
     * has been canceled.  No error message will be displayed.
     * Throws an exception if an error has occurred.
     * @param id unique identifier of a memory rendering type
     * @return a new rendering of the given type
     */
    public IMemoryRendering createRendering(String id) throws Exception;

}

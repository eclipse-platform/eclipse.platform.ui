/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/


package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;


/**
 * Manages all memory renderings in the workbench.
 * @since 3.1
 */
public interface IMemoryRenderingManager
{

	/**
	 * Create and return an IMemoryRendering based on the given memory block and rendering
	 * Id.
	 * @param mem
	 * @param renderingId
	 * @return memory rendering created, null if the rendering cannot be created.  Throws a debug
	 * exception if there is a problem creating the rendering.
	 */
	public IMemoryRendering createRendering(IMemoryBlock mem, String renderingId) throws DebugException;
	
	/**
	 * @param renderingId
	 * @return rendering type of the given rendering id
	 */
	public IMemoryRenderingType getRenderingTypeById(String renderingId);
	
	/**
	 * @param obj
	 * @return all rendering types valid for the given object
	 */
	public IMemoryRenderingType[] getRenderingTypes(Object obj);
	
	/**
	 * @param obj
	 * @param viewId
	 * @return all rendering types valid for the given object and view
	 */
	public IMemoryRenderingType[] getRenderingTypes(Object obj, String viewId);
	
	/**
	 * @param obj
	 * @return default rendering type for the given object
	 */
	public IMemoryRenderingType[] getDefaultRenderingTypes(Object obj);
	

	
}

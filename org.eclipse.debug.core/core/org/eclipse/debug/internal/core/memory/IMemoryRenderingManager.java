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


package org.eclipse.debug.internal.core.memory;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;


/**
 * Manages all memory renderings in the workbench.
 * @since 3.0
 */
public interface IMemoryRenderingManager
{
	/**
	 * Tell the manager to add a new memory rendering
	 * @param mem
	 * @param renderingId
	 * @return the IMemoryRendering object created
	 */
	public IMemoryRendering addMemoryBlockRendering(IMemoryBlock mem, String renderingId) throws DebugException;
	
	
	/**
	 * Tell the manager that a memory rendering has been removed.
	 * Remove all renderings with that same memory block and rendering ids 
	 * @param mem
	 */
	public void removeMemoryBlockRendering(IMemoryBlock mem, String renderingId);
	
	/**
	 * Add the specified rendering from the manager and notify listeners
	 * @param rendering
	 */
	public void addMemoryBlockRendering(IMemoryRendering rendering) throws DebugException;
	
	/**
	 * Remove the specified rendering from the manager and notify listeners
	 * @param rendering
	 */
	public void removeMemoryBlockRendering(IMemoryRendering rendering);
	
	/**
	 * Add a listener to the memory rendering manager.  
	 * @param listener
	 */
	public void addListener(IMemoryRenderingListener listener);
	
	
	/**
	 * Remove a listener from the memory rendering manager.
	 * @param listener
	 */
	public void removeListener(IMemoryRenderingListener listener);
	
	/**
	 * Get renderings based on given memory block and rendering id.
	 * Return all renderings related to the memory block if renderingId
	 * is null.
	 * Return an empty array if the rendering cannot be found.
	 * @param mem
	 * @param renderingId
	 */
	public IMemoryRendering[] getRenderings(IMemoryBlock mem, String renderingId);
	
	/**
	 * Get all memory renderings from the given debug target
	 * Return an empty array if nothing can be found for the debug target.
	 * @param target
	 * @return all memory renderings from the given debug target
	 */
	public IMemoryRendering[] getRenderingsFromDebugTarget(IDebugTarget target);
	
	/**
	 * Get all memory renderings from the given memory block
	 * Return an empty array if nothing can be found for the memory block.
	 * @param block
	 * @return all memory renderings from the given memory block
	 */
	public IMemoryRendering[] getRenderingsFromMemoryBlock(IMemoryBlock block);	
	
	/**
	 * @param renderingId
	 * @return rendering information of the given rendering id
	 */
	public IMemoryRenderingInfo getRenderingInfo(String renderingId);
	
	/**
	 * @param obj
	 * @return all rendering information valid for the given object
	 */
	public IMemoryRenderingInfo[] getAllRenderingInfo(Object obj);
	
	/**
	 * @param obj
	 * @return default renderings' ids for the given object
	 */
	public String[] getDefaultRenderings(Object obj);
	
}

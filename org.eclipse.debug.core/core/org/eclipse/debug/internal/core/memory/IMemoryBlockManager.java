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

import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;


/**
 * Manages all memory blocks in the workbench
 * @since 3.0
 */
public interface IMemoryBlockManager {

	/**
	 * Tell the manager that a memory block has been added.
	 * @param mem
	 * @param addDefaultRenderings - true if default renderings are to be added
	 */
	void addMemoryBlock(IMemoryBlock mem, boolean addDefaultRenderings);
	
	
	/**
	 * Tell the manager that a memory block has been removed.
	 * @param mem
	 */
	void removeMemoryBlock(IMemoryBlock mem);
	
	
	/**
	 * Add a listener to the memory block manager.  
	 * @param listener
	 */
	void addListener(IMemoryBlockListener listener);
	
	
	/**
	 * Remove a listener from the memory block manager.
	 * @param listener
	 */
	void removeListener(IMemoryBlockListener listener);
	
	/**
	 * @return all memory blocks in the workbench.
	 */
	public IMemoryBlock[] getAllMemoryBlocks();
	
	/**
	 * Get all memory blocks associated with the given debug target
	 * (i.e <memoryBlock>.getDebugTarget == debugTarget)
	 * @param debugTarget
	 * @return all memory blocks associated with the given debug target
	 */
	public IMemoryBlock[] getMemoryBlocks(IDebugTarget debugTarget);
	
	/**
	 * Get all memory blocks associated with the given memory block retrieval.
	 * @param retrieve
	 * @return all memory blocks associated with the given memory block retrieval.
	 */
	public IMemoryBlock[] getMemoryBlocks(IMemoryBlockRetrieval retrieve);

}

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




/**
 * Rendering listeners listen for add/removal events from IMemoryRenderingManager.
 * Implementors should add itself to IMemoryRenderingManager
 * @since 3.0
 */
public interface IMemoryRenderingListener
{
	/**
	 * This function is called when a new rendering is added.
	 * @param rendering
	 */
	void MemoryBlockRenderingAdded(IMemoryRendering rendering);

	/**
	 * Fired when a memory rendering is removed.
	 * @param rendering
	 */
	void MemoryBlockRenderingRemoved(IMemoryRendering rendering);	
}

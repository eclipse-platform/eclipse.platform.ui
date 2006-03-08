/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.memory.provisional;

import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;

/**
 *  Context in which an asynchronous request has been made.  This context provides
 *  additional information about the Memory View.
 *
 */
public interface IMemoryViewPresentationContext extends IPresentationContext{

	/**
	 * Returns the memory rendering site from which the asynchronous request
	 * has been made from.
	 * 
	 * @return the memory rendering site
	 */
	public IMemoryRenderingSite getMemoryRenderingSite();

	/**
	 * Returns the container identifier of the memory rendering container from which the
	 * asynchronous request has been made from.  This method returns <code>null</code> if
	 * the request does not originate from a memory rendering container
	 * 
	 * @return the memory rendering container
	 */
	public IMemoryRenderingContainer getMemoryRenderingContainer();

	/**
	 * Returns the rendering of which the asynchronous request has been made from.
	 * If the request is not made from a rendering, this method returns <code>null</code>.
	 * 
	 * @return the rendering or <code>null</code>
	 */
	public IMemoryRendering getRendering();

	/**
	 * Returns if the memory view is currently pinned
	 * @return true if the Memory View is currently pinned, false otherwise.
	 */
	public boolean isPinned();

	/**
	 * Returns if the given memory block is registered with the Memory View.
	 * A memory block is registered to the view if it's added from that Memory View.
	 * 
	 * TODO:  need to review this again and see what to do about registering memory blocks
	 * 
	 * @param memoryBlock the memory block to check
	 * @return true if the memory block is registered, false otherwise.
	 */
	public boolean isMemoryBlockRegistered(IMemoryBlock memoryBlock);

}
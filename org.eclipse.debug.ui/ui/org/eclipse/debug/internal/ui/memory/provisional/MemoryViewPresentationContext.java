/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.viewers.PresentationContext;
import org.eclipse.debug.internal.ui.views.memory.MemoryView;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;

/**
 * Presentation contex from the Memory View.  This presentation provides additional
 * information regarding the originator of the asynchronous request.
 * 
 * Clients may reference or subclass from this class.
 * 
 * @since 3.2
 *
 */
public class MemoryViewPresentationContext extends PresentationContext  {

	private IMemoryRenderingContainer fContainer;	
	private IMemoryRendering fRendering;			
	private IMemoryRenderingSite fMemoryView;
	
	/**
	 * Constructs MemoryViewPresentationContext
	 * @param site - the memory rendering site that this presetation context is for
	 * @param container - the memory rendering container that this presentation context is for, may be <code>null</code>
	 * @param rendering - - the memory rendering that this presentation context is for, may be <code>null</code>
	 */
	public MemoryViewPresentationContext(IMemoryRenderingSite site, IMemoryRenderingContainer container, IMemoryRendering rendering) {
		super(site.getSite().getPart());
		
		fMemoryView = site;
		fContainer = container;
		fRendering = rendering;
	}
	
	/**
	 * Returns the memory rendering site that this presentation context is for
	 * @return the memory rendering site that this presentation context is for
	 */
	public IMemoryRenderingSite getMemoryRenderingSite()
	{
		return fMemoryView;
	}
	
	/**
	 * Returns the memory rendering container that this presentation context is for
	 * @return the memory rendering container that this presentation context is for, <code>null</code> if none.
	 */
	public IMemoryRenderingContainer getMemoryRenderingContainer()
	{
		return fContainer;
	}
	
	/**
	 * Returns the memory rendering that this presentation context is for
	 * @return the memory rendering that this presentation context is for, <code>null</code> if none.
	 */
	public IMemoryRendering getRendering()
	{
		return fRendering;
	}
	
	/**
	 * Returns true if the Memory View is pinned, false otherwise.
	 * TODO:  need to revisit to see what to do with this
	 * 
	 * @return true if the Memory View is pinned, false otherwise.
	 */
	public boolean isPinned()
	{
		if (fMemoryView instanceof MemoryView)
		{
			return ((MemoryView)fMemoryView).isPinMBDisplay();
		}
		return false;
	}
	
	/**
	 * Returns true if the memory block registered to the Memory View, false otherwise.
	 * A memory block is registered to the view if it is added from that memory view.
	 * TODO:  need to revisit to see what to do with this
	 * 
	 * @return true if the memory block registered to the Memory View, false otherwise.
	 */
	public boolean isMemoryBlockRegistered(IMemoryBlock memoryBlock)
	{
		if (fMemoryView instanceof MemoryView)
		{
			return ((MemoryView)fMemoryView).isMemoryBlockRegistered(memoryBlock);
		}
		return false;
	}
}

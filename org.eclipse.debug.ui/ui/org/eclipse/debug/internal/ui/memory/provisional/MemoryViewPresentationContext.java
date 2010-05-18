/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.memory.provisional;

import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;

/**
 * Presentation context from the Memory View.  This presentation provides additional
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
	 * @param site - the memory rendering site that this presentation context is for
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
}

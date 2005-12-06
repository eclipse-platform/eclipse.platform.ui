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
package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.ui.viewers.PresentationContext;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.ui.IWorkbenchPart;

public class MemoryViewPresentationContext extends PresentationContext {

	private String fContainerId;			// optional field to indicate which container the context is in
	private IMemoryRendering fRendering;	// optional field to indicate which rendering the context is in
	private MemoryView fMemoryView;
	
	public MemoryViewPresentationContext(IWorkbenchPart part) {
		super(part);
		
		if (part instanceof MemoryView){
			fMemoryView = (MemoryView)part;
		}
	}
	
	public void setContainerId(String paneId)
	{
		fContainerId = paneId;
	}
	
	public String getContainerId()
	{
		return fContainerId;
	}
	
	public void setRendering(IMemoryRendering rendering)
	{
		fRendering = rendering;
	}
	
	public IMemoryRendering getRendering()
	{
		return fRendering;
	}
	
	public boolean isPinMBDisplay()
	{
		if (fMemoryView != null)
		{
			return fMemoryView.isPinMBDisplay();
		}
		return false;
	}
	
	public boolean isMemoryBlockRegistered(IMemoryBlock memoryBlock)
	{
		if (fMemoryView != null)
		{
			return fMemoryView.isMemoryBlockRegistered(memoryBlock);
		}
		return false;
	}
}

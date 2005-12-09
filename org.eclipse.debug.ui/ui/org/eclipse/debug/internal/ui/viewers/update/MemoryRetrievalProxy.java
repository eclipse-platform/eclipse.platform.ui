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
 
package org.eclipse.debug.internal.ui.viewers.update;

import java.util.Iterator;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IMemoryBlockListener;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.ui.viewers.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewPresentationContext;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

public class MemoryRetrievalProxy extends AbstractModelProxy implements IMemoryBlockListener {
	private IMemoryBlockRetrieval fRetrieval;

	public MemoryRetrievalProxy(IMemoryBlockRetrieval retrieval)
	{
		fRetrieval = retrieval;
	
	}
	
	public void memoryBlocksAdded(IMemoryBlock[] memory) {
		
		ModelDelta delta = new ModelDelta(fRetrieval, IModelDelta.NOCHANGE);
		
		for (int i=0; i<memory.length; i++){
			IMemoryBlockRetrieval retrieval = (IMemoryBlockRetrieval)memory[i].getAdapter(IMemoryBlockRetrieval.class);
			if (retrieval == null)
				retrieval = memory[i].getDebugTarget();
			
			if (retrieval != null)
			{
				if (retrieval == fRetrieval)
				{
					if (toSelect(memory[i]))
						delta.addNode(memory[i], IModelDelta.ADDED | IModelDelta.SELECT);
					else
						delta.addNode(memory[i], IModelDelta.ADDED);
				}
			}
		}
		
		fireModelChanged(delta);
	}

	public void memoryBlocksRemoved(IMemoryBlock[] memory) {
		ModelDelta delta = new ModelDelta(fRetrieval, IModelDelta.NOCHANGE);
		
		// find a memory block to select
		
		for (int i=0; i<memory.length; i++){
			IMemoryBlockRetrieval retrieval = (IMemoryBlockRetrieval)memory[i].getAdapter(IMemoryBlockRetrieval.class);
			if (retrieval == null)
				retrieval = memory[i].getDebugTarget();
			
			if (retrieval != null)
			{
				if (retrieval == fRetrieval)
				{
					// do not change selection if the memory block removed is not 
					// currently selected
					if (isMemoryBlockSelected(getCurrentSelection(), memory[i]))
						addSelectDeltaNode(delta);
					delta.addNode(memory[i], IModelDelta.REMOVED);
				}
			}
		}
		
		fireModelChanged(delta);
		
	}

	public void init(IPresentationContext context) {
		super.init(context);
		DebugPlugin.getDefault().getMemoryBlockManager().addListener(this);
	}

	public synchronized void dispose() {
		super.dispose();
		DebugPlugin.getDefault().getMemoryBlockManager().removeListener(this);
	}

	private void addSelectDeltaNode(ModelDelta delta)
	{
		IMemoryBlock[] memoryBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(fRetrieval);
		if (memoryBlocks != null && memoryBlocks.length > 0)
		{
			delta.addNode(memoryBlocks[0], IModelDelta.CHANGED | IModelDelta.SELECT);
		}
	}
	
	private IStructuredSelection getCurrentSelection()
	{
		ISelection selection = getPresentationContext().getPart().getSite().getSelectionProvider().getSelection();
		
		if (selection instanceof IStructuredSelection)
			return (IStructuredSelection)selection;
		
		return null;
	}
	
	private boolean isMemoryBlockSelected(IStructuredSelection selection, IMemoryBlock memoryBlock)
	{
		if (selection != null)
		{
			Iterator iter = selection.iterator();
			while (iter.hasNext())
			{
				Object sel = iter.next();
				if (sel == memoryBlock)
					return true;
				
				if (sel instanceof IMemoryRendering)
				{
					if (((IMemoryRendering)sel).getMemoryBlock() == memoryBlock)
						return true;
				}
			}
		}
		return false;
	}

	public void setInitialState() {
		IMemoryBlock[] memoryBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(fRetrieval);
		if (memoryBlocks.length > 0)
		{
			ModelDelta delta = new ModelDelta(fRetrieval, IModelDelta.NOCHANGE);
			addSelectDeltaNode(delta);
			fireModelChanged(delta);
		}
	}
	
	private boolean toSelect(IMemoryBlock memoryBlock)
	{
		// if it's the first memory block, always select
		IMemoryBlock[] memoryBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(fRetrieval);
		if (memoryBlocks.length == 1)
			return true;
		
		if (getPresentationContext() instanceof MemoryViewPresentationContext)
		{
			// if registered, meaning the memory block is added from this view, select
			MemoryViewPresentationContext context = (MemoryViewPresentationContext)getPresentationContext();
			if (context.isMemoryBlockRegistered(memoryBlock))
				return true;
			// if display is not pinned, select
			else if (!context.isPinMBDisplay())
				return true;
		}
		return false;
	}
	
}

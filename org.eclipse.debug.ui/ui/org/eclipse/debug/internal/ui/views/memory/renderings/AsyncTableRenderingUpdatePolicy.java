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
package org.eclipse.debug.internal.ui.views.memory.renderings;

import java.math.BigInteger;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTableViewerContentManager;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.update.DefaultTableUpdatePolicy;

/**
 * This update policy updates immediately after a model changed event.  The update policy will
 * only update if the rendering is visible.  Cache from the content manager is cleared
 * when the memory block has changed when the rendering is not visible
 *
 */
class AsyncTableRenderingUpdatePolicy extends DefaultTableUpdatePolicy
{
	public void modelChanged(IModelDelta node) {
		
		// clear current cache as it becomes invalid when the memory block is changed
		AsynchronousTableViewerContentManager contentManager = getTableViewer().getContentManager();
		IContentChangeComputer computer = null;
		if (contentManager instanceof IContentChangeComputer)
			computer = (IContentChangeComputer)contentManager;
		
		if (shouldClearCache(node))
		{
			clearCache(computer);
		}
		
		if (!shouldHandleChange(node))
			return;
		
		if (node.getElement() instanceof IMemoryBlock && (node.getFlags() & IModelDelta.CONTENT) != 0)
		{
			if (computer != null && getTableViewer() != null)
			{
				// only cache if the rendering is not currently displaying error
				if (!getTableViewer().getRendering().isDisplayingError())
				{
					// cache visible elelements
					computer.cache(getTableViewer().getContentManager().getElements());
				}
			}
			
			if (node.getElement() instanceof IMemoryBlock)
			{
				// update policy figured out what's changed in the memory block
				// update content input if the base address is changed

				// if there is an error in the handling of this, do not notify rendering that
				// something is changed
				if (handleMemoryBlockChanged((IMemoryBlock)node.getElement(), node))
					notifyRendering(node);
			}
		}
		
		super.modelChanged(node);
	}

	/**
	 * @param computer
	 */
	protected void clearCache(IContentChangeComputer computer) {
		if (computer != null)
			computer.clearCache();
	}

	private void notifyRendering(IModelDelta node) {
		if (getTableViewer() != null)
		{
			IModelChangedListener listener = (IModelChangedListener)getTableViewer().getRendering().getAdapter(IModelChangedListener.class);
			if (listener != null)
				listener.modelChanged(node);
		}
	}
	
	protected boolean handleMemoryBlockChanged(IMemoryBlock mb, IModelDelta delta)
	{
		try {
			if (getViewer().getPresentationContext() instanceof TableRenderingPresentationContext)
			{
				TableRenderingContentInput input = ((TableRenderingPresentationContext)getViewer().getPresentationContext()).getInput();
				BigInteger address = getMemoryBlockBaseAddress(mb);
				if (!address.equals(input.getContentBaseAddress()))
				{
					// load at the new base address
					input.setLoadAddress(address);
					input.updateContentBaseAddress();
					getTableViewer().setTopIndex(address);
					getTableViewer().setSelection(address);
				}
				return true;
			}
		} catch (DebugException e) {
			// TODO:  hack to get the rendering to be notified of error
			if (getTableViewer() != null)
				getTableViewer().handlePresentationFailure(null, e.getStatus());
			return false;
		}
		return true;
	}
	
	private BigInteger getMemoryBlockBaseAddress(IMemoryBlock mb) throws DebugException
	{
		if (mb instanceof IMemoryBlockExtension)
			return ((IMemoryBlockExtension)mb).getBigBaseAddress();
		else
			return BigInteger.valueOf(mb.getStartAddress());
	}
	
	private AsyncTableRenderingViewer getTableViewer()
	{
		if (getViewer() instanceof AsyncTableRenderingViewer)
			return (AsyncTableRenderingViewer)getViewer();
		return null;
	}
	
	protected boolean shouldHandleChange(IModelDelta delta)
	{
		if (getViewer().getPresentationContext() instanceof TableRenderingPresentationContext)
		{
			TableRenderingPresentationContext context = (TableRenderingPresentationContext) getViewer().getPresentationContext();
			if (context.getRendering() instanceof AbstractAsyncTableRendering)
			{
				AbstractAsyncTableRendering rendering = (AbstractAsyncTableRendering)context.getRendering();
				if (!rendering.isVisible())
					return false;
			}
		}
		return true;
	}
	
	protected boolean shouldClearCache(IModelDelta delta)
	{
		return true;
	}
}
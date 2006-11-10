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

package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.ui.viewers.AbstractUpdatePolicy;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.PartPresentationContext;
import org.eclipse.debug.internal.ui.viewers.TreeUpdatePolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.swt.widgets.Composite;

/**
 * Customized tree viewer for the Memory View
 * This Tree Viewer has a specialized update policy for the memory view.
 * When the model fires a ADDED delta, the update policy handles the event as follows:
 * If the ADDED delta is accompanied by SELECT, and the added element is an memory blok, then
 * the udpate policy asks the Memory View if the it is currently pinned to a memory block.  If the view
 * is currently pinned, then the SELECT delta is ignored.
 * 
 * If the ADDED delta and SELECT delta are recieved in separate nodes, then the delta will be handled as-is and would
 * not take the pinning state of the memory view into account. 
 *
 */
public class MemoryViewTreeViewer extends AsynchronousTreeViewer {
	
	private class MemoryViewTreeUpdatePolicy extends TreeUpdatePolicy
	{
		protected void updateNodes(IModelDelta[] nodes) {                          
	        AsynchronousTreeViewer viewer = (AsynchronousTreeViewer) getViewer();
	        if (viewer == null) {
	            return;
	        }

	        for (int i = 0; i < nodes.length; i++) {
	            IModelDelta node = nodes[i];
	            int flags = node.getFlags();
	            
	            if ((flags & IModelDelta.ADDED) != 0)
	           {
	               if (node.getElement() instanceof IMemoryBlock)
	               {
		        	   if ((flags & IModelDelta.SELECT) != 0)
		        	   {
		        		   PartPresentationContext context = (PartPresentationContext) getViewer().getPresentationContext();
		        		   if (context.getPart() instanceof MemoryView)
		        		   {
		        			   MemoryView view = (MemoryView)context.getPart();
		        			   if (view.isPinMBDisplay())
		        			   {
		        				   // turn off select if the view is currently pinned
		        				   flags |= IModelDelta.SELECT;
		        				   flags ^= IModelDelta.SELECT;
		        			   }
		        		   }
		        	   }
		        	   
		        	   // override and select the first memory block
		     		   if (isFirstMemoryBlock())
		    		   {
		    			   flags |= IModelDelta.SELECT;
		    		   }
	               }
	           }

	            if ((flags & IModelDelta.ADDED) != 0) {
	                handleAdd(viewer, node);
	            }
	            if ((flags & IModelDelta.REMOVED) != 0) {
	                handleRemove(viewer, node);
	            }
	            if ((flags & IModelDelta.CONTENT) != 0) {
	                handleContent(viewer, node);
	            }
	            if ((flags & IModelDelta.EXPAND) != 0) {
	                handleExpand(viewer, node);
	            }
	            if ((flags & IModelDelta.SELECT) != 0) {
	                handleSelect(viewer, node);
	            }
	            if ((flags & IModelDelta.STATE) != 0) {
	                handleState(viewer, node);
	            }
	            if ((flags & IModelDelta.INSERTED) != 0) {
	            }
	            if ((flags & IModelDelta.REPLACED) != 0) {
	            }

	            updateNodes(node.getChildDeltas());
	        }
		}
	}

	public MemoryViewTreeViewer(Composite parent) {
		super(parent);
	}

	public AbstractUpdatePolicy createUpdatePolicy() {
		return new MemoryViewTreeUpdatePolicy();
	}
	
	private boolean isFirstMemoryBlock()
	{
		if (getInput() instanceof IMemoryBlockRetrieval)
		{
			IMemoryBlock[] memoryBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks((IMemoryBlockRetrieval)getInput());
			if (memoryBlocks.length == 1)
				return true;
		}
		return false;
	}

}

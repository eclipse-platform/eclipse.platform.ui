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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IAddMemoryBlocksTarget;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * This is the retargettable add memory block action in the Memory View.
 * All AddMemoryBlock actions in the view will use this action to make sure
 * that clients can override the "Add Memory Monitor" dialog. 
 *
 */
public class RetargetAddMemoryBlockAction extends AddMemoryBlockAction {

	public RetargetAddMemoryBlockAction(IMemoryRenderingSite site)
	{
		super(site);
	}
	
	public RetargetAddMemoryBlockAction(IMemoryRenderingSite site, boolean addDefaultRenderings)
	{
		super(site, addDefaultRenderings);
	}
	
	public RetargetAddMemoryBlockAction(String text, int style, IMemoryRenderingSite site)
	{
		super(text, style, site);
	}

	public void run() {
		//	get current selection from Debug View
		ISelection selection = fSite.getSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
		IAddMemoryBlocksTarget target = getAddMemoryBlocksTarget(selection);
		
		if (target != null)
		{
			try {
				if (target.supportsAddMemoryBlocks(getMemoryView()))
				{
					IMemoryBlockRetrieval standardMemRetrieval = getMemoryBlockRetrieval(selection);
					target.addMemoryBlocks(getMemoryView(), getMemoryView().getSite().getSelectionProvider().getSelection(), standardMemRetrieval);
				}
				else
					super.run();
			} catch (CoreException e) {
				DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), ActionMessages.RetargetAddMemoryBlockAction_0, ActionMessages.RetargetAddMemoryBlockAction_1, e);
			}
		}
		else	
		{
			super.run();
		}
	}

	protected void updateAction(ISelection debugContext) {
		
		try {
			IAddMemoryBlocksTarget target = getAddMemoryBlocksTarget(debugContext);
			
			if (target != null)
			{
				if (target.supportsAddMemoryBlocks(getMemoryView()))
				{
					IMemoryBlockRetrieval standardMemRetrieval = getMemoryBlockRetrieval(debugContext);
					if (getMemoryView().getSite().getSelectionProvider() != null)
						setEnabled(target.canAddMemoryBlocks(getMemoryView(), getMemoryView().getSite().getSelectionProvider().getSelection(), standardMemRetrieval));
					else
						super.updateAction(debugContext);
				}
				else
					super.updateAction(debugContext);
			}
			else
			{
				super.updateAction(debugContext);
			}
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}
	}
	
	private IAddMemoryBlocksTarget getAddMemoryBlocksTarget(ISelection debugContext)
	{
		IMemoryBlockRetrieval standardMemRetrieval = getMemoryBlockRetrieval(debugContext);
		
		if (standardMemRetrieval == null)
			return null;
		
		IAddMemoryBlocksTarget target = null;
		
		if (standardMemRetrieval instanceof IAddMemoryBlocksTarget)
		{
			target = (IAddMemoryBlocksTarget) standardMemRetrieval;
		}
		else if (standardMemRetrieval instanceof IAdaptable)
		{
			target = (IAddMemoryBlocksTarget)((IAdaptable)standardMemRetrieval).getAdapter(IAddMemoryBlocksTarget.class);
		}
		return target;
	}
	
	private IMemoryBlockRetrieval getMemoryBlockRetrieval(ISelection debugContext)
	{
		if (!(debugContext instanceof IStructuredSelection))
		{
			return null;
		}
		
		Object elem = ((IStructuredSelection)debugContext).getFirstElement();
		if (!(elem instanceof IDebugElement))
		{
			return null;
		}
		
		// ask debug element about memeory retrieval
		IDebugTarget debugTarget = ((IDebugElement)elem).getDebugTarget();
		IMemoryBlockRetrieval standardMemRetrieval = (IMemoryBlockRetrieval)((IDebugElement)elem).getAdapter(IMemoryBlockRetrieval.class);
		if (standardMemRetrieval == null)
		{
			// if getAdapter returns null, assume debug target as memory block retrieval
			standardMemRetrieval = debugTarget;
		}
		if (standardMemRetrieval == null)
		{
			return null;
		}
		
		return standardMemRetrieval;
	}
}

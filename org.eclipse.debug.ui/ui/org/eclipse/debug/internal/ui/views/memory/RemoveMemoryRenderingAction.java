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

package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.core.memory.IMemoryRendering;
import org.eclipse.debug.internal.core.memory.MemoryBlockManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;


/**
 * Remove Memory Rendering action
 * This action serves two purposes:
 *   - remove memory rendering from Memory Rendering Pane
 *   - quck way to remove a memory block from Memory Rendering Pane
 * 
 * When user clicks on the this tool bar action, it simply removes
 * the top view tab from Memory Rendering Pane.
 */
public class RemoveMemoryRenderingAction extends AbstractMemoryAction
{
	
	private static final String PREFIX = "RemoveMemoryRenderingAction."; //$NON-NLS-1$
	private static final String TITLE = PREFIX + "title"; //$NON-NLS-1$
	private static final String REMOVE_MONITOR = PREFIX + "remove_block"; //$NON-NLS-1$
	private static final String UNKNOWN = PREFIX + "Unknown"; //$NON-NLS-1$
	
	public RemoveMemoryRenderingAction()
	{
		// create action as drop down
		super(DebugUIMessages.getString("RemoveMemoryRenderingAction.Remove_rendering"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setText(DebugUIMessages.getString("RemoveMemoryRenderingAction.Remove_rendering")); //$NON-NLS-1$

		setToolTipText(DebugUIMessages.getString("RemoveMemoryRenderingAction.Remove_rendering")); //$NON-NLS-1$
		setToolTipText(DebugUIMessages.getString("RemoveMemoryRenderingAction.Remove_rendering"));  //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_MEMORY));	
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_REMOVE_MEMORY));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_MEMORY));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		
		// user has click on the RemoveMemoryRendering button
		IMemoryViewTab topTab = getViewTab();
		
		if (topTab != null)
		{
			// get  memory block from tob view tab
			IMemoryBlock mem = topTab.getMemoryBlock();
			
			// get rendering id
			String renderingId = topTab.getRenderingId();
			IMemoryRendering rendering = topTab.getRendering();
			
			if (rendering != null)
			{
				// remove from Memory Rendering Manager
				MemoryBlockManager.getMemoryRenderingManager().removeMemoryBlockRendering(rendering);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.defaultrenderings.internal.actions.AbstractMemoryAction#getViewTab()
	 */
	IMemoryViewTab getViewTab() {
		String viewId = IInternalDebugUIConstants.ID_MEMORY_VIEW;
		//	open a new view if necessary
		IWorkbenchPage p= DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (p == null) {
			return null;
		}
		IViewPart view = null;
		view= p.findView(viewId);
		
		if (view == null) {
			try {
				IWorkbenchPart activePart= p.getActivePart();
				view= p.showView(viewId);
				p.activate(activePart);
			} catch (PartInitException e) {
				return null;
			}		
			
		}
		
		if (view instanceof IMultipaneMemoryView)
		{
			IMemoryViewTab topTap = ((IMultipaneMemoryView)view).getTopMemoryTab(RenderingViewPane.RENDERING_VIEW_PANE_ID);
			
			return topTap;
		}
		else if (view instanceof IMemoryView)
		{
			IMemoryViewTab topTap = ((IMemoryView)view).getTopMemoryTab();
			return topTap;
		}
		else
		{
			return null;
		}
	}		
}

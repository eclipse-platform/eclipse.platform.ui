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

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;


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
	private IMemoryViewPane fViewPane;
	public RemoveMemoryRenderingAction(IMemoryViewPane viewPane)
	{
		// create action as drop down
		super(DebugUIMessages.getString("RemoveMemoryRenderingAction.Remove_rendering"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setText(DebugUIMessages.getString("RemoveMemoryRenderingAction.Remove_rendering")); //$NON-NLS-1$

		setToolTipText(DebugUIMessages.getString("RemoveMemoryRenderingAction.Remove_rendering")); //$NON-NLS-1$
		setToolTipText(DebugUIMessages.getString("RemoveMemoryRenderingAction.Remove_rendering"));  //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_MEMORY));	
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_REMOVE_MEMORY));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_MEMORY));
		fViewPane = viewPane;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		
		// user has click on the RemoveMemoryRendering button
		IMemoryViewTab topTab = getViewTab();
		
		if (topTab != null)
		{
			IMemoryRendering rendering = topTab.getRendering();
			
			if (rendering != null)
			{
				// remove from Memory Rendering Manager
				if (fViewPane instanceof IRenderingViewPane)
					((IRenderingViewPane)fViewPane).removeMemoryRendering(rendering);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.defaultrenderings.internal.actions.AbstractMemoryAction#getViewTab()
	 */
	IMemoryViewTab getViewTab() {
		if (fViewPane instanceof IMemoryView)
		{
			return ((IMemoryView)fViewPane).getTopMemoryTab();
		}
		return null;
	}		
}

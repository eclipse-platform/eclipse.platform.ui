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
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Remove Memory Block Action from Memory View
 * 
 * @since 3.0
 */
public class RemoveMemoryBlockAction extends AbstractMemoryAction {
	
	
	public RemoveMemoryBlockAction()
	{
		setText(DebugUIMessages.getString("RemoveMemoryBlockAction.title")); //$NON-NLS-1$

		setToolTipText(DebugUIMessages.getString("RemoveMemoryBlockAction.tooltip")); //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_MEMORY));	
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_REMOVE_MEMORY));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_MEMORY));
		WorkbenchHelp.setHelp(this, IDebugUIConstants.PLUGIN_ID + ".RemoveMemoryBlockAction_context"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		
		if (getViewTab() == null)
			return;
		
		// get top view tab
		IMemoryViewTab topTab = getViewTab();
		
		// remove memory block from memory block manager
		if (topTab != null)
		{
			IMemoryBlock mem = topTab.getMemoryBlock();
			MemoryViewUtil.getMemoryBlockManager().removeMemoryBlocks(new IMemoryBlock[]{mem});
		}
	}	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.internal.actions.AbstractMemoryAction#getViewTab()
	 */
	IMemoryViewTab getViewTab() {
		
		String viewId = IInternalDebugUIConstants.ID_MEMORY_VIEW;
		
		return super.getTopViewTabFromView(viewId);
	}
}

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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;


/**
 * Reest MemoryViewTab to the base address of a memory block
 * 
 * @since 3.0
 */
public class ResetMemoryBlockAction extends AbstractMemoryAction
{
	private static final String PREFIX = "ResetMemoryBlockAction."; //$NON-NLS-1$
	private static final String TITLE = PREFIX + "title"; //$NON-NLS-1$
	private static final String TOOLTIP = PREFIX + "tootip"; //$NON-NLS-1$
	private static final String FAILED_TO_RESET = PREFIX + "Failed_to_Reset"; //$NON-NLS-1$
	private static final String FAILED_TO_RESET_TO_BASE_ADD = PREFIX + "Failed_to_reset_to_base_address"; //$NON-NLS-1$


	public ResetMemoryBlockAction()
	{
		setText(DebugUIMessages.getString(TITLE));
		setToolTipText(DebugUIMessages.getString(TOOLTIP));
		
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_RESET_MEMORY));	
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_RESET_MEMORY));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_RESET_MEMORY));	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{
		try
		{
			IMemoryViewTab top = getViewTab();
			
			if (top != null)
			{
				top.resetAtBaseAddress();
			}
		}
		catch (DebugException e)
		{
			MemoryViewUtil.openError(DebugUIMessages.getString(FAILED_TO_RESET), 
					DebugUIMessages.getString(FAILED_TO_RESET_TO_BASE_ADD), e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.internal.actions.AbstractMemoryAction#getViewTab()
	 */
	IMemoryViewTab getViewTab()
	{
		return getTopViewTabFromView(IInternalDebugUIConstants.ID_MEMORY_VIEW);
	}

}

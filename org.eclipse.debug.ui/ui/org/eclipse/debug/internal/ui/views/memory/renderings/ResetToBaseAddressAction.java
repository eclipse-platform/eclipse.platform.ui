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


package org.eclipse.debug.internal.ui.views.memory.renderings;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.memory.AbstractTableRendering;
import org.eclipse.jface.action.Action;


/**
 * Reest MemoryViewTab to the base address of a memory block
 * 
 * @since 3.0
 */
public class ResetToBaseAddressAction extends Action
{
	private static final String PREFIX = "ResetMemoryBlockAction."; //$NON-NLS-1$
	private static final String TITLE = PREFIX + "title"; //$NON-NLS-1$
	private static final String TOOLTIP = PREFIX + "tootip"; //$NON-NLS-1$

	private AbstractTableRendering fRendering;
	
	public ResetToBaseAddressAction(AbstractTableRendering rendering)
	{
		fRendering = rendering;
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
		fRendering.reset();
	}
}

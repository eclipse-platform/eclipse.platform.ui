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

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;


/**
 * Action for formatting Memory View Tab
 * 
 * @since 3.0
 */
public class FormatColumnAction extends Action
{
	private static final String PREFIX = "FormatColumnAction."; //$NON-NLS-1$
	private static final String BYTE = PREFIX + "byte"; //$NON-NLS-1$
	private static final String BYTES = PREFIX + "bytes"; //$NON-NLS-1$
	
	ITableMemoryViewTab fViewTab;
	int fNumBytesPerCol;
	
	public FormatColumnAction(int numBytes, ITableMemoryViewTab viewTab)
	{
		super();
		
		String label;
		if (numBytes > 1)
			label = String.valueOf(numBytes) + " " + DebugUIMessages.getString(BYTES); //$NON-NLS-1$
		else	
			label = String.valueOf(numBytes) + " " + DebugUIMessages.getString(BYTE); //$NON-NLS-1$
		
		super.setText(label);
		
		fViewTab = viewTab;	
		
		// check this action if the view tab is currently in this format
		if (numBytes == fViewTab.getColumnSize())
		{
			setChecked(true);
		}

		fNumBytesPerCol = numBytes;
		
		WorkbenchHelp.setHelp(this, IDebugUIConstants.PLUGIN_ID + ".FormatColumnAction_context"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{
		fViewTab.format(fViewTab.getBytesPerLine(), fNumBytesPerCol);
	}
	
	public int getColumnSize()
	{
		return fNumBytesPerCol;
	}

}

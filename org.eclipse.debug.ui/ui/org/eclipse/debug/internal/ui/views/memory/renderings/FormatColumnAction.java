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

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.AbstractTableRendering;
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
	private static final String UNIT = PREFIX + "unit"; //$NON-NLS-1$
	private static final String UNITS = PREFIX + "units"; //$NON-NLS-1$
	
	AbstractTableRendering fRendering;
	int fNumBytesPerCol;
	
	public FormatColumnAction(int numUnits, int addressibleSize, AbstractTableRendering rendering)
	{	
		String label;
		if (numUnits == 1)
			label = String.valueOf(numUnits) + " " + DebugUIMessages.getString(UNIT); //$NON-NLS-1$
		else	
			label = String.valueOf(numUnits) + " " + DebugUIMessages.getString(UNITS); //$NON-NLS-1$
		
		super.setText(label);
		
		fRendering = rendering;	
		
		// check this action if the view tab is currently in this format
		if (numUnits*addressibleSize == fRendering.getBytesPerColumn())
		{
			setChecked(true);
		}

		fNumBytesPerCol = numUnits*addressibleSize;
		
		WorkbenchHelp.setHelp(this, IDebugUIConstants.PLUGIN_ID + ".FormatColumnAction_context"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{
		fRendering.format(fRendering.getBytesPerLine(), fNumBytesPerCol);
	}
	
	public int getColumnSize()
	{
		return fNumBytesPerCol;
	}

}

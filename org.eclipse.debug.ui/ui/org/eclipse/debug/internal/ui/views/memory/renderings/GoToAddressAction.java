/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.AbstractTableRendering;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


/**
 * Go To Address Action for "MemoryViewTab"
 * 
 * @since 3.0
 */
public class GoToAddressAction extends Action
{
	private AbstractTableRendering fRendering;
	
	private static final String PREFIX = "GoToAddressAction."; //$NON-NLS-1$
	private static final String TITLE = PREFIX + "title"; //$NON-NLS-1$
	private static final String GO_TO_ADDRESS_FAILED = PREFIX + "Go_to_address_failed"; //$NON-NLS-1$
	private static final String ADDRESS_IS_INVALID = PREFIX + "Address_is_invalid"; //$NON-NLS-1$
	private static final String TOOLTIP = PREFIX + "tooltip"; //$NON-NLS-1$
	
	public GoToAddressAction(AbstractTableRendering rendering)
	{		
		super(DebugUIMessages.getString(TITLE));
		setToolTipText(DebugUIMessages.getString(TOOLTIP));
		
		fRendering = rendering;
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugUIConstants.PLUGIN_ID + ".GoToAddressAction_context"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{
		try
		{	
			Shell shell= DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
		
			// create dialog to ask for expression/address to block
			GoToAddressDialog dialog = new GoToAddressDialog(shell);
			dialog.open();
		
			int returnCode = dialog.getReturnCode();
		
			if (returnCode == Window.CANCEL)
			{
				return;
			}
		
			// get expression from dialog
			String expression = dialog.getExpression();
			
			expression = expression.toUpperCase();
			expression = expression.trim();
			
			if (expression.startsWith("0X")) //$NON-NLS-1$
			{
				expression = expression.substring(2);
			}
			
			// convert expression to address
			BigInteger address = new BigInteger(expression, 16);
			
			// go to specified address
			fRendering.goToAddress(address);
		}
		// open error in case of any error
		catch (DebugException e)
		{
			MemoryViewUtil.openError(DebugUIMessages.getString(GO_TO_ADDRESS_FAILED), 
				DebugUIMessages.getString(GO_TO_ADDRESS_FAILED), e);
		}
		catch (NumberFormatException e1)
		{
			MemoryViewUtil.openError(DebugUIMessages.getString(GO_TO_ADDRESS_FAILED), 
				DebugUIMessages.getString(ADDRESS_IS_INVALID), null);
		}
	}

}

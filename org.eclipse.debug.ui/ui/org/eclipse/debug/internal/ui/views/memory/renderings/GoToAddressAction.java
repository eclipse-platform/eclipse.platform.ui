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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.memory.MemoryView;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
	private AbstractBaseTableRendering fRendering;
	
	public GoToAddressAction(AbstractBaseTableRendering rendering)
	{		
		super(DebugUIMessages.GoToAddressAction_title);
		setToolTipText(DebugUIMessages.GoToAddressAction_title);
		
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
			
			// look at this address and figure out if a new memory block should
			// be opened.
			IMemoryBlock mb = fRendering.getMemoryBlock();
			if (mb instanceof IMemoryBlockExtension)
			{
				IMemoryBlockExtension mbExt = (IMemoryBlockExtension)mb;
				BigInteger mbStart = mbExt.getMemoryBlockStartAddress();
				BigInteger mbEnd = mbExt.getMemoryBlockEndAddress();
				
				if (mbStart != null)
				{
					// if trying to go beyond the start address
					// of the memory block
					if (address.compareTo(mbStart) < 0)
					{
						IMemoryBlockRetrievalExtension retrieval = (IMemoryBlockRetrievalExtension)mbExt.getAdapter(IMemoryBlockRetrievalExtension.class);
						IDebugTarget dt = mbExt.getDebugTarget();
						
						if (retrieval == null && dt instanceof IMemoryBlockRetrievalExtension)
							retrieval = (IMemoryBlockRetrievalExtension)dt;
						
						// add a new memory block and then the same rendering as fRendering
						// in the same container.
						if (retrieval != null)
						{
							addNewMemoryBlock(expression, retrieval);
							return;
						}
					}
				}
				if (mbEnd != null)
				{
					// if trying to go beyond the end address
					// of the memory block
					if (address.compareTo(mbEnd) > 0)
					{
						IMemoryBlockRetrievalExtension retrieval = (IMemoryBlockRetrievalExtension)mbExt.getAdapter(IMemoryBlockRetrievalExtension.class);
						IDebugTarget dt = mbExt.getDebugTarget();
						
						if (retrieval == null && dt instanceof IMemoryBlockRetrievalExtension)
							retrieval = (IMemoryBlockRetrievalExtension)dt;
						
						// add a new memory block and then the same rendering as fRendering
						// in the same container.
						if (retrieval != null)
						{
							addNewMemoryBlock(expression, retrieval);
							return;
						}
					}
				}
			}
			
			// go to specified address
			fRendering.goToAddress(address);
		}
		// open error in case of any error
		catch (DebugException e)
		{
			MemoryViewUtil.openError(DebugUIMessages.GoToAddressAction_Go_to_address_failed, 
				DebugUIMessages.GoToAddressAction_Go_to_address_failed, e);
		}
		catch (NumberFormatException e1)
		{
			MemoryViewUtil.openError(DebugUIMessages.GoToAddressAction_Go_to_address_failed, 
				DebugUIMessages.GoToAddressAction_Address_is_invalid, null);
		}
	}
	
	private void addNewMemoryBlock(String expression, IMemoryBlockRetrievalExtension retrieval)
	{
		ISelection selection = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
		Object elem = ((IStructuredSelection)selection).getFirstElement();
		
		if (!(elem instanceof IDebugElement))
			return;
		 
		try {
			if (retrieval != null)
			{
				// save the pin setting in the parent view
				IMemoryRenderingSite site = fRendering.getMemoryRenderingContainer().getMemoryRenderingSite();
				MemoryView view = null;
				if (site instanceof MemoryView)
				{
					view = (MemoryView)site;
				}
				
				IMemoryBlockExtension mbext = retrieval.getExtendedMemoryBlock(expression, elem);
				if (mbext != null)
				{
					IMemoryBlock[] memArray = new IMemoryBlock[]{mbext};
					if (view != null)
						view.registerMemoryBlocks(memArray);
					DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks(memArray);
				}
				
				IMemoryRenderingType renderingType = DebugUITools.getMemoryRenderingManager().getRenderingType(fRendering.getRenderingId());
				
				if (renderingType != null)
				{
					IMemoryRendering rendering = renderingType.createRendering();
					
					if (rendering != null)
					{
						rendering.init(fRendering.getMemoryRenderingContainer(), mbext);
						fRendering.getMemoryRenderingContainer().addMemoryRendering(rendering);
					}
				}
			}
		} catch (DebugException e) {
			MemoryViewUtil.openError(DebugUIMessages.GoToAddressAction_Go_to_address_failed, 
			DebugUIMessages.GoToAddressAction_Go_to_address_failed, e);
		} catch (CoreException e)
		{
			MemoryViewUtil.openError(DebugUIMessages.GoToAddressAction_Go_to_address_failed, 
			DebugUIMessages.GoToAddressAction_Go_to_address_failed, e);
		}
	}
}

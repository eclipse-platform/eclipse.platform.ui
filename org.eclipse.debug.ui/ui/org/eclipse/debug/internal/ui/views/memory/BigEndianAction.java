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

import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.internal.core.memory.IMemoryRendering;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Show integers in Big Integer
 */
public class BigEndianAction implements IObjectActionDelegate {

	IWorkbenchPart fTargetPart;
	IMemoryRendering fRendering;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fTargetPart = targetPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {

		if (fRendering instanceof IntegerRendering)
		{	
			IntegerRendering intRendering = (IntegerRendering)fRendering;
			
			if (intRendering.getCurrentEndianess() != RendererUtil.BIG_ENDIAN){
				
				intRendering.setCurrentEndianess(RendererUtil.BIG_ENDIAN);
				
				if (fTargetPart instanceof IMemoryView)
				{	
					IMemoryViewTab top = ((IMemoryView)fTargetPart).getTopMemoryTab();
					
					top.refresh();
				}
				else if (fTargetPart instanceof IMultipaneMemoryView)
				{
					IMemoryViewTab top = ((IMultipaneMemoryView)fTargetPart).getTopMemoryTab(RenderingViewPane.RENDERING_VIEW_PANE_ID);
					top.refresh();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection)
		{	
			IStructuredSelection strucSelection = (IStructuredSelection)selection;
			
			if(strucSelection.getFirstElement() instanceof IMemoryRendering){
				fRendering = (IMemoryRendering)strucSelection.getFirstElement();
				
				if (fRendering.getBlock() instanceof IMemoryBlockExtension)
					action.setEnabled(false);
				else
					action.setEnabled(true);
				
				if (fRendering instanceof IntegerRendering){
					int endianess = ((IntegerRendering)fRendering).getCurrentEndianess();
					
					if(endianess == RendererUtil.BIG_ENDIAN)
						action.setChecked(true);
					else
						action.setChecked(false);
				}
			}
		}
	}
}

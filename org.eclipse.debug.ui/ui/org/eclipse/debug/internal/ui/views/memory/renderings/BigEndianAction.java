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

import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Show integers in Big Endian
 */
public class BigEndianAction implements IObjectActionDelegate {

	AbstractIntegerRendering fRendering;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {

		if (fRendering == null)
			return;
		
		if (fRendering.getCurrentEndianess() != RenderingsUtil.BIG_ENDIAN){
			fRendering.setCurrentEndianess(RenderingsUtil.BIG_ENDIAN);
			fRendering.refresh();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection)
		{	
			IStructuredSelection strucSelection = (IStructuredSelection)selection;
			
			if(strucSelection.getFirstElement() instanceof AbstractIntegerRendering){
				fRendering = (AbstractIntegerRendering)strucSelection.getFirstElement();
				
				if (fRendering.getMemoryBlock() instanceof IMemoryBlockExtension)
					action.setEnabled(false);
				else
					action.setEnabled(true);
				

				int endianess = fRendering.getCurrentEndianess();
				
				if(endianess == RenderingsUtil.BIG_ENDIAN)
					action.setChecked(true);
				else
					action.setChecked(false);
			}
		}
	}
}

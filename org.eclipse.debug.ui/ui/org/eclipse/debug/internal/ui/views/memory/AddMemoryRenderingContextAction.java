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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;


public class AddMemoryRenderingContextAction implements IViewActionDelegate {

	private IMultipaneMemoryView fMemoryView;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		
		if (view instanceof IMultipaneMemoryView)
		{
			fMemoryView = (IMultipaneMemoryView)view;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
	
		if (fMemoryView == null)
			return;
		
		IMemoryViewPane[] viewPanes = fMemoryView.getViewPanes();
		String actionId = action.getId();
		IMemoryViewPane selectedPane = null;
		
		for (int i=0; i<viewPanes.length; i++)
		{
			if (actionId.indexOf(viewPanes[i].getPaneId()) != -1)
			{
				selectedPane = viewPanes[i];
				break;
			}
		}
		
		if (selectedPane == null)
			return;
		
		AddMemoryRenderingAction addAction = new AddMemoryRenderingAction(selectedPane);
		addAction.run();
		
		addAction.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}

}

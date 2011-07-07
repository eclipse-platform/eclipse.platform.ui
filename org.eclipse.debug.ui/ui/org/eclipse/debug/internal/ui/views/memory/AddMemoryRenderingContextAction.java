/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;


public class AddMemoryRenderingContextAction implements IViewActionDelegate {

	private IMemoryRenderingSite fMemoryView;
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		
		if (view instanceof IMemoryRenderingSite)
		{
			fMemoryView = (IMemoryRenderingSite)view;			
		}				
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
	
		if (fMemoryView == null)
			return;
				
		IMemoryRenderingContainer container = getRenderingContainer(action);
		if (container != null)
		{
			AddMemoryRenderingAction addAction = new AddMemoryRenderingAction(container);
			addAction.run();		
			addAction.dispose();
		}
	}

	/**
	 * @param action the action to find the view pane for
	 * @return the first container that has the given action in it
	 */
	private IMemoryRenderingContainer getRenderingContainer(IAction action) {
		IMemoryRenderingContainer[] viewPanes = fMemoryView.getMemoryRenderingContainers();
		String actionId = action.getId();
		IMemoryRenderingContainer selectedPane = null;
		
		for (int i=0; i<viewPanes.length; i++)
		{
			if (actionId.indexOf(viewPanes[i].getId()) != -1)
			{
				selectedPane = viewPanes[i];
				break;
			}
		}
		
		return selectedPane;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		IMemoryRenderingContainer container = getRenderingContainer(action);
		if (container instanceof RenderingViewPane)
		{
			if (!((RenderingViewPane)container).canAddRendering())
				action.setEnabled(false);
			else
				action.setEnabled(true);
		}
	}

}

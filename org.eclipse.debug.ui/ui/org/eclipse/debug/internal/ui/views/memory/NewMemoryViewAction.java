/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * Action for opening a new memory view.
 */
public class NewMemoryViewAction implements IViewActionDelegate {

	private MemoryView fView;
	public void init(IViewPart view) {
		if (view instanceof MemoryView)
			fView = (MemoryView)view;                                                                                                                                                                                                                                                                                                                                               
	}

	public void run(IAction action) {
		
		String secondaryId = MemoryViewIdRegistry.getUniqueSecondaryId(IInternalDebugUIConstants.ID_MEMORY_VIEW);
		try {
			IWorkbenchPage page = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart newView = page.showView(IInternalDebugUIConstants.ID_MEMORY_VIEW, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
			
			setInitialViewSettings(newView);
		} catch (PartInitException e) {
			// if view cannot be opened, open error
			DebugUIPlugin.log(e);
		}
	}

	private void setInitialViewSettings(IViewPart newView) {
		if (fView != null && newView instanceof MemoryView)
		{
			MemoryView newMView = (MemoryView)newView;
			IMemoryViewPane[] viewPanes = fView.getViewPanes();
			for (int i=0; i<viewPanes.length; i++)
			{
				// copy view pane visibility
				newMView.showViewPane(fView.isViewPaneVisible(viewPanes[i].getId()), viewPanes[i].getId());
			}
			
			// do not want to copy renderings as it could be very expensive
			// create a blank view and let user creates renderings as needed
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

}

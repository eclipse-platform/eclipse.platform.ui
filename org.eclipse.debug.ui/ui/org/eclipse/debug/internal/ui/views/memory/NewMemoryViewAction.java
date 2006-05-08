/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
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
		
		String secondaryId = MemoryViewIdRegistry.getUniqueSecondaryId(IDebugUIConstants.ID_MEMORY_VIEW);
		try {
			IWorkbenchPage page = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart newView = page.showView(IDebugUIConstants.ID_MEMORY_VIEW, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
			
			// set initial selection for new view
			setInitialSelection(newView);
			setInitialViewSettings(newView);
			
		} catch (PartInitException e) {
			// if view cannot be opened, open error
			DebugUIPlugin.log(e);
		}
	}

	private void setInitialSelection(IViewPart newView) {
		ISelection selection = fView.getSite().getSelectionProvider().getSelection();
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection strucSel = (IStructuredSelection)selection;
			
			// return if current selection is empty
			if (strucSel.isEmpty())
				return;
			
			Object obj = strucSel.getFirstElement();
			
			if (obj == null)
				return;
			
			if (obj instanceof IMemoryRendering)
			{
				IMemoryBlock memBlock = ((IMemoryRendering)obj).getMemoryBlock();
				strucSel = new StructuredSelection(memBlock);
				newView.getSite().getSelectionProvider().setSelection(strucSel);
			}
			else if (obj instanceof IMemoryBlock)
			{
				newView.getSite().getSelectionProvider().setSelection(strucSel);
			}
		}
	}

	private void setInitialViewSettings(IViewPart newView) {
		if (fView != null && newView instanceof MemoryView)
		{
			MemoryView newMView = (MemoryView)newView;
			IMemoryViewPane[] viewPanes = fView.getViewPanes();
			int orientation = fView.getViewPanesOrientation();
			for (int i=0; i<viewPanes.length; i++)
			{
				// copy view pane visibility
				newMView.showViewPane(fView.isViewPaneVisible(viewPanes[i].getId()), viewPanes[i].getId());
			}
			
			// do not want to copy renderings as it could be very expensive
			// create a blank view and let user creates renderings as needed
			
			// set orientation of new view
			newMView.setViewPanesOrientation(orientation);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

}

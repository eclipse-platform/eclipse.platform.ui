/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.internal.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * The action for editing the source lookup path. Brings up the 
 * EditSourceLookupPathDialog.
 * 
 * @since 3.0
 */
public class EditSourceLookupPathAction implements IViewActionDelegate {
	
	private IDebugView fLaunchView;	
	
	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		if ((view == null) || !(view instanceof IDebugView))
			return;
		else
			fLaunchView = (IDebugView) view;
	}
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		ISourceLocator locator = null;		
		Object selectedObject = null;
		ISelection selection =
			fLaunchView.getViewSite().getSelectionProvider().getSelection();
		if (selection != null
				&& !selection.isEmpty()
				&& selection instanceof IStructuredSelection)
			selectedObject =
				((IStructuredSelection) selection).getFirstElement();
		else if (selection != null) {
			selectedObject = selection;
		} else
		{ //should never happen - action should not be visible in this case
			action.setEnabled(false);
			return; 
		}
		if (selectedObject instanceof ILaunch) {
			locator = ((ILaunch) selectedObject).getSourceLocator();			
		} else if (selectedObject instanceof IDebugElement) {
			locator =
				((IDebugElement) selectedObject).getLaunch().getSourceLocator();			
		}
		if (locator == null || !(locator instanceof AbstractSourceLookupDirector))
		{	//should never happen - action should not be visible in this case
			action.setEnabled(false);
			return; 
		}
		Shell shell = DebugUIPlugin.getShell();
		
		final EditSourceLookupPathDialog dialog =
			new EditSourceLookupPathDialog(shell, (AbstractSourceLookupDirector)locator);
		
		dialog.open();		
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		//do nothing.  plugin.xml will take care of it
	}
	
	
}

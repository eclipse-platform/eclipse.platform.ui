/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.sourcelookup.SourceLookupDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * The action for editing the source lookup path. Brings up the 
 * EditSourceLookupPathDialog.
 * 
 * @since 3.0
 */
public class EditSourceLookupPathAction extends SelectionListenerAction {
	
	private ISourceLookupDirector director = null;
	private LaunchView fView = null;
	
	public EditSourceLookupPathAction(LaunchView view) {
		super(SourceLookupUIMessages.EditSourceLookupPathAction_0); 
		setEnabled(false);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.EDIT_SOURCELOOKUP_ACTION);
		setImageDescriptor(DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_SRC_LOOKUP_MENU));
		fView = view;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		director = null;
		if (selection.size() == 1) {
			Object object = selection.getFirstElement();
			ILaunch launch = null;
			if (object instanceof IDebugElement) {
				launch = ((IDebugElement)object).getLaunch();
			} else if (object instanceof ILaunch) {
				launch = (ILaunch)object;
			}
			if (launch != null && launch.getLaunchConfiguration() != null &&
					launch.getSourceLocator() instanceof ISourceLookupDirector) {
				director = (ISourceLookupDirector) launch.getSourceLocator();
			}
		}
		return director != null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		Shell shell = DebugUIPlugin.getShell();		
		SourceLookupDialog dialog = new SourceLookupDialog(shell, director);
		if (dialog.open() == Window.OK) {
			ISelection selection = fView.getViewer().getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (ss.size() == 1) {
					IWorkbenchPage page = fView.getSite().getPage();
					SourceLookupManager.getDefault().displaySource(ss.getFirstElement(), page, true);
				}
			}
		}
	}
}

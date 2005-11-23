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
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * Does source lookup for the selected stack frame again.
 * 
 * @since 3.0
 */
public class LookupSourceAction extends SelectionListenerAction {
	
	private ISourceLookupDirector director = null;
	private LaunchView fView = null;
	private IStackFrame frame = null;
	
	public LookupSourceAction(LaunchView view) {
		super(SourceLookupUIMessages.LookupSourceAction_0); 
		setEnabled(false);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.LOOKUP_SOURCE_ACTION);
		fView = view;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		director = null;
		frame = null;
		if (selection.size() == 1) {
			Object object = selection.getFirstElement();
			if (object instanceof IStackFrame) {
				frame = (IStackFrame)object;
				ILaunch launch = frame.getLaunch();
				if (launch != null && launch.getLaunchConfiguration() != null &&
						launch.getSourceLocator() instanceof ISourceLookupDirector) {
					director = (ISourceLookupDirector) launch.getSourceLocator();
				}
			}
		}
		return director != null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
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

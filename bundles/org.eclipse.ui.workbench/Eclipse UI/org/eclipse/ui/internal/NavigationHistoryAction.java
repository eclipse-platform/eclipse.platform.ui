/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The <code>NavigationHistoryAction</code> moves navigation history 
 * back and forward.
 */
public class NavigationHistoryAction extends PageEventAction {
	private boolean forward;
	
	/**
	 * Create a new instance of <code>NavigationHistoryAction</code>
	 * 
	 * @param window the workbench window this action applies to
	 * @param forward if this action should move history forward of backward
	 */
	public NavigationHistoryAction(IWorkbenchWindow window,boolean forward) {
		super("",window); //$NON-NLS-1$
		if(forward) {
			setText(WorkbenchMessages.getString("NavigationHistoryAction.forward.text"));
			setToolTipText(WorkbenchMessages.getString("NavigationHistoryAction.forward.toolTip")); //$NON-NLS-1$
		} else {
			setText(WorkbenchMessages.getString("NavigationHistoryAction.backward.text"));
			setToolTipText(WorkbenchMessages.getString("NavigationHistoryAction.backward.toolTip")); //$NON-NLS-1$
		}
		// WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_ALL_PAGES_ACTION);
		setEnabled(false);
		this.forward = forward;
	}
	/* (non-Javadoc)
	 * Method declared on PageEventAction.
	 */		
	public void pageClosed(IWorkbenchPage page) {
		super.pageClosed(page);
		setEnabled(false);
	}
	/* (non-Javadoc)
	 * Method declared on PageEventAction.
	 */	
	public void pageActivated(IWorkbenchPage page) {
		super.pageActivated(page);
		NavigationHistory nh = (NavigationHistory)page.getNavigationHistory();
		if(forward)
			nh.setForwardAction(this);
		else
			nh.setBackwardAction(this);
	}
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		WorkbenchPage page = (WorkbenchPage)getActivePage();
		if (page != null) {
			NavigationHistory nh = (NavigationHistory)page.getNavigationHistory();
			if(forward)
				nh.forward();
			else
				nh.backward();
		}
	}
}
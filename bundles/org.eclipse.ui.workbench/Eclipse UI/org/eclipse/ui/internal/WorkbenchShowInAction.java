/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ShowInAction;

/**
 * Workbench's internal implementation of the Show In... action, which
 * tracks part activation. 
 */
/* package */
class WorkbenchShowInAction extends ShowInAction implements IPartListener {

	public WorkbenchShowInAction(IWorkbenchWindow window) {
		super(window);
		updateState();
	}

	public void partActivated(IWorkbenchPart part) {
		updateState(part);
	}
	
	public void partBroughtToTop(IWorkbenchPart part) {
	}
	
	public void partClosed(IWorkbenchPart part) {
		updateState();
	}
	
	public void partDeactivated(IWorkbenchPart part) {
		updateState();
	}
	
	public void partOpened(IWorkbenchPart part) {
	}

}

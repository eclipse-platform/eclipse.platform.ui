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
package org.eclipse.ui.internal.dialogs;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.IDialogBlockedHandler;
import org.eclipse.ui.internal.progress.BlockedJobsDialog;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
/**
 * The WorkbenchWizardBlockedHandler is the class that implements the blocked
 * handler for the workbench.
 */
public class WorkbenchDialogBlockedHandler implements IDialogBlockedHandler {
	BlockedJobsDialog blockedDialog;
	IProgressMonitor currentMonitor;
	/**
	 * Create a new instance of the receiver.
	 */
	public WorkbenchDialogBlockedHandler() {
		//No default behavior
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogBlockedHandler#clearBlocked()
	 */
	public void clearBlocked() {
		if (blockedDialog == null)
			return;
		blockedDialog.close(currentMonitor);
		blockedDialog = null;
		currentMonitor = null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogBlockedHandler#showBlocked(org.eclipse.swt.widgets.Shell,
	 *      org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IStatus, java.lang.String)
	 */
	public void showBlocked(Shell parentShell,
			IProgressMonitor blockingMonitor, IStatus blockingStatus,
			String blockedName) {
		
		currentMonitor = blockingMonitor;
		//Try to get a name as best as possible
		if (blockedName == null)
			blockedName = parentShell.getText();
		blockedDialog = BlockedJobsDialog.createBlockedDialog(parentShell,
				blockingMonitor, blockingStatus);
		blockedDialog.setBlockedTaskName(blockedName);
		blockedDialog.open();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogBlockedHandler#showBlocked(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IStatus, java.lang.String)
	 */
	public void showBlocked(IProgressMonitor blocking, IStatus blockingStatus,
			String blockedName) {
		showBlocked(ProgressManagerUtil.getDefaultParent(), blocking,
				blockingStatus, blockedName);
	}
}
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
import org.eclipse.jface.wizard.IWizardBlockedHandler;
import org.eclipse.ui.internal.progress.BlockedJobsDialog;
/**
 * The WorkbenchWizardBlockedHandler is the class that 
 * implements the blocked handler for the workbench.
 */
public class WorkbenchWizardBlockedHandler implements IWizardBlockedHandler {
	BlockedJobsDialog blockedDialog;
	
	/**
	 * Create a new instance of the receiver.
	 */
	public WorkbenchWizardBlockedHandler(){
		//No default behavior
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardBlockedHandler#clearBlocked()
	 */
	public void clearBlocked() {
		if (blockedDialog == null)
			return;
		blockedDialog.close();
		blockedDialog = null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardBlockedHandler#showBlocked(org.eclipse.swt.widgets.Shell, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IStatus, java.lang.String)
	 */
	public void showBlocked(Shell parentShell, IProgressMonitor blockingMonitor,
			IStatus blockingStatus, String blockedName) {
		//Try to get a name as best as possible
		if (blockedName == null)
			blockedName = parentShell.getText();
		blockedDialog = new BlockedJobsDialog(parentShell, blockingMonitor, blockingStatus);
		blockedDialog.setBlockedTaskName(blockedName);
		blockedDialog.open();
	}
}
/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.internal.progress.BlockedJobsDialog;
/**
 * The WorkbenchWizardDialog is the WizardDialog that can handle
 * any workbench level information such as blocked operations.
 * This is intended to be used by any wizard 
 */
public class WorkbenchWizardDialog extends WizardDialog {
	private BlockedJobsDialog blockedDialog;
	/**
	 * Open the receiver on the parent shell using the newWizard.
	 * @param parentShell
	 * @param newWizard
	 */
	public WorkbenchWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardDialog#createProgressMonitorPart(org.eclipse.swt.widgets.Composite, org.eclipse.swt.layout.GridLayout)
	 */
	protected ProgressMonitorPart createProgressMonitorPart(Composite composite, GridLayout pmlayout) {
		return new ProgressMonitorPart(composite, pmlayout, SWT.DEFAULT) {
			String currentTask = null;
			/* (non-Javadoc)
			 * @see org.eclipse.jface.wizard.ProgressMonitorPart#setBlocked(org.eclipse.core.runtime.IStatus)
			 */
			public void setBlocked(IStatus reason) {
				super.setBlocked(reason);
				//Use the shell name if we don't have anything yet.
				if(currentTask == null)
					currentTask = getShell().getText();
				showBlockedDialog(reason, this, currentTask);
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jface.wizard.ProgressMonitorPart#clearBlocked()
			 */
			public void clearBlocked() {
				super.clearBlocked();
				if (blockedDialog != null) {
					blockedDialog.close();
					blockedDialog = null;
				}
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jface.wizard.ProgressMonitorPart#beginTask(java.lang.String, int)
			 */
			public void beginTask(String name, int totalWork) {
				super.beginTask(name, totalWork);
				currentTask = name;
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jface.wizard.ProgressMonitorPart#setTaskName(java.lang.String)
			 */
			public void setTaskName(String name) {
				super.setTaskName(name);
				currentTask = name;
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jface.wizard.ProgressMonitorPart#subTask(java.lang.String)
			 */
			public void subTask(String name) {
				super.subTask(name);
				//If we haven't got anything yet use this value for more context
				if (currentTask == null)
					currentTask = name;
			}
		};
	}
	/**
	 * Show the BlockedJobsDialog for the monitor.
	 * @param reason The reason it is blocked.
	 * @param part The part that is showing progress.
	 * @param taskName The name of the task being blocked.
	 */
	private void showBlockedDialog(IStatus reason, ProgressMonitorPart part, String taskName) {
		blockedDialog = new BlockedJobsDialog(getShell(), part, reason);
		blockedDialog.setBlockedTaskName(taskName);
		blockedDialog.open();
	}
}
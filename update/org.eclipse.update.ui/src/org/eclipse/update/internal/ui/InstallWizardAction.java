/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.custom.*;
import org.eclipse.ui.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * Insert the type's description here.
 * @see IWorkbenchWindowActionDelegate
 */
public class InstallWizardAction extends Action implements IWorkbenchWindowActionDelegate {

	IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public InstallWizardAction() {
	}
	public void run() {
		init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		run(null);
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		BusyIndicator
			.showWhile(window.getShell().getDisplay(), new Runnable() {
			public void run() {
				doRun();
			}
		});
	}

	private void doRun() {
		openNewUpdatesWizard();
	}
	
	private void openNewUpdatesWizard() {
		InstallWizard wizard = new InstallWizard();
		WizardDialog dialog = new ResizableWizardDialog(window.getShell(), wizard);
		dialog.create();
		dialog.getShell().setText(UpdateUI.getString("InstallWizardAction.title")); //$NON-NLS-1$
		SWTUtil.setDialogSize(dialog, 600, 500);
		if (dialog.open() == IDialogConstants.OK_ID)
			UpdateUI.requestRestart(wizard.isRestartNeeded());
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}
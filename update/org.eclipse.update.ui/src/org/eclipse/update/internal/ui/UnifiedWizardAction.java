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
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.custom.*;
import org.eclipse.ui.*;
import org.eclipse.update.internal.ui.wizards.*;

/**
 * Insert the type's description here.
 * @see IWorkbenchWindowActionDelegate
 */
public class UnifiedWizardAction implements IWorkbenchWindowActionDelegate {
	private static final String KEY_TITLE = "NewUpdates.noUpdates.title";

	IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public UnifiedWizardAction() {
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
		dialog.getShell().setText(UpdateUI.getString(KEY_TITLE));
		dialog.getShell().setSize(600, 500);
		dialog.open();
		if (wizard.isSuccessfulInstall())
			UpdateUI.requestRestart();
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
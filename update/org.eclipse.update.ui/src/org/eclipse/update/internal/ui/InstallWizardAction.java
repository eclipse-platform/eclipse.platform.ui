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
import org.eclipse.swt.custom.*;
import org.eclipse.ui.*;
import org.eclipse.update.ui.*;

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
	 */
	public void run(IAction action) {
		BusyIndicator
			.showWhile(window.getShell().getDisplay(), new Runnable() {
			public void run() {
				UpdateManagerUI.openInstaller(window.getShell());
			}
		});
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}
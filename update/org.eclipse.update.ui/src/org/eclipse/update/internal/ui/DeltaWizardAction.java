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
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.custom.*;
import org.eclipse.ui.*;
import org.eclipse.update.core.*;

/**
 * Insert the type's description here.
 * @see IWorkbenchWindowActionDelegate
 */
public class DeltaWizardAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public DeltaWizardAction() {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		BusyIndicator.showWhile(window.getShell().getDisplay(), new Runnable() {
			public void run() {
				try {
					SiteManager.handleNewChanges();
				}
				catch (CoreException e) {
					UpdateUI.logException(e);
				}
			}
		});
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction arg0, ISelection arg1) {
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

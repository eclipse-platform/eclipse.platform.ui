/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.ui.internal;

import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * Action which can open the keyword index in either the help view or the browser.
 * 
 * @since 3.4
 */

public class ShowIndexAction implements IWorkbenchWindowActionDelegate {
	
	/**
	 * Constant to indicate that the keyword index should be opened in the browser
	 */
	public static final int OPEN_IN_BROWSER = 1;
	
	/**
	 * Constant to indicate that the keyword index should be opened in the help view
	 */
	public static final int OPEN_IN_HELP_VIEW = 2;

	private int mode;
	
	/**
	 * @param mode one of OPEN_IN_BROWSER or OPEN_IN_HELP_VIEW
	 */
	public ShowIndexAction(int mode) {
		this.mode = mode;
	}

	/**
	 * Shows the keyword index
	 */
	public void run(IAction action) {
		if (mode == OPEN_IN_BROWSER) {
		    openInBrowser();
		} else {
			openInHelpView();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	private void openInBrowser() {
		PlatformUI.getWorkbench().getHelpSystem();
		BaseHelpSystem.getHelpDisplay().displayHelpResource("tab=index", false); //$NON-NLS-1$
	}
	
	private void openInHelpView() {
		DefaultHelpUI.showIndex();		
	}
}
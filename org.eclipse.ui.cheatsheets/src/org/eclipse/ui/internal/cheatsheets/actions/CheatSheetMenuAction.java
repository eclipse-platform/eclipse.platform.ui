/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

/**
 * This is the action used to contribute the CheatSheets menu to the workbench's
 * help menu.  
 */
public class CheatSheetMenuAction implements IWorkbenchWindowPulldownDelegate2 {

	/**
	 * The constructor.
	 */
	public CheatSheetMenuAction() {
	}

	/* (non-Javadoc)
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate2#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		Menu menu = new Menu(parent);
		
		CheatSheetMenu cheatsheetMenuMenuItem = new CheatSheetMenu();
		
		cheatsheetMenuMenuItem.fill(menu, 0);
		return menu;
	}

	/* (non-Javadoc)
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
	}

	/* (non-Javadoc)
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}

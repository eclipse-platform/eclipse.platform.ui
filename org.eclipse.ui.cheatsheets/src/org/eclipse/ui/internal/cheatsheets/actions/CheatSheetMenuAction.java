/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;

/**
 * This is the action used to contribute the CheatSheets menu to the workbench's
 * help menu.  
 */
public class CheatSheetMenuAction implements IWorkbenchWindowPulldownDelegate2, IPropertyListener {
	/**
	 * The menu created by this action
	 */
	private Menu fMenu;

	/**
	 * Indicates whether the cheat sheet history has changed and
	 * the sub menu needs to be recreated.
	 */
	protected boolean fRecreateMenu = false;

	/**
	 * The constructor.
	 */
	public CheatSheetMenuAction() {
		CheatSheetPlugin.getPlugin().getCheatSheetHistory().addListener(this);
	}

	/* (non-Javadoc)
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
		setMenu(null);
		CheatSheetPlugin.getPlugin().getCheatSheetHistory().removeListener(this);
	}

	/**
	 * Fills the drop-down menu with cheat sheets history
	 * 
	 * @param menu the menu to fill
	 */
	protected void fillMenu(Menu menu) {
		CheatSheetMenu cheatsheetMenuMenuItem = new CheatSheetMenu();
		cheatsheetMenuMenuItem.fill(menu, 0);
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
		setMenu(new Menu(parent));
		fillMenu(fMenu);
		initMenu();
		return fMenu;
	}

	/* (non-Javadoc)
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
	}

	/**
	 * Creates the menu for the action
	 */
	private void initMenu() {
		// Add listener to repopulate the menu each time
		// it is shown because of dynamic history list
		fMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				if (fRecreateMenu) {
					Menu m = (Menu)e.widget;
					MenuItem[] items = m.getItems();
					for (int i=0; i < items.length; i++) {
						items[i].dispose();
					}
					fillMenu(m);
					fRecreateMenu= false;
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
	 */
	public void propertyChanged(Object source, int propId) {
		fRecreateMenu = true;
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

	/**
	 * Sets this action's drop-down menu, disposing the previous menu.
	 * 
	 * @param menu the new menu
	 */
	private void setMenu(Menu menu) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		fMenu = menu;
	}
}

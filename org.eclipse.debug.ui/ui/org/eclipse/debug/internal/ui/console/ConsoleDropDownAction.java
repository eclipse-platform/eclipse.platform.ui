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
package org.eclipse.debug.internal.ui.console;


import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Drop down action in the console to select the console to display.
 */
class ConsoleDropDownAction extends Action implements IMenuCreator, IConsoleListener, IUpdate {

	private IConsoleView fView;
	private Menu fMenu;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		IConsole[] consoles = DebugUIPlugin.getDefault().getConsoleManager().getConsoles();
		setEnabled(consoles.length > 1);
	}

	public ConsoleDropDownAction(IConsoleView view) {
		fView= view;
		setText(ConsoleMessages.getString("ConsoleDropDownAction.0")); //$NON-NLS-1$
		setToolTipText(ConsoleMessages.getString("ConsoleDropDownAction.1")); //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_VIEW_CONSOLE));
		setMenuCreator(this);
		DebugUIPlugin.getDefault().getConsoleManager().addConsoleListener(this);
		update();
	}

	public void dispose() {
		if (fMenu != null)
			fMenu.dispose();
		
		fView= null;
		DebugUIPlugin.getDefault().getConsoleManager().removeConsoleListener(this);
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

	public Menu getMenu(Control parent) {
		if (fMenu != null)
			fMenu.dispose();
		
		fMenu= new Menu(parent);
		IConsole[] consoles= DebugUIPlugin.getDefault().getConsoleManager().getConsoles();
		IConsole current = fView.getConsole();
		for (int i = 0; i < consoles.length; i++) {
			IConsole console = consoles[i];
			Action action = new ShowConsoleAction(fView, console);  
			action.setChecked(console.equals(current));
			addActionToMenu(fMenu, action);
		}
		return fMenu;
	}
	
	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	protected void addMenuSeparator() {
		new MenuItem(fMenu, SWT.SEPARATOR);		
	}

	public void run() {
		// do nothing - this is a menu
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsoleListener#consolesAdded(org.eclipse.debug.internal.ui.console.IConsole[])
	 */
	public void consolesAdded(IConsole[] consoles) {
		Display display = DebugUIPlugin.getStandardDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				update();
			}
		});
	}

	/* (non-Javadoc)
	 * 
	 * Dispose the menu when a launch is removed, such that the actions in this
	 * menu do not hang on to associated resources.
	 * 
	 * @see org.eclipse.debug.internal.ui.console.IConsoleListener#consolesRemoved(org.eclipse.debug.internal.ui.console.IConsole[])
	 */
	public void consolesRemoved(IConsole[] consoles) {
		Display display = DebugUIPlugin.getStandardDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				if (fMenu != null) {
					fMenu.dispose();
				}
				update();
			}
		});
	}

}

package org.eclipse.toolscript.ui.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.ArrayList;

import org.apache.tools.ant.BuildListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.toolscript.core.internal.IToolScriptContext;
import org.eclipse.toolscript.core.internal.ToolScript;
import org.eclipse.toolscript.core.internal.ToolScriptContext;
import org.eclipse.toolscript.core.internal.ToolScriptPlugin;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * This action will display the tool script configuration dialog.
 * In addition, as a tool bar item, it's drop down list will include
 * tool scripts to run directly.
 */
public class ToolScriptConfigureAction extends ActionDelegate implements IWorkbenchWindowPulldownDelegate2, IMenuCreator {
	private IWorkbenchWindow window;
	private IAction realAction;
	
	/**
	 * Creates the tool script configure action
	 */
	public ToolScriptConfigureAction() {
		super();
	}
	
	/* (non-Javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		if (action.isEnabled())
			showConfigurationDialog();
	}

	/* (non-Javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (realAction == null) {
			realAction = action;
			realAction.setMenuCreator(this);
		}
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWindowPulldownDelegate.
	 */
	public Menu getMenu(Control parent) {
		Menu menu= new Menu(parent);
		return createMenu(menu, false);
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWindowPulldownDelegate2.
	 */
	public Menu getMenu(Menu parent) {
		Menu menu= new Menu(parent);
		return createMenu(menu, true);
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWindowActionDelegate.
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWindowActionDelegate.
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	
	/**
	 * Creates the menu for the action
	 */
	private Menu createMenu(Menu menu, final boolean wantFastAccess) {
		// Add listener to repopulate the menu each time
		// it is shown because of dynamic history list
		menu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				Menu m = (Menu)e.widget;
				MenuItem[] items = m.getItems();
				for (int i=0; i < items.length; i++)
					items[i].dispose();
				populateMenu(m, wantFastAccess);
			}
		});
		
		return menu;
	}

	/**
	 * Populates the menu with its items
	 */
	private void populateMenu(Menu menu, boolean wantFastAccess) {
		// Add a menu item for each tool script in the history
		ArrayList scripts = ToolScriptPlugin.getDefault().getRegistry().getToolScripts();
		if (scripts.size() > 0) {
			for (int i = 0; i < scripts.size(); i++) {
				ToolScript script = (ToolScript)scripts.get(i);
				StringBuffer label = new StringBuffer();
				if (i < 9 && wantFastAccess) {
					//add the numerical accelerator
					label.append('&');
					label.append(i+1);
					label.append(' ');
				}
				label.append(script.getName());
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setText(label.toString());
				item.setData(script);
				item.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						runScript((ToolScript)e.widget.getData());
					}
				});
			}
			
			// Add a separator
			new MenuItem(menu, SWT.SEPARATOR);
		}

		// Add a menu to edit the configurations
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText(ToolScriptMessages.getString("ToolScriptConfigureAction.configure")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showConfigurationDialog();
			}
		});
	}

	/**
	 * Runs the specified script
	 */
	private void runScript(ToolScript script) {
		if (script == null)
			return;

		BuildListener listener = null;
		//	AntConsole[] consoles = new AntConsole[AntConsole.instances.size()];
		//	AntConsole.instances.toArray(consoles);
		//	for (int i = 0; i<consoles.length; i++)
		//		consoles[i].clearOutput();
		//	return new UIBuildListener(null, null, null, consoles);

		try {
			IToolScriptContext context = new ToolScriptContext(script, null);
			script.run(listener, null, context);
		} catch(CoreException e) {
			ErrorDialog.openError(
				window.getShell(),
				ToolScriptMessages.getString("ToolScriptConfigureAction.errorShellTitle"), //$NON-NLS-1$
				ToolScriptMessages.getString("ToolScriptConfigureAction.errorMessage"), //$NON-NLS-1$
				e.getStatus());
		}
	}
	
	/**
	 * Shows the tool script configuration dialog
	 */
	private void showConfigurationDialog() {
		ToolScriptConfigurationDialog dialog;
		dialog = new ToolScriptConfigurationDialog(window.getShell());
		dialog.open();
	}
}

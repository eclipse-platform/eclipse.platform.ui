package org.eclipse.ui.externaltools.internal.menu;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.AbstractLaunchHistoryAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * This action delegate is responsible for producing the
 * Run > External Tools sub menu contents, which includes
 * an items to run last tool, favorite tools, and show the
 * external tools view. Default action is to run the last tool
 * if one exist.
 */
public class ExternalToolMenuDelegate extends AbstractLaunchHistoryAction implements IWorkbenchWindowPulldownDelegate2, IMenuCreator {
	private IWorkbenchWindow window;
	private IAction realAction;
	
	/**
	 * Creates the action delegate
	 */
	public ExternalToolMenuDelegate() {
		super(IExternalToolConstants.ID_EXTERNAL_TOOLS_LAUNCH_GROUP);
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWindowActionDelegate.
	 */
	public void dispose() {
		super.dispose();
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWindowActionDelegate.
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
		super.init(window);
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
		// Add a menu item to show the external tools dialog
		MenuItem configure = new MenuItem(menu, SWT.NONE);
		configure.setText(ToolMessages.getString("ExternalToolMenuDelegate.configure")); //$NON-NLS-1$
		configure.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DebugUITools.openLaunchConfigurationDialogOnGroup(ExternalToolsPlugin.getActiveWorkbenchWindow().getShell(), new StructuredSelection(), IExternalToolConstants.ID_EXTERNAL_TOOLS_LAUNCH_GROUP);
			}
		});
	}
	
}

package org.eclipse.ui.externaltools.internal.menu;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.externaltools.action.RunExternalToolAction;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * This action delegate is responsible for producing the
 * Run > External Tools sub menu contents, which includes
 * an items to run last tool, favorite tools, and show the
 * external tools view. Default action is to run the last tool
 * if one exist.
 */
public class ExternalToolMenuDelegate extends ActionDelegate implements IWorkbenchWindowPulldownDelegate2, IMenuCreator {
	private IWorkbenchWindow window;
	private IAction realAction;
	
	/**
	 * Creates the action delegate
	 */
	public ExternalToolMenuDelegate() {
		super();
	}
	
	/* (non-Javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		if (action.isEnabled())
			runLastTool();
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
		// Add a menu item to run the most recent tool.
		MenuItem runRecent = new MenuItem(menu, SWT.NONE);
		runRecent.setText(ToolMessages.getString("ExternalToolMenuDelegate.runRecent")); //$NON-NLS-1$
		runRecent.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runLastTool();
			}
		});
		// Disable option if no tool has been run yet.
		runRecent.setEnabled(getLastTool() != null);
		
		// Add a separator.
		new MenuItem(menu, SWT.SEPARATOR);
				
		// Add a menu item for each tool in the favorites list.
		ExternalTool[] tools = FavoritesManager.getInstance().getFavorites();
		if (tools.length > 0) {
			for (int i = 0; i < tools.length; i++) {
				ExternalTool tool = tools[i];
				StringBuffer label = new StringBuffer();
				if (i < 9 && wantFastAccess) {
					//add the numerical accelerator
					label.append('&');
					label.append(i+1);
					label.append(' ');
				}
				label.append(tool.getName());
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setText(label.toString());
				item.setData(tool);
				item.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						runTool((ExternalTool)e.widget.getData());
					}
				});
			}
			
			// Add a separator.
			new MenuItem(menu, SWT.SEPARATOR);
		}

		// Add a menu item to show the external tools view.
		MenuItem showView = new MenuItem(menu, SWT.NONE);
		showView.setText(ToolMessages.getString("ExternalToolMenuDelegate.showView")); //$NON-NLS-1$
		showView.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showView();
			}
		});
	}

	/**
	 * Runs the specified tool
	 */
	private void runTool(final ExternalTool tool) {
		RunExternalToolAction runToolAction;
	 	runToolAction = new RunExternalToolAction(window);
		runToolAction.setTool(tool);
		runToolAction.run();
	}
	
	/**
	 * Shows the external tool view.
	 */
	private void showView() {
		try {
			IWorkbenchPage page = window.getActivePage();
			if (page != null)
				page.showView(IExternalToolConstants.VIEW_ID);
		} catch (PartInitException e) {
			ExternalToolsPlugin.getDefault().log("Unable to display the External Tools view.", e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Run the most recently run external tool.
	 */
	private void runLastTool() {
		if (getLastTool() == null)
			return;
		runTool(getLastTool());	
	}
	
	/**
	 * Returns the tool which was run most recently.
	 */
	 private ExternalTool getLastTool() {
	 	return FavoritesManager.getInstance().getLastTool();	
	 }
	
}

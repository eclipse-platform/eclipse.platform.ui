/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.rcp.browser;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * Builds the actions and populates the menubar and toolbar when a new window
 * is opened.
 * This work is factored into a separate class to avoid cluttering 
 * <code>BrowserAdvisor</code>
 * <p>
 * This adds several actions to the menus and toolbar that are typical for 
 * web browsers (e.g. Back, Forward, Stop, Refresh).  These are defined as 
 * retargetable actions, for which the <code>BrowserView</code> registers 
 * handling actions.
 * 
 * @since 3.0
 */
public class BrowserActionBuilder {

	private IWorkbenchWindow window;
	private IAction newWindowAction, quitAction, aboutAction;
	private RetargetAction backAction, forwardAction, stopAction, refreshAction;

	public BrowserActionBuilder(IWorkbenchWindow window) {
		this.window = window;
	}

	public void fillActionBars(IActionBarConfigurer configurer, int flags) {
		if ((flags & WorkbenchAdvisor.FILL_PROXY) == 0) {
			makeActions();
		}
		if ((flags & WorkbenchAdvisor.FILL_MENU_BAR) != 0) {
			fillMenuBar(configurer.getMenuManager());
		}
		if ((flags & WorkbenchAdvisor.FILL_TOOL_BAR) != 0) {
			fillCoolBar(configurer.getCoolBarManager());
		}
	}
	
	private void makeActions() {
		newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
		newWindowAction.setText("New Window");
		quitAction = ActionFactory.QUIT.create(window);
		backAction = new RetargetAction("back", "Back");  //$NON-NLS-1$
		window.getPartService().addPartListener(backAction);
		forwardAction = new RetargetAction("forward", "Forward");  //$NON-NLS-2$
		window.getPartService().addPartListener(forwardAction);
		stopAction = new RetargetAction("stop", "Stop");  //$NON-NLS-1$
		window.getPartService().addPartListener(stopAction);
		refreshAction = new RetargetAction("refresh", "Refresh");  //$NON-NLS-1$
		window.getPartService().addPartListener(refreshAction);
		aboutAction = new Action() {
			{ setText("&About"); }
			public void run() {
				MessageDialog.openInformation(
					window.getShell(), 
					"About RCP Browser Example", 
					"Eclipse Rich Client Platform Browser Example.\n\n" +
					"(c) 2003 IBM Corporation and others.");
			}
		};
	}

	public void fillMenuBar(IMenuManager menuBar) {
		IMenuManager fileMenu = new MenuManager("&File", "file");  //$NON-NLS-2$
		menuBar.add(fileMenu);
		fileMenu.add(newWindowAction);
		fileMenu.add(new Separator());
		fileMenu.add(quitAction);
		
		IMenuManager viewMenu = new MenuManager("&View", "view");  //$NON-NLS-2$
		menuBar.add(viewMenu);
		IMenuManager goToMenu = new MenuManager("Go To", "goto");  //$NON-NLS-2$
		viewMenu.add(goToMenu);
		goToMenu.add(backAction);
		goToMenu.add(forwardAction);
		viewMenu.add(stopAction);
		viewMenu.add(refreshAction);

		IMenuManager helpMenu = new MenuManager("&Help", "help");  //$NON-NLS-2$
		menuBar.add(helpMenu);
		helpMenu.add(aboutAction);
	}

	public void fillCoolBar(ICoolBarManager coolBar) {
		IToolBarManager toolBar = new ToolBarManager(coolBar.getStyle());
		coolBar.add(new ToolBarContributionItem(toolBar, "standard"));
		toolBar.add(backAction);
		toolBar.add(forwardAction);
		toolBar.add(stopAction);
		toolBar.add(refreshAction);
	}
}

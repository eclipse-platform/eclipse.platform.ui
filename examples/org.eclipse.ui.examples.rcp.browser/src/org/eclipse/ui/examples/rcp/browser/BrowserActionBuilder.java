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
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
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
	private ActionFactory.IWorkbenchAction newWindowAction, quitAction, aboutAction;
	private RetargetAction backAction, forwardAction, stopAction, refreshAction;
    private IAction historyAction;
    private IAction newTabAction;

	public BrowserActionBuilder(IWorkbenchWindow window) {
		this.window = window;
	}

	public void fillActionBars(IActionBarConfigurer configurer, int flags) {
		if ((flags & WorkbenchAdvisor.FILL_PROXY) == 0) {
			makeActions(configurer);
		}
		if ((flags & WorkbenchAdvisor.FILL_MENU_BAR) != 0) {
			fillMenuBar(configurer.getMenuManager());
		}
		if ((flags & WorkbenchAdvisor.FILL_COOL_BAR) != 0) {
			fillCoolBar(configurer.getCoolBarManager());
		}
	}
	
	private void makeActions(IActionBarConfigurer configurer) {
		ISharedImages images = window.getWorkbench().getSharedImages();
		
		newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
		newWindowAction.setText("&New Window");
		configurer.registerGlobalAction(newWindowAction);
		
		newTabAction = new Action("New &Tab") {
		    int counter = 0;
		    { setActionDefinitionId(IBrowserConstants.COMMAND_PREFIX + "newTab"); } //$NON-NLS-1$
            public void run() {
                try {
                    String secondaryId = Integer.toString(++counter);
                    IWorkbenchPage page = window.getActivePage();
                    if (page != null) {
                        page.showView(IBrowserConstants.BROWSER_VIEW_ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
                    }
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
		};
		configurer.registerGlobalAction(newTabAction);
		
		quitAction = ActionFactory.QUIT.create(window);
		configurer.registerGlobalAction(quitAction);
		
		backAction = new RetargetAction("back", "&Back");  //$NON-NLS-1$
		backAction.setActionDefinitionId(IBrowserConstants.COMMAND_PREFIX + "back"); //$NON-NLS-1$
		backAction.setToolTipText("Back");
		backAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
		window.getPartService().addPartListener(backAction);
		configurer.registerGlobalAction(backAction);
		
		forwardAction = new RetargetAction("forward", "&Forward");  //$NON-NLS-1$
		forwardAction.setActionDefinitionId(IBrowserConstants.COMMAND_PREFIX + "forward"); //$NON-NLS-1$
		forwardAction.setToolTipText("Forward");
		forwardAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
		window.getPartService().addPartListener(forwardAction);
		configurer.registerGlobalAction(forwardAction);
		
		stopAction = new RetargetAction("stop", "Sto&p");  //$NON-NLS-1$
		stopAction.setActionDefinitionId(IBrowserConstants.COMMAND_PREFIX + "stop"); //$NON-NLS-1$
		stopAction.setToolTipText("Stop");
		window.getPartService().addPartListener(stopAction);
		configurer.registerGlobalAction(stopAction);
		
		refreshAction = new RetargetAction("refresh", "&Refresh");  //$NON-NLS-1$
		refreshAction.setActionDefinitionId(IBrowserConstants.COMMAND_PREFIX + "refresh"); //$NON-NLS-1$
		refreshAction.setToolTipText("Refresh");
		window.getPartService().addPartListener(refreshAction);
		configurer.registerGlobalAction(refreshAction);
		
		historyAction = new Action("History", IAction.AS_CHECK_BOX) {
		    { setActionDefinitionId(IBrowserConstants.COMMAND_PREFIX + "history"); } //$NON-NLS-1$
		    public void run() {
		        try {
		            IWorkbenchPage page = window.getActivePage();
		            if (page != null) {
		                IViewPart historyView = page.findView(IBrowserConstants.HISTORY_VIEW_ID);
		                if (historyView == null) {
		                    page.showView(IBrowserConstants.HISTORY_VIEW_ID);
		                    setChecked(true);
		                }
		                else {
		                    page.hideView(historyView);
		                    setChecked(false);
		                }
		            }
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
		    }
		};
		configurer.registerGlobalAction(historyAction);

		aboutAction = ActionFactory.ABOUT.create(window);
		configurer.registerGlobalAction(aboutAction);
	}

	public void fillMenuBar(IMenuManager menuBar) {
		IMenuManager fileMenu = new MenuManager("&File", "file");  //$NON-NLS-2$
		menuBar.add(fileMenu);
		fileMenu.add(newWindowAction);
		fileMenu.add(newTabAction);
		fileMenu.add(new Separator());
		fileMenu.add(quitAction);
		
		IMenuManager viewMenu = new MenuManager("&View", "view");  //$NON-NLS-2$
		menuBar.add(viewMenu);
		viewMenu.add(backAction);
		viewMenu.add(forwardAction);
		viewMenu.add(stopAction);
		viewMenu.add(refreshAction);
		viewMenu.add(new Separator("views")); //$NON-NLS-1$
		viewMenu.add(historyAction);

		IMenuManager helpMenu = new MenuManager("&Help", "help");  //$NON-NLS-2$
		menuBar.add(helpMenu);
		helpMenu.add(aboutAction);
	}

	public void fillCoolBar(ICoolBarManager coolBar) {
		IToolBarManager toolBar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
		coolBar.add(new ToolBarContributionItem(toolBar, "standard"));
		
		// For the Back and Forward actions, force their text to be shown on the toolbar,
		// not just their image.  For the remaining actions, the ActionContributionItem
		// is created implicitly with the default presentation mode.
		ActionContributionItem backCI = new ActionContributionItem(backAction);
		backCI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		toolBar.add(backCI);
		
		ActionContributionItem forwardCI = new ActionContributionItem(forwardAction);
		forwardCI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		toolBar.add(forwardCI);
		
		toolBar.add(stopAction);
		toolBar.add(refreshAction);
	}
	
	public void dispose() {
	    newWindowAction.dispose();
	    quitAction.dispose();
	    aboutAction.dispose();
	}
}

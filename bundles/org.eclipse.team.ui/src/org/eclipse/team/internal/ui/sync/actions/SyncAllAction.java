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
package org.eclipse.team.internal.ui.sync.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.team.core.subscribers.TeamProvider;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.sync.ISyncViewer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;

public class SyncAllAction implements IWorkbenchWindowPulldownDelegate2 {
	
	private IWorkbenchWindow window;
	private Menu createdMenu;
	
	class SubscriberAction extends Action {
		TeamSubscriber subscriber;
		SubscriberAction(TeamSubscriber s) {
			this.subscriber = s;
			setText(s.getName());			
		}
		public void run() {
			ISyncViewer view = TeamUI.showSyncViewInActivePage(window.getActivePage());
			if(view != null) {
				view.refreshWithRemote(subscriber);
			}
		}
	}
	
	public void run(IAction action) {
		ISyncViewer view = TeamUI.showSyncViewInActivePage(window.getActivePage());
		if(view != null) {
			view.refreshWithRemote();
		}
	}

	public void dispose() {
		if(getCreatedMenu() != null) {
			getCreatedMenu().dispose();
		}
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	private void createAction(Menu parent, IAction action, int count) {
		StringBuffer label= new StringBuffer();
		//add the numerical accelerator
		if (count != -1) {
			label.append('&');
			label.append(count);
			label.append(' ');
		}
		label.append(action.getText());
		action.setText(label.toString());
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}
	
	public Menu getMenu(Menu parent) {
		dispose();
		setCreatedMenu(new Menu(parent));
		fillMenu();
		initMenu();
		return getCreatedMenu();
	}

	private Menu getCreatedMenu() {
		return createdMenu;
	}
	
	private void setCreatedMenu(Menu menu) {
		createdMenu = menu;
	}

	private void fillMenu() {
		TeamSubscriber[] subscribers = TeamProvider.getSubscribers();
		for (int i = 0; i < subscribers.length; i++) {
			TeamSubscriber subscriber = subscribers[i];
			createAction(getCreatedMenu(), new SubscriberAction(subscriber), i + 1);
		}
		new Separator().fill(getCreatedMenu(), -1);
		createAction(getCreatedMenu(), new SyncViewerShowPreferencesAction(window.getShell()), -1);
		
	}	
	
	/**
	 * Creates the menu for the action
	 */
	private void initMenu() {
		// Add listener to repopulate the menu each time
		// it is shown to reflect changes in selection or active perspective
		getCreatedMenu().addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				Menu m = (Menu)e.widget;
				MenuItem[] items = m.getItems();
				for (int i=0; i < items.length; i++) {
					items[i].dispose();
				}
				fillMenu();
			}
		});
	}

	public Menu getMenu(Control parent) {
		dispose();
		setCreatedMenu(new Menu(parent));
		fillMenu();
		initMenu();
		return getCreatedMenu();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}		
}
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
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * This class allows SyncViewerActionGroups to be place in a toolbar as 
 * drop down menus
 */
public class SyncViewerToolbarDropDownAction extends Action implements IMenuCreator {

	SyncViewerActionGroup[] actionGroup;
	private Menu fMenu;

	public SyncViewerToolbarDropDownAction(SyncViewerActionGroup actionGroup) {
		this.actionGroup = new SyncViewerActionGroup[] {actionGroup};
		setMenuCreator(this);
	}
	
	public SyncViewerToolbarDropDownAction(SyncViewerActionGroup[] actionGroups) {
		this.actionGroup = actionGroups;
		setMenuCreator(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		// TODO: The menu is recreated each time. Another possibility would be to
		// cache the menu and reset it at the appropriate time
		if (fMenu != null)
			fMenu.dispose();
		
		fMenu= new Menu(parent);
		fillMenu();
		return fMenu;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		return null;
	}

	private void fillMenu() {
		for (int i = 0; i < actionGroup.length; i++) {
			if(i != 0 && i < (actionGroup.length) && getMenu().getItemCount() > 0) {
				new Separator().fill(getMenu(), -1);	
			}
			actionGroup[i].fillMenu(this);
		}
	}
	 
	public void add(Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(getMenu(), -1);		
	}
	
	public void add(ContributionItem item) {
		item.fill(getMenu(), -1);		
	}
	
	public Menu getMenu() {
		return fMenu;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		super.run();
	}
	
	public SyncViewerActionGroup[] getActionGroups() {
		return actionGroup;
	} 
}

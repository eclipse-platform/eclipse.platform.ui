/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.actions.NewWizardAction;
import org.eclipse.ui.actions.NewWizardMenu;

/**
 * Invoke the resource creation wizard selection Wizard.
 * This action will retarget to the active view.
 */
public class NewWizardDropDownAction extends Action implements IMenuCreator, IWorkbenchWindowPulldownDelegate2 {
	private IWorkbenchWindow window;
	private NewWizardAction newWizardAction;
	private MenuManager dropDownMenuMgr;
	/**
	 *	Create a new instance of this class
	 */
	public NewWizardDropDownAction(IWorkbenchWindow window, NewWizardAction newWizardAction) {
		super(WorkbenchMessages.getString("NewWizardDropDown.text")); //$NON-NLS-1$
		this.window = window;
		this.newWizardAction = newWizardAction;
		setToolTipText(newWizardAction.getToolTipText());
		setImageDescriptor(newWizardAction.getImageDescriptor());
		setMenuCreator(this);
	}
	/**
	 * create the menu manager for the drop down menu.
	 */
	protected void createDropDownMenuMgr() {
		if (dropDownMenuMgr == null) {
			dropDownMenuMgr = new MenuManager();
			dropDownMenuMgr.add(new NewWizardMenu(window));
		}
	}
	/**
	 * dispose method comment.
	 */
	public void dispose() {
		if (dropDownMenuMgr != null) {
			dropDownMenuMgr.dispose();
			dropDownMenuMgr = null;
		}
	}
	/**
	 * getMenu method comment.
	 */
	public Menu getMenu(Control parent) {
		createDropDownMenuMgr();
		return dropDownMenuMgr.createContextMenu(parent);
	}
	/**
	 * Create the drop down menu as a submenu of parent.  Necessary
	 * for CoolBar support.
	 */
	public Menu getMenu(Menu parent) {
		createDropDownMenuMgr();
		Menu menu = new Menu(parent);
		IContributionItem[] items = dropDownMenuMgr.getItems();
		for (int i = 0; i < items.length; i++) {
			IContributionItem item = items[i];
			IContributionItem newItem = item;
			if (item instanceof ActionContributionItem) {
				newItem = new ActionContributionItem(((ActionContributionItem) item).getAction());
			}
			newItem.fill(menu, -1);
		}
		return menu;
	}
	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}
	public void run() {
		newWizardAction.run();
	}
	/**
	 * @see runWithEvent(IAction, Event)
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
	}
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}

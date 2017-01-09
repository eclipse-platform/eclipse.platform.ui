/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Patrick Chuong (Texas Instruments) - Improve usability of the breakpoint view (Bug 238956)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointSortBy;

import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.breakpointGroups.AbstractBreakpointsViewAction;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 *
 */
public class SortBreakpointsByAction extends AbstractBreakpointsViewAction implements IMenuCreator {

	private IAction fAction= null;

	public SortBreakpointsByAction() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    @Override
	public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    @Override
	public Menu getMenu(Control parent) {
        // Never called
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
     */
    @Override
	public Menu getMenu(Menu parent) {
		Menu menu = new Menu(parent);
		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				Menu m = (Menu)e.widget;
				MenuItem[] items = m.getItems();
				for (int i=0; i < items.length; i++) {
					items[i].dispose();
				}
				fillMenu(m);
			}
		});
		return menu;
    }

 	/**
	 * Fill pull down menu with the "group by" options
	 */
	private void fillMenu(Menu menu) {

		// Add hard-coded sorting order by Name
		IAction action = new SortBreakpointsAction(fView, Messages.GroupBreakpointsSortByAction_Name, IInternalDebugUIConstants.BREAKPOINT_SORTING_ORDER_NAME);
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(menu, 0);

		// Add hard-coded sorting order by Creation Time
		action = new SortBreakpointsAction(fView, Messages.GroupBreakpointsSortByAction_CreationTime, IInternalDebugUIConstants.BREAKPOINT_SORTING_ORDER_CREATION_TIME);
		item = new ActionContributionItem(action);
		item.fill(menu, 1);

	}


    /* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	    if (action != fAction) {
	        action.setMenuCreator(this);
	        fAction= action;
	    }
	}
}

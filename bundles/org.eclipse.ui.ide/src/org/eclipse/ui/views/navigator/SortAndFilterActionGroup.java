/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Images for menu items (27481)
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;

/**
 * This is the action group for the sort and filter actions.
 */
public class SortAndFilterActionGroup extends ResourceNavigatorActionGroup {

    private SortViewAction sortByTypeAction;

    private SortViewAction sortByNameAction;

    private FilterSelectionAction filterAction;

    public SortAndFilterActionGroup(IResourceNavigator navigator) {
        super(navigator);
    }

    protected void makeActions() {
        sortByNameAction = new SortViewAction(navigator, false);
        sortByTypeAction = new SortViewAction(navigator, true);

        filterAction = new FilterSelectionAction(navigator,
                ResourceNavigatorMessages.ResourceNavigator_filterText);
        filterAction
                .setDisabledImageDescriptor(getImageDescriptor("dlcl16/filter_ps.gif"));//$NON-NLS-1$
        filterAction
                .setImageDescriptor(getImageDescriptor("elcl16/filter_ps.gif"));//$NON-NLS-1$
    }

    public void fillActionBars(IActionBars actionBars) {
        IMenuManager menu = actionBars.getMenuManager();
        IMenuManager submenu = new MenuManager(ResourceNavigatorMessages.ResourceNavigator_sort);
        menu.add(submenu);
        submenu.add(sortByNameAction);
        submenu.add(sortByTypeAction);
        menu.add(filterAction);
    }

    public void updateActionBars() {
        int criteria = navigator.getSorter().getCriteria();
        sortByNameAction.setChecked(criteria == ResourceSorter.NAME);
        sortByTypeAction.setChecked(criteria == ResourceSorter.TYPE);
    }
}

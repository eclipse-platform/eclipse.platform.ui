/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Images for menu items (27481)
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430694
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;

/**
 * This is the action group for the sort and filter actions.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 *
 *              Planned to be deleted, please see Bug
 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=549953
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
@Deprecated(forRemoval = true)
public class SortAndFilterActionGroup extends ResourceNavigatorActionGroup {

	private SortViewAction sortByTypeAction;

	private SortViewAction sortByNameAction;

	private FilterSelectionAction filterAction;

	/**
	 * Constructor.
	 *
	 * @param navigator the resource navigator
	 */
	public SortAndFilterActionGroup(IResourceNavigator navigator) {
		super(navigator);
	}

	@Override
	protected void makeActions() {
		sortByNameAction = new SortViewAction(navigator, false);
		sortByTypeAction = new SortViewAction(navigator, true);

		filterAction = new FilterSelectionAction(navigator, ResourceNavigatorMessages.ResourceNavigator_filterText);
		filterAction.setDisabledImageDescriptor(getImageDescriptor("dlcl16/filter_ps.png"));//$NON-NLS-1$
		filterAction.setImageDescriptor(getImageDescriptor("elcl16/filter_ps.png"));//$NON-NLS-1$
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		IMenuManager menu = actionBars.getMenuManager();
		IMenuManager submenu = new MenuManager(ResourceNavigatorMessages.ResourceNavigator_sort);
		menu.add(submenu);
		submenu.add(sortByNameAction);
		submenu.add(sortByTypeAction);
		menu.add(filterAction);
	}

	@Override
	public void updateActionBars() {
		int criteria = navigator.getComparator().getCriteria();
		sortByNameAction.setChecked(criteria == ResourceComparator.NAME);
		sortByTypeAction.setChecked(criteria == ResourceComparator.TYPE);
	}
}

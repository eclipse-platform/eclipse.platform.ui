/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;

/**
 * Baseclass for working set pulldown actions.
 *
 * @since 3.3
 */
public abstract class AbstractWorkingSetPulldownDelegate
		implements IWorkbenchWindowActionDelegate, IWorkbenchWindowPulldownDelegate2 {

	private Menu menubarMenu;

	private Menu toolbarMenu;

	private ISelection selection;

	private IWorkbenchWindow window;

	public AbstractWorkingSetPulldownDelegate() {
		super();
	}

	@Override
	public void dispose() {
		if (menubarMenu != null) {
			menubarMenu.dispose();
			menubarMenu = null;
		}
		if (toolbarMenu != null) {
			toolbarMenu.dispose();
			toolbarMenu = null;
		}
	}

	@Override
	public Menu getMenu(Control parent) {
		if (toolbarMenu != null) {
			toolbarMenu.dispose();
		}
		toolbarMenu = new Menu(parent);
		initMenu(toolbarMenu);
		return toolbarMenu;
	}

	@Override
	public Menu getMenu(Menu parent) {
		if (menubarMenu != null) {
			menubarMenu.dispose();
		}
		menubarMenu = new Menu(parent);
		initMenu(menubarMenu);
		return menubarMenu;
	}

	/**
	 * Creates the menu for the action
	 */
	private void initMenu(Menu menu) {
		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				Menu m = (Menu) e.widget;
				MenuItem[] items = m.getItems();
				for (MenuItem item : items) {
					item.dispose();
				}
				fillMenu(m);
			}

		});
	}

	protected abstract void fillMenu(Menu m);

	/**
	 * Split the working sets known by the manager into arrays based on their
	 * defining page Id.
	 *
	 * @return an array of arrays
	 */
	protected IWorkingSet[][] splitSets() {
		IWorkingSet[] allSets = getWindow().getWorkbench().getWorkingSetManager().getWorkingSets();

		Map<String, List<IWorkingSet>> map = new HashMap<>();
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();

		for (IWorkingSet allSet : allSets) {
			String setType = allSet.getId();
			if (WorkbenchActivityHelper.filterItem(registry.getWorkingSetDescriptor(setType))) {
				continue;
			}
			List<IWorkingSet> setsOfType = map.get(setType);
			if (setsOfType == null) {
				setsOfType = new ArrayList<>();
				map.put(setType, setsOfType);
			}
			setsOfType.add(allSet);
		}

		IWorkingSet[][] typedSets = new IWorkingSet[map.size()][];
		int i = 0;
		for (Map.Entry<String, List<IWorkingSet>> entry : map.entrySet()) {
			List<IWorkingSet> setsOfType = entry.getValue();
			typedSets[i] = new IWorkingSet[setsOfType.size()];
			setsOfType.toArray(typedSets[i++]);
		}
		return typedSets;
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	protected IWorkbenchWindow getWindow() {
		return window;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	protected ISelection getSelection() {
		return selection;
	}
}
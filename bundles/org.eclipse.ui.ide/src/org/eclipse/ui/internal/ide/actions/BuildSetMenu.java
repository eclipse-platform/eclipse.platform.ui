/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 * hzhou@actuate.com - Fix for  Bug 71695 -
 * [WorkingSets]Removed Working Set is still shown under the menu item
 * when it is the recently used working set
 *******************************************************************************/
package org.eclipse.ui.internal.ide.actions;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * Sub-menu off project menu for showing MRU list of working set builds.
 *
 * @since 3.0
 */
public class BuildSetMenu extends ContributionItem {
	private IActionBarConfigurer actionBars;

	boolean dirty = true;

	private IMenuListener menuListener = manager -> {
		manager.markDirty();
		dirty = true;
	};

	private IAction selectBuildWorkingSetAction;

	private IWorkbenchWindow window;

	/**
	 * Create a new instance of the receiver.
	 */
	public BuildSetMenu(IWorkbenchWindow window, IActionBarConfigurer actionBars) {
		this.window = window;
		this.actionBars = actionBars;
		selectBuildWorkingSetAction = new SelectBuildWorkingSetAction(window,
				actionBars);
	}

	/**
	 * Adds a mnemonic accelerator to actions in the MRU list of
	 * recently built working sets.
	 * @param action the action to add
	 * @param index the index to add it at
	 */
	private void addMnemonic(BuildSetAction action, int index) {
		StringBuilder label = new StringBuilder();
		//add the numerical accelerator
		if (index < 9) {
			label.append('&');
			label.append(index);
			label.append(' ');
		}
		label.append(action.getWorkingSet().getLabel());
		action.setText(label.toString());
	}

	@Override
	public void fill(Menu menu, int index) {
		if (getParent() instanceof MenuManager) {
			((MenuManager) getParent()).addMenuListener(menuListener);
		}
		if (!dirty) {
			return;
		}
		fillMenu(menu);
		dirty = false;
	}

	/**
	 * Fills the menu with Show View actions.
	 * @param menu The menu being filled.
	 */
	private void fillMenu(Menu menu) {
		boolean isAutoBuilding = ResourcesPlugin.getWorkspace()
				.isAutoBuilding();

		//build MRU list of recently built working sets:
		IWorkingSet[] sets = window.getWorkbench().getWorkingSetManager()
				.getRecentWorkingSets();
		BuildSetAction last = BuildSetAction.lastBuilt;
		IWorkingSet lastSet = null;
		//add build action for the last working set that was built
		int accel = 1;
		if (last != null) {
			// add it only if it has not been removed
			boolean found = false;
			for (IWorkingSet set : sets) {
				if (set.equals(last.getWorkingSet())){
					found = true;
					break;
				}
			}
			if (found) {
				last.setChecked(true);
				last.setEnabled(!isAutoBuilding);
				last.setActionDefinitionId("org.eclipse.ui.project.buildLast"); //$NON-NLS-1$
				addMnemonic(last, accel++);
				new ActionContributionItem(last).fill(menu, -1);
				lastSet = last.getWorkingSet();
			} else {
				BuildSetAction.lastBuilt = null;
			}
		}
		//add build actions for the most recently used working sets
		for (IWorkingSet set : sets) {
			if (lastSet != null && lastSet.equals(set)) {
				continue;
			}
			BuildSetAction action = new BuildSetAction(set, window,
					actionBars);
			addMnemonic(action, accel++);
			action.setEnabled(!isAutoBuilding);
			new ActionContributionItem(action).fill(menu, -1);
		}
		//add the action to select a different working set
		if (sets.length > 0) {
			new Separator().fill(menu, -1);
		}
		selectBuildWorkingSetAction.setEnabled(!isAutoBuilding);
		new ActionContributionItem(selectBuildWorkingSetAction).fill(menu, -1);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public void dispose() {
		window = null;
		selectBuildWorkingSetAction = null;
		actionBars = null;
		super.dispose();
	}
}

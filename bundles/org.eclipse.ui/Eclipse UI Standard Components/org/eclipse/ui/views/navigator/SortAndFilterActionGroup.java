package org.eclipse.ui.views.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionGroup;

/**
 * This is the action group for the sort and filter actions.
 */
public class SortAndFilterActionGroup extends ActionGroup {

	private IResourceNavigatorPart navigator;
	private SortViewAction sortByTypeAction;
	private SortViewAction sortByNameAction;
	private FilterSelectionAction filterAction;

	public SortAndFilterActionGroup(IResourceNavigatorPart navigator) {
		this.navigator = navigator;
		makeActions();
	}

	private void makeActions() {
		sortByNameAction = new SortViewAction(navigator, false);
		sortByTypeAction = new SortViewAction(navigator, true);
		filterAction =
			new FilterSelectionAction(
				navigator,
				ResourceNavigatorMessages.getString("ResourceNavigator.filterText")); //$NON-NLS-1$
	}

	public void fillActionBars(IActionBars actionBars) {
		IMenuManager menu = actionBars.getMenuManager();
		IMenuManager submenu =
			new MenuManager(ResourceNavigatorMessages.getString("ResourceNavigator.sort")); //$NON-NLS-1$
		menu.add(submenu);
		submenu.add(sortByNameAction);
		submenu.add(sortByTypeAction);
		menu.add(filterAction);
	}

	public void updateActionBars() {
		int criteria = navigator.getResourceSorter().getCriteria();
		sortByNameAction.setChecked(criteria == ResourceSorter.NAME);
		sortByTypeAction.setChecked(criteria == ResourceSorter.TYPE);
	}
}
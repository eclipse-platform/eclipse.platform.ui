package org.eclipse.ui.views.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * ActionBarMenuContributionFactory is the factory for the
 * actions that are in the drop down menu of the action 
 * bar
 */

public class ActionBarMenuContributionFactory
	extends ActionContributionFactory {

	protected SortViewAction sortByTypeAction;
	protected SortViewAction sortByNameAction;
	protected FilterSelectionAction filterAction;
	protected IResourceNavigatorPart navigatorPart;

	public ActionBarMenuContributionFactory(IResourceNavigatorPart part) {
		navigatorPart = part;
	}

	/*
	 * @see ActionContributionFactory#updateActions(IStructuredSelection)
	 */
	public void updateActions(IStructuredSelection selection) {
		sortByTypeAction.selectionChanged(selection);
		sortByNameAction.selectionChanged(selection);
	}

	/*
	 * @see ActionContributionFactory#makeActions()
	 */
	public void makeActions() {
		sortByNameAction = new SortViewAction(navigatorPart, false);
		sortByTypeAction = new SortViewAction(navigatorPart, true);
		filterAction =
			new FilterSelectionAction(
				navigatorPart,
				ResourceNavigatorMessages.getString("ResourceNavigator.filterText"));

	}

	/**
	 * Fill the menu with any menu actions. The selection is not interesting
	 * for these options.
	 * @param menu the context sensitive menu
	 */
	public void fillMenu(IMenuManager menu, ResourceSorter sorter) {
		IMenuManager submenu =
			new MenuManager(ResourceNavigatorMessages.getString("ResourceNavigator.sort"));
		//$NON-NLS-1$
		menu.add(submenu);
		updateSortActions(sorter);
		submenu.add(sortByNameAction);
		submenu.add(sortByTypeAction);
		menu.add(filterAction);
	}

	/**
		 * Updates the checked state of the sort actions.
		 */
	protected void updateSortActions(ResourceSorter sorter) {
		int criteria = sorter.getCriteria();
		if (sortByNameAction != null && sortByTypeAction != null) {
			sortByNameAction.setChecked(criteria == ResourceSorter.NAME);
			sortByTypeAction.setChecked(criteria == ResourceSorter.TYPE);
		}
	}

}
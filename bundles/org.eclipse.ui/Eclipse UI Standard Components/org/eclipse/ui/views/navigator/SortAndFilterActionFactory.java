package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.views.navigator.FilterSelectionAction;
import org.eclipse.ui.views.navigator.IResourceNavigatorPart;
import org.eclipse.ui.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.views.navigator.ResourceSorter;
import org.eclipse.ui.views.navigator.SortViewAction;

/**
 * SortAndFilterActionFactory is the factory for the
 * actions that are in the drop down menu of the action 
 * bar
 * 
 * @deprecated use SortAndFilterActionGroup
 */
public class SortAndFilterActionFactory
	extends ActionFactory {

	protected SortViewAction sortByTypeAction;
	protected SortViewAction sortByNameAction;
	protected FilterSelectionAction filterAction;
	protected IResourceNavigatorPart navigatorPart;

	/**
	 * @deprecated
	 */
	public SortAndFilterActionFactory(IResourceNavigatorPart part) {
		navigatorPart = part;
	}


	/*
	 * @see ActionFactory#makeActions()
	 */
	public void makeActions() {
		sortByNameAction = new SortViewAction(navigatorPart, false);
		sortByTypeAction = new SortViewAction(navigatorPart, true);
		filterAction =
			new FilterSelectionAction(
				navigatorPart,
				ResourceNavigatorMessages.getString("ResourceNavigator.filterText"));

	}

	/*
	 * @see ActionFactory#fillActionBarMenu(IMenuManager,IStructuredSelection)
	 */
	public void fillActionBarMenu(IMenuManager menu, IStructuredSelection selection) {
		
		//Update the selections of those who need a refresh before filling
		
		sortByTypeAction.selectionChanged(selection);
		sortByNameAction.selectionChanged(selection);
		
		IMenuManager submenu =
			new MenuManager(ResourceNavigatorMessages.getString("ResourceNavigator.sort"));
		//$NON-NLS-1$
		menu.add(submenu);
		updateSortActions();
		submenu.add(sortByNameAction);
		submenu.add(sortByTypeAction);
		menu.add(filterAction);
	}

	/**
	 * Updates the checked state of the sort actions.
	 */
	public void updateSortActions() {
		int criteria = navigatorPart.getResourceSorter().getCriteria();
		if (sortByNameAction != null && sortByTypeAction != null) {
			sortByNameAction.setChecked(criteria == ResourceSorter.NAME);
			sortByTypeAction.setChecked(criteria == ResourceSorter.TYPE);
		}
	}

}
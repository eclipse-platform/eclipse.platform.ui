package org.eclipse.ui.views.navigator;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Implementation of the view sorting actions.
 */
/* package */ class SortViewAction extends ResourceNavigatorAction {
	private boolean sort = true;
/**
 * Creates the action.
 *
 * @param navigator the resource navigator
 * @param sortByType <code>true</code> for sort by type, <code>false</code> for sort by name
 */
public SortViewAction(ResourceNavigator navigator, boolean sortByType) {
	super(navigator, sortByType ? "by &Type" : "by &Name");
	setToolTipText("Sort the resources by " + (sortByType ? "type" : "name"));
	setEnabled(true);
	this.sort = sortByType;
}
public void run() {
	if (sort)
		getNavigator().setResourceSorter(new ResourceSorter(ResourceSorter.TYPE));
	else
		getNavigator().setResourceSorter(new ResourceSorter(ResourceSorter.NAME));
}
}

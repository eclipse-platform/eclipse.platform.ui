package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.help.*;

/**
 * Implementation of the view sorting actions.
 * @since 2.0
 */
public class SortViewAction extends ResourceNavigatorAction {
	private boolean sort = true;
/**
 * Creates the action.
 *
 * @param navigator the resource navigator
 * @param sortByType <code>true</code> for sort by type, <code>false</code> for sort by name
 */
public SortViewAction(IResourceNavigator navigator, boolean sortByType) {
	super(navigator, sortByType ? ResourceNavigatorMessages.getString("SortView.byType") : ResourceNavigatorMessages.getString("SortView.byName")); //$NON-NLS-2$ //$NON-NLS-1$
	if (sortByType) {
		setToolTipText(ResourceNavigatorMessages.getString("SortView.toolTipByType")); //$NON-NLS-1$
	} else {
		setToolTipText(ResourceNavigatorMessages.getString("SortView.toolTipByName")); //$NON-NLS-1$
	}
	setEnabled(true);
	this.sort = sortByType;
	WorkbenchHelp.setHelp(this, INavigatorHelpContextIds.SORT_VIEW_ACTION);
}
public void run() {
	if (sort)
		getNavigator().setSorter(new ResourceSorter(ResourceSorter.TYPE));
	else
		getNavigator().setSorter(new ResourceSorter(ResourceSorter.NAME));
}
}

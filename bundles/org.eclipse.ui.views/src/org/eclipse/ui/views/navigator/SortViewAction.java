/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Implementation of the view sorting actions.
 * @since 2.0
 */
public class SortViewAction extends ResourceNavigatorAction {
	private int sortCriteria = ResourceSorter.TYPE;

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
	sortCriteria = sortByType ? ResourceSorter.TYPE : ResourceSorter.NAME;
	WorkbenchHelp.setHelp(this, INavigatorHelpContextIds.SORT_VIEW_ACTION);
}
public void run() {
	ResourceSorter sorter = getNavigator().getSorter();
	
	if (sorter == null)
		getNavigator().setSorter(new ResourceSorter(sortCriteria));
	else {
		Viewer viewer = getNavigator().getViewer();
		viewer.getControl().setRedraw(false);
		sorter.setCriteria(sortCriteria);
		getNavigator().setSorter(sorter);
		viewer.refresh();
		viewer.getControl().setRedraw(true);		
	}		
}
}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.sync.actions;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.team.internal.ui.sync.views.ChangeFiltersContentProvider;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.dialogs.ListSelectionDialog;

/**
 * This action prompts the user in order to obtain sync view change filters
 */
public class FilterSyncViewerAction extends SyncViewerAction {

	SyncViewerChangeFilters filters;
	
	public FilterSyncViewerAction(IViewPart viewPart, SyncViewerChangeFilters filters) {
		super(viewPart, "Filter...");
		this.filters = filters;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		int[] filter = promptForFilter();
		if (filter == null) return;
		filters.refreshFilters();
	}

	/**
	 * @return
	 */
	private int[] promptForFilter() {
		ChangeFiltersContentProvider contentProvider = filters.getContentProvider();
		ListSelectionDialog dialog =
			new ListSelectionDialog(
				getShell(),
				this /* the input can be any object */,
				contentProvider,
				filters.getLabelProvider(),
				"Select the change types to be shown");

		dialog.setTitle("Synchronize View Filters");
		dialog.setInitialSelections(contentProvider.getInitialSelections());
		dialog.open();
		int[] changeFilters = null;
		if (dialog.getReturnCode() == Dialog.OK) {
			Object[] results = dialog.getResult();
			filters.setActiveFilters(results);
			changeFilters = filters.getChangeFilters();
		}
		return changeFilters;
	}
}

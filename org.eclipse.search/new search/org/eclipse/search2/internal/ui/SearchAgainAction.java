/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui;

import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

class SearchAgainAction extends Action {
	private SearchView fView;
	
	public SearchAgainAction(SearchView view) {
		setText(SearchMessages.getString("SearchAgainAction.label")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("SearchAgainAction.tooltip")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_TOOL, SearchPluginImages.IMG_TOOL_SEARCH);
		fView= view;	
	}

	public void run() {
		final ISearchResult search= fView.getCurrentSearchResult();
		if (search != null) {
			ISearchQuery query= search.getQuery();
			NewSearchUI.cancelQuery(query);
			if (query.canRerun()) {
				if (query.canRunInBackground())
					NewSearchUI.runQueryInBackground(query);
				else {
					ProgressMonitorDialog pmd= new ProgressMonitorDialog(fView.getSite().getShell());
					NewSearchUI.runQueryInForeground(pmd, query);
				}
			}
		}
	}
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;



/**
 * Invoke the resource creation wizard selection Wizard.
 * This action will retarget to the active view.
 */
class ShowSearchesAction extends Action {
	private SearchView fSearchView;


	/*
	 *	Create a new instance of this class
	 */
	public ShowSearchesAction(SearchView searchView) {
		super(SearchMessages.getString("ShowSearchesAction.label")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("ShowSearchesAction.tooltip")); //$NON-NLS-1$
		fSearchView= searchView;
	}
	 
	public void run() {
		QueryManager sm= InternalSearchUI.getInstance().getSearchManager();
		ISearchQuery[] queries= sm.getQueries();

		ArrayList input= new ArrayList();
		for (int j= 0; j < queries.length; j++) {
			ISearchResult search= queries[j].getSearchResult();
			input.add(search);
		}
		
		SearchesDialog dlg= new SearchesDialog(SearchPlugin.getActiveWorkbenchShell(),input);
		
		ISearchResult current= fSearchView.getCurrentSearchResult();
		if (current != null) {
			Object[] selected= new Object[1];
			selected[0]= current;
			dlg.setInitialSelections(selected);
		}
		if (dlg.open() == Window.OK) {
			List result= Arrays.asList(dlg.getResult());
			if (result != null && result.size() == 1) {
				fSearchView.showSearchResult((ISearchResult) result.get(0));
			}
		}

	}
}

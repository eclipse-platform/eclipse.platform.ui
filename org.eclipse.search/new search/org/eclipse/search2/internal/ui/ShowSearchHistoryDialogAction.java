/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.NewSearchUI;



/**
 * Invoke the resource creation wizard selection Wizard.
 * This action will retarget to the active view.
 */
class ShowSearchHistoryDialogAction extends Action {
	private SearchView fSearchView;


	/*
	 *	Create a new instance of this class
	 */
	public ShowSearchHistoryDialogAction(SearchView searchView) {
		super(SearchMessages.ShowSearchesAction_label);
		setToolTipText(SearchMessages.ShowSearchesAction_tooltip);
		fSearchView= searchView;
	}

	@Override
	public void run() {
		ISearchQuery[] queries= NewSearchUI.getQueries();

		ArrayList<ISearchResult> input= new ArrayList<>();
		for (ISearchQuery query : queries) {
			ISearchResult search= query.getSearchResult();
			input.add(search);
		}

		SearchHistorySelectionDialog dlg= new SearchHistorySelectionDialog(SearchPlugin.getActiveWorkbenchShell(),input);

		ISearchResult current= fSearchView.getCurrentSearchResult();
		if (current != null) {
			Object[] selected= new Object[1];
			selected[0]= current;
			dlg.setInitialSelections(selected);
		}
		if (dlg.open() == Window.OK) {
			Object[] result= dlg.getResult();
			if (result != null && result.length == 1) {
				ISearchResult searchResult= (ISearchResult) result[0];
				InternalSearchUI.getInstance().showSearchResult(fSearchView, searchResult, dlg.isOpenInNewView());
			}
		}

	}
}

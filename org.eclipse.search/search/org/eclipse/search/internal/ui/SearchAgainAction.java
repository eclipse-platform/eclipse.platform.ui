/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

class SearchAgainAction extends Action {

	public SearchAgainAction() {
		super(SearchPlugin.getResourceString("SearchResultView.searchAgain.text"));
		setToolTipText(SearchPlugin.getResourceString("SearchResultView.searchAgain.tooltip"));
	}
	
	public void run() {
		Search selected= SearchManager.getDefault().getCurrentSearch();
		if (selected != null)
			selected.searchAgain();
	}
}
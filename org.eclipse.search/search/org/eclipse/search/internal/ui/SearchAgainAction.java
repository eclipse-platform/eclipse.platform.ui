package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
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
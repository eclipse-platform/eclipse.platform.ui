package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.action.Action;

class RemoveAllSearchesAction extends Action {

	public RemoveAllSearchesAction() {
		super(SearchPlugin.getResourceString("SearchResultView.removeAllSearches.text"), SearchPluginImages.DESC_CLCL_SEARCH_REM_ALL);
		setToolTipText(SearchPlugin.getResourceString("SearchResultView.removeAllSearches.tooltip"));
	}
	
	public void run() {
		SearchManager.getDefault().removeAllSearches();
	}
}
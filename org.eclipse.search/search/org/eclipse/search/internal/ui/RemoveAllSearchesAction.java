/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

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
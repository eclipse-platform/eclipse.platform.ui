/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

class ShowNextResultAction extends Action {
	
	private SearchResultViewer fViewer;

	public ShowNextResultAction(SearchResultViewer viewer) {
		super(SearchPlugin.getResourceString("SearchResultView.showNext.text"), SearchPluginImages.DESC_CLCL_SEARCH_NEXT);
		setToolTipText(SearchPlugin.getResourceString("SearchResultView.showNext.tooltip"));
		fViewer= viewer;
	}
	
	public void run() {
		fViewer.showNextResult();
	}
}
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

class GotoMarkerAction extends Action {
	
	private SearchResultViewer fViewer;

	public GotoMarkerAction(SearchResultViewer viewer) {
		super(SearchPlugin.getResourceString("SearchResultView.gotoMarker.text"), SearchPluginImages.DESC_CLCL_SEARCH_GOTO);
		setToolTipText(SearchPlugin.getResourceString("SearchResultView.gotoMarker.tooltip"));
		fViewer= viewer;
	}
	
	public void run() {
		fViewer.showResult();
	}
}
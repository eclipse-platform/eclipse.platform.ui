package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
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
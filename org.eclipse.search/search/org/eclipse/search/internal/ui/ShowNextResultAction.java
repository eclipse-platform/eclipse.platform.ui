package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
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
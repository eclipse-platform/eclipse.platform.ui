/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

class ShowPreviousResultAction extends Action {
	
	private SearchResultViewer fViewer;

	public ShowPreviousResultAction(SearchResultViewer viewer) {
		super(SearchPlugin.getResourceString("SearchResultView.showPrev.text"));
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_PREV);
		setToolTipText(SearchPlugin.getResourceString("SearchResultView.showPrev.tooltip"));
		fViewer= viewer;
	}
	
	public void run() {
		fViewer.showPreviousResult();
	}
}
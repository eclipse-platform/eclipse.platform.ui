/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

class GotoMarkerAction extends Action {
	
	private SearchResultViewer fViewer;

	public GotoMarkerAction(SearchResultViewer viewer) {
		super(SearchMessages.getString("SearchResultView.gotoMarker.text")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_GOTO);
		setToolTipText(SearchMessages.getString("SearchResultView.gotoMarker.tooltip")); //$NON-NLS-1$
		fViewer= viewer;
	}
	
	public void run() {
		fViewer.showResult();
	}
}
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

class ShowNextResultAction extends Action {
	
	private SearchResultViewer fViewer;

	public ShowNextResultAction(SearchResultViewer viewer) {
		super(SearchMessages.getString("SearchResultView.showNext.text")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_NEXT);
		setToolTipText(SearchMessages.getString("SearchResultView.showNext.tooltip")); //$NON-NLS-1$
		fViewer= viewer;
	}
	
	public void run() {
		fViewer.showNextResult();
	}
}
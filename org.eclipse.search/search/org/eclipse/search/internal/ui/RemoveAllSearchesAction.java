/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

class RemoveAllSearchesAction extends Action {

	public RemoveAllSearchesAction() {
		super(SearchMessages.getString("SearchResultView.removeAllSearches.text")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_REM_ALL);		
		setToolTipText(SearchMessages.getString("SearchResultView.removeAllSearches.tooltip")); //$NON-NLS-1$
	}
	
	public void run() {
		SearchManager.getDefault().removeAllSearches();
	}
}
/*
 * (c) Copyright IBM Corp. 2000, 2003.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

class RemoveAllResultsAction extends Action {

	public RemoveAllResultsAction() {
		super(SearchMessages.getString("SearchResultView.removeAllResults.text")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_REM_ALL);		
		setToolTipText(SearchMessages.getString("SearchResultView.removeAllResults.tooltip")); //$NON-NLS-1$
	}
	
	public void run() {
		SearchManager.getDefault().removeAllResults();
	}
}
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

class SearchAgainAction extends Action {

	public SearchAgainAction() {
		super(SearchMessages.getString("SearchResultView.searchAgain.text")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("SearchResultView.searchAgain.tooltip")); //$NON-NLS-1$
	}
	
	public void run() {
		Search selected= SearchManager.getDefault().getCurrentSearch();
		if (selected != null)
			selected.searchAgain();
	}
}
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

class RemoveAllResultsAction extends Action {

	public RemoveAllResultsAction() {
		super(SearchMessages.getString("SearchResultView.removeAllResults.text")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("SearchResultView.removeAllResults.tooltip")); //$NON-NLS-1$
	}
	
	public void run() {
		SearchManager.getDefault().removeAllResults();
	}
}
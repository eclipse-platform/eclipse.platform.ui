/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui;

import org.eclipse.jface.action.Action;

import org.eclipse.search.ui.ISearchResult;

import org.eclipse.search.internal.ui.SearchPluginImages;

public class CancelSearchAction extends Action {

	private SearchView fView;

	public CancelSearchAction(SearchView view) {
		super(SearchMessages.getString("CancelSearchAction.label")); //$NON-NLS-1$
		fView= view;
		setToolTipText(SearchMessages.getString("CancelSearchAction.tooltip")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_CANCEL);
	}
	
	public void run() {
		ISearchResult result= fView.getCurrentSearchResult();
		if (result != null) {
			InternalSearchUI.getInstance().cancelSearch(result.getQuery());
		}
	}
}

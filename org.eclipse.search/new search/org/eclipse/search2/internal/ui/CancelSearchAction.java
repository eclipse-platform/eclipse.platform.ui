/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui;

import org.eclipse.jface.action.Action;

import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.ui.ISearchResult;

public class CancelSearchAction extends Action {

	private SearchView fView;

	public CancelSearchAction(SearchView view) {
		super(SearchMessages.CancelSearchAction_label);
		fView= view;
		setToolTipText(SearchMessages.CancelSearchAction_tooltip);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_CANCEL);
	}

	@Override
	public void run() {
		ISearchResult result= fView.getCurrentSearchResult();
		if (result != null) {
			InternalSearchUI.getInstance().cancelSearch(result.getQuery());
		}
	}
}

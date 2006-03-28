/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.search.internal.ui.SearchPluginImages;

class SearchAgainAction extends Action {
	private SearchView fView;
	
	public SearchAgainAction(SearchView view) {
		setText(SearchMessages.SearchAgainAction_label); 
		setToolTipText(SearchMessages.SearchAgainAction_tooltip); 
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_AGAIN);
		fView= view;	
	}

	public void run() {
		final ISearchResult search= fView.getCurrentSearchResult();
		if (search != null) {
			ISearchQuery query= search.getQuery();
			NewSearchUI.cancelQuery(query);
			if (query.canRerun()) {
				if (query.canRunInBackground())
					NewSearchUI.runQueryInBackground(query, fView);
				else {
					Shell shell= fView.getSite().getShell();
					ProgressMonitorDialog pmd= new ProgressMonitorDialog(shell);
					IStatus status= NewSearchUI.runQueryInForeground(pmd, query, fView);
					if (!status.isOK() && status.getSeverity() != IStatus.CANCEL) {
						ErrorDialog.openError(shell, SearchMessages.SearchAgainAction_Error_title, SearchMessages.SearchAgainAction_Error_message, status); 
					}
				}
			}
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.NewSearchUI;


class SearchAgainAction extends Action {
	private SearchView fView;

	public SearchAgainAction(SearchView view) {
		setText(SearchMessages.SearchAgainAction_label);
		setToolTipText(SearchMessages.SearchAgainAction_tooltip);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_REFRESH);
		fView= view;
	}

	@Override
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

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

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;

import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.MatchFilter;


public class MatchFilterSelectionAction extends Action {

	public static final String ACTION_ID= "MatchFilterSelectionAction"; //$NON-NLS-1$

	private AbstractTextSearchViewPage fPage;

	public MatchFilterSelectionAction(AbstractTextSearchViewPage page) {
		super(SearchMessages.MatchFilterSelectionAction_label);
		setId(ACTION_ID);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_FILTER);
		fPage= page;
	}

	@Override
	public void run() {
		Shell shell= fPage.getSite().getShell();

		AbstractTextSearchResult input= fPage.getInput();
		if (input == null) {
			return;
		}

		MatchFilter[] allFilters= input.getAllMatchFilters();
		MatchFilter[] checkedFilters= input.getActiveMatchFilters();
		Integer limit= fPage.getElementLimit();

		boolean enableMatchFilterConfiguration= checkedFilters != null;
		boolean enableLimitConfiguration= limit != null;
		int elementLimit= limit != null ? limit.intValue() : -1;

		MatchFilterSelectionDialog dialog = new MatchFilterSelectionDialog(shell, enableMatchFilterConfiguration, allFilters, checkedFilters, enableLimitConfiguration, elementLimit);
		if (dialog.open() == Window.OK) {
			if (enableMatchFilterConfiguration) {
				input.setActiveMatchFilters(dialog.getMatchFilters());
			}
			if (enableLimitConfiguration) {
				fPage.setElementLimit(Integer.valueOf(dialog.getLimit()));
			}
		}
	}

}

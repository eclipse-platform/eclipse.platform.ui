/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.IWorkbenchCommandConstants;

/**
 * @deprecated old search
 */
@Deprecated
class ShowPreviousResultAction extends Action {

	private SearchResultViewer fViewer;

	public ShowPreviousResultAction(SearchResultViewer viewer) {
		super(SearchMessages.SearchResultView_showPrev_text);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_PREV);
		setToolTipText(SearchMessages.SearchResultView_showPrev_tooltip);
		setActionDefinitionId(IWorkbenchCommandConstants.NAVIGATE_PREVIOUS);
		fViewer= viewer;
	}

	@Override
	public void run() {
		fViewer.showPreviousResult();
	}
}

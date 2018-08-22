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
class ShowNextResultAction extends Action {

	private SearchResultViewer fViewer;

	public ShowNextResultAction(SearchResultViewer viewer) {
		super(SearchMessages.SearchResultView_showNext_text);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_NEXT);
		setToolTipText(SearchMessages.SearchResultView_showNext_tooltip);
		fViewer= viewer;
		setActionDefinitionId(IWorkbenchCommandConstants.NAVIGATE_NEXT);
	}

	@Override
	public void run() {
		fViewer.showNextResult();
	}
}

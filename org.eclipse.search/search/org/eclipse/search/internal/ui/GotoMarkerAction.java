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
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

/**
 * @deprecated old search
 */
@Deprecated
class GotoMarkerAction extends Action {

	private SearchResultViewer fViewer;

	public GotoMarkerAction(SearchResultViewer viewer) {
		super(SearchMessages.SearchResultView_gotoMarker_text);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_GOTO);
		setToolTipText(SearchMessages.SearchResultView_gotoMarker_tooltip);
		fViewer= viewer;
	}

	@Override
	public void run() {
		fViewer.showResult();
	}
}

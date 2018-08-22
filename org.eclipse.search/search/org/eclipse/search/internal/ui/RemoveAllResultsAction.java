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
class RemoveAllResultsAction extends Action {

	public RemoveAllResultsAction() {
		super(SearchMessages.SearchResultView_removeAllResults_text);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_REM_ALL);
		setToolTipText(SearchMessages.SearchResultView_removeAllResults_tooltip);
	}

	@Override
	public void run() {
		SearchManager.getDefault().removeAllResults();
	}
}

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
package org.eclipse.search2.internal.ui.basic.views;

import org.eclipse.jface.action.Action;

import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;

import org.eclipse.search2.internal.ui.SearchMessages;

public class ShowNextResultAction extends Action {

	private AbstractTextSearchViewPage fPage;

	public ShowNextResultAction(AbstractTextSearchViewPage page) {
		super(SearchMessages.ShowNextResultAction_label);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_NEXT);
		setToolTipText(SearchMessages.ShowNextResultAction_tooltip);
		fPage= page;
	}

	@Override
	public void run() {
		fPage.gotoNextMatch();
	}
}

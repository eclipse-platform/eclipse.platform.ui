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
package org.eclipse.search2.internal.ui.basic.views;

import org.eclipse.jface.action.Action;

import org.eclipse.search.ui.text.*;

import org.eclipse.search.internal.ui.SearchPluginImages;

import org.eclipse.search2.internal.ui.SearchMessages;

public class RemoveMatchAction extends Action {

	private AbstractTextSearchViewPage fPage;

	public RemoveMatchAction(AbstractTextSearchViewPage page) {
		fPage= page;
		setText(SearchMessages.getString("RemoveMatchAction.label")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("RemoveMatchAction.tooltip")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_REM);
	}
	
	public void run() {
		Match match= fPage.getCurrentMatch();
		AbstractTextSearchResult result= fPage.getInput();
		if (match != null && result != null)
			result.removeMatch(match);
	}
	
}

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
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

class RemoveAllSearchesAction extends Action {

	public RemoveAllSearchesAction() {
		super(SearchMessages.getString("SearchResultView.removeAllSearches.text")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_REM_ALL);		
		setToolTipText(SearchMessages.getString("SearchResultView.removeAllSearches.tooltip")); //$NON-NLS-1$
	}
	
	public void run() {
		SearchManager.getDefault().removeAllSearches();
	}
}

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

class ShowNextResultAction extends Action {
	
	private SearchResultViewer fViewer;

	public ShowNextResultAction(SearchResultViewer viewer) {
		super(SearchMessages.getString("SearchResultView.showNext.text")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_NEXT);
		setToolTipText(SearchMessages.getString("SearchResultView.showNext.tooltip")); //$NON-NLS-1$
		fViewer= viewer;
		setActionDefinitionId("org.eclipse.ui.navigate.next"); //$NON-NLS-1$
	}
	
	public void run() {
		fViewer.showNextResult();
	}
}

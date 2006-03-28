/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

/**
 * @deprecated old search
 */
class ShowPreviousResultAction extends Action {
	
	private SearchResultViewer fViewer;

	public ShowPreviousResultAction(SearchResultViewer viewer) {
		super(SearchMessages.SearchResultView_showPrev_text); 
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_PREV);
		setToolTipText(SearchMessages.SearchResultView_showPrev_tooltip); 
		setActionDefinitionId("org.eclipse.ui.navigate.previous"); //$NON-NLS-1$
		fViewer= viewer;
	}
	
	public void run() {
		fViewer.showPreviousResult();
	}
}

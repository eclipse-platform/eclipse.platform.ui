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

class GotoMarkerAction extends Action {
	
	private SearchResultViewer fViewer;

	public GotoMarkerAction(SearchResultViewer viewer) {
		super(SearchMessages.getString("SearchResultView.gotoMarker.text")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_GOTO);
		setToolTipText(SearchMessages.getString("SearchResultView.gotoMarker.tooltip")); //$NON-NLS-1$
		fViewer= viewer;
	}
	
	public void run() {
		fViewer.showResult();
	}
}

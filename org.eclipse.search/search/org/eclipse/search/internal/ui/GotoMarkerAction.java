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
class GotoMarkerAction extends Action {
	
	private SearchResultViewer fViewer;

	public GotoMarkerAction(SearchResultViewer viewer) {
		super(SearchMessages.SearchResultView_gotoMarker_text); 
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_GOTO);
		setToolTipText(SearchMessages.SearchResultView_gotoMarker_tooltip); 
		fViewer= viewer;
	}
	
	public void run() {
		fViewer.showResult();
	}
}

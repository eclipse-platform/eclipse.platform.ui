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
package org.eclipse.search2.internal.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.search.ui.ISearchResult;


class ShowSearchAction extends Action {
	private ISearchResult fSearch;
	private SearchView fView;
	
	/**
	 *	Create a new instance of this class
	 */
	public ShowSearchAction(SearchView view, ISearchResult search, String text, ImageDescriptor image, String tooltip) {
		fSearch= search;
		fView= view;
		// fix for bug 38049
		if (text.indexOf('@') >= 0)
			text+= '@';
		setText(text);
		setImageDescriptor(image);
		setToolTipText(tooltip);
	}
	
	public void run() {
		fView.showSearchResult(fSearch);
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.basic.views;

import org.eclipse.search.ui.ISearchResultChangedListener;
import org.eclipse.search.ui.text.IStructureProvider;
import org.eclipse.search.ui.text.ITextSearchResult;


/**
 * @author Thomas Mäder
 *
 */
public class SearchResultTableModel extends SearchResultModel implements ISearchResultChangedListener {

	public SearchResultTableModel(DefaultSearchViewPage page, ITextSearchResult result) {
		super(result, page);
	}
	

	protected void remove(IStructureProvider structureProvider, Object child, boolean refreshViewer) {
		if (refreshViewer) {
			if (fResult.getMatchCount(child) == 0) {
				fPage.handleRemove(child);
			}
		} else {
				fPage.handleUpdate(child);
		}
	}

	protected void insert(IStructureProvider structureProvider, Object child, boolean refreshViewer) {
		if (refreshViewer) {
			if (fResult.getMatchCount(child) > 1)
				fPage.handleUpdate(child);
			else {
				fPage.handleInsert(child);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.internal.ui.basic.views.SearchResultModel#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		if (parent == this)
			return fResult.getElements();
		return EMPTY_ARRAY;
	}
}

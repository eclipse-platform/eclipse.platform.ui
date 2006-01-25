/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.core.tests;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;

import org.eclipse.search.internal.ui.text.FileSearchResult;

public class NullSearchResult extends FileSearchResult { // inherit from FileSearchResult so a search result view can be found

	private final NullQuery fNullQuery;
	public NullSearchResult(NullQuery query) {
		super(null);
		fNullQuery= query;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getText(org.eclipse.search.ui.ISearchResult)
	 */
	public String getLabel() {
		return "Null Query"; //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getTooltip(org.eclipse.search.ui.ISearchResult)
	 */
	public String getTooltip() {
		return "Null Query"; //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getImageDescriptor(org.eclipse.search.ui.ISearchResult)
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getQuery()
	 */
	public ISearchQuery getQuery() {
		return fNullQuery;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchResult#getEditorMatchAdapter()
	 */
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchResult#getFileMatchAdapter()
	 */
	public IFileMatchAdapter getFileMatchAdapter() {
		return null;
	}
}

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
package org.eclipse.search.core.tests;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorPart;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

/**
 * @author Thomas Mäder
 *
 */
public class NullSearchResult extends AbstractTextSearchResult {
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractSearchResult#findContainedMatches(org.eclipse.search.ui.text.AbstractSearchResult, org.eclipse.core.resources.IFile)
	 */
	public Match[] findContainedMatches(IFile file) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractSearchResult#getFile(java.lang.Object)
	 */
	public IFile getFile(Object element) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractSearchResult#isShownInEditor(org.eclipse.search.ui.text.Match, org.eclipse.ui.IEditorPart)
	 */
	public boolean isShownInEditor(Match match, IEditorPart editor) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractSearchResult#findContainedMatches(org.eclipse.search.ui.text.AbstractSearchResult, org.eclipse.ui.IEditorPart)
	 */
	public Match[] findContainedMatches(IEditorPart editor) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getText(org.eclipse.search.ui.ISearchResult)
	 */
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getTooltip(org.eclipse.search.ui.ISearchResult)
	 */
	public String getTooltip() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getImageDescriptor(org.eclipse.search.ui.ISearchResult)
	 */
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getQuery()
	 */
	public ISearchQuery getQuery() {
		// TODO Auto-generated method stub
		return null;
	}
}

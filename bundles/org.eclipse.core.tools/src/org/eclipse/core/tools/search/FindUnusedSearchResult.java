/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.tools.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tools.search.FindUnusedMembers.IResultReporter;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.*;

/**
 * 
 */
public class FindUnusedSearchResult extends AbstractTextSearchResult implements IResultReporter {

	private final FindUnusedSearchQuery fQuery;

	public FindUnusedSearchResult(FindUnusedSearchQuery query) {
		fQuery = query;
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

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getLabel()
	 */
	public String getLabel() {
		if (getMatchCount() == 1) {
			return "1 unused element found"; //$NON-NLS-1$
		} else {
			return getMatchCount() + " unused elements found"; //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getQuery()
	 */
	public ISearchQuery getQuery() {
		return fQuery;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getTooltip()
	 */
	public String getTooltip() {
		return getLabel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tools.search.FindUnusedMembers.IResultReporter#unusedElementFound(org.eclipse.jdt.core.IMember)
	 */
	public void unusedElementFound(IMember member) throws CoreException {
		ISourceRange nameRange = member.getNameRange();
		addMatch(new Match(member, nameRange.getOffset(), nameRange.getLength()));
	}

}

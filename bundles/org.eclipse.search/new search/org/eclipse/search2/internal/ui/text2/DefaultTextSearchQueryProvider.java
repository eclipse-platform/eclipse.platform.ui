/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Christian Walther (Indel AG) - Bug 399094: Add whole word option to file search
 *******************************************************************************/
package org.eclipse.search2.internal.ui.text2;

import org.eclipse.core.runtime.Assert;

import org.eclipse.core.resources.IResource;

import org.eclipse.ui.IWorkingSet;

import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.TextSearchQueryProvider;


public class DefaultTextSearchQueryProvider extends TextSearchQueryProvider {

	@Override
	public ISearchQuery createQuery(TextSearchInput input) {
		FileTextSearchScope scope= input.getScope();
		String text= input.getSearchText();
		boolean regEx= input.isRegExSearch();
		boolean caseSensitive= input.isCaseSensitiveSearch();
		boolean wholeWord= input.isWholeWordSearch();
		boolean searchInBinaries= input.searchInBinaries();
		Assert.isLegal(!(wholeWord && regEx));
		return new FileSearchQuery(text, regEx, caseSensitive, wholeWord, searchInBinaries, scope);
	}

	@Override
	public ISearchQuery createQuery(String searchForString) {
		FileTextSearchScope scope= FileTextSearchScope.newWorkspaceScope(getPreviousFileNamePatterns(), false);
		return new FileSearchQuery(searchForString, false, true, scope);
	}

	@Override
	public ISearchQuery createQuery(String selectedText, IResource[] resources) {
		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(resources, getPreviousFileNamePatterns(), false);
		return new FileSearchQuery(selectedText, false, true, scope);
	}

	@Override
	public ISearchQuery createQuery(String selectedText, IWorkingSet[] ws) {
		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(ws, getPreviousFileNamePatterns(), false);
		return new FileSearchQuery(selectedText, false, true, scope);
	}

	private String[] getPreviousFileNamePatterns() {
		return new String[] { "*" }; //$NON-NLS-1$
	}

}

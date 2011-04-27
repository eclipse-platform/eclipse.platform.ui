/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.search.ui.text;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IResource;

import org.eclipse.ui.IWorkingSet;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.ISearchQuery;

/**
 * Abstract base class for text search query providers supplied via the <code>org.eclipse.search.textSearchQueryProvider</code>
 * extension point. The plug-in preference <code>org.eclipse.search.textSearchQueryProvider<code> defines the preferred
 * query provider. It is intended that only products choose a preferred query provider.
 *
 * @since 3.2
 */
public abstract class TextSearchQueryProvider {

	/**
	 * Specified the input for a search query.
     * <p>
     * Clients may instantiate this class.
     * </p>
	 */
	public static abstract class TextSearchInput {

		/**
		 * Returns the search text to search for.
		 *
		 * @return the search text, depending on {@link #isRegExSearch()} the search text represents a regular expression
		 * or a pattern using '*' and '?' as wildcards. The empty search text signals a file name search.
		 */
		public abstract String getSearchText();

		/**
		 * Returns whether the search is a case sensitive search or not.
		 *
		 * @return whether the pattern is to be used case sensitive or not.
		 */
		public abstract boolean isCaseSensitiveSearch();

		/**
		 * Returns whether the search text denotes a regular expression or not.
		 *
		 * @return whether the pattern denotes a regular expression.
		 */
		public abstract boolean isRegExSearch();

		/**
		 * Returns the scope for the search
		 *
		 * @return the scope for the search
		 */
		public abstract FileTextSearchScope getScope();

	}

	/**
	 * Returns the preferred query provider. The preferred query provider is typically configured by the product
	 * and defined by the search plug-in preference 'org.eclipse.search.textSearchQueryProvider'.
	 * It is not intended that query providers change at runtime, but clients should always use this method to access
	 * the query provider.
	 *
	 * @return the preferred {@link TextSearchQueryProvider}.
	 */
	public static TextSearchQueryProvider getPreferred() {
		return SearchPlugin.getDefault().getTextSearchQueryProviderRegistry().getPreferred();
	}

	/**
	 * Create a query for the input with the given information.
	 *
	 * @param textSearchInput the search input
	 * @return returns the created search query
	 * @throws CoreException a {@link CoreException} can be thrown when the query provider can not
	 * create a query for the given input.
	 */
	public abstract ISearchQuery createQuery(TextSearchInput textSearchInput) throws CoreException;

	/**
	 * Create a query to search for the selected text in the workspace.
	 *
	 * @param selectedText the text to search for
	 * @return returns the created search query
	 * @throws CoreException a {@link CoreException} can be thrown when the query provider can not
	 * create a query for the given input.
	 */
	public abstract ISearchQuery createQuery(String selectedText) throws CoreException;

	/**
	 * Create a query to search for the selected text in the given resources.
	 *
	 * @param selectedText the text to search for
	 * @param resources the resources to search in
	 * @return returns the created search query
	 * @throws CoreException a {@link CoreException} can be thrown when the query provider can not
	 * create a query for the given input.
	 */
	public abstract ISearchQuery createQuery(String selectedText, IResource[] resources) throws CoreException;

	/**
	 * Create a query to search for the selected text in the given working sets.
	 *
	 * @param selectedText the text to search for
	 * @param ws the working sets to search in
	 * @return returns the created search query
	 * @throws CoreException a {@link CoreException} can be thrown when the query provider can not
	 * create a query for the given input.
	 */
	public abstract ISearchQuery createQuery(String selectedText, IWorkingSet[] ws) throws CoreException;
}

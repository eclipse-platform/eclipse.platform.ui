/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.ui;
/**
 * This class manages the set of search results.
 * 
 * This API is preliminary and subject to change at any time.
 * 
 * @since 3.0
 */
public interface ISearchResultManager {
	/**
	 * Returns all search results know to this ISearchResultManager.
	 * 
	 * @return All search result.
	 */
	ISearchResult[] getSearchResults();
	/**
	 * Removes the given search result from this manager. Does nothing if the
	 * search result is not found
	 * 
	 * @param search
	 */
	void removeSearchResult(ISearchResult searchResult);
	/**
	 * Adds the given search result to the search manager Does nothing if the
	 * search result is already present.
	 * 
	 * @param searchResult
	 */
	void addSearchResult(ISearchResult searchResult);
	/**
	 * Adds the given listener. Does nothing when the listener is already
	 * present.
	 * 
	 * @param l The listener to be added.
	 */
	public void addSearchResultListener(ISearchResultManagerListener l);
	/**
	 * Removes the give ISearchResultListener. Does nothing if the listener is
	 * not present.
	 * 
	 * @param l The listener to be removed.
	 */
	public void removeSearchResultListener(ISearchResultManagerListener l);
	/**
	 * Removes all <code>ISearchResults</code> from search manager.
	 */
	void removeAll();
}

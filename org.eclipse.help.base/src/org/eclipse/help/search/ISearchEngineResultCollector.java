/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.search;

/**
 * A collector for the search hits (asynchronously) returned by the help search
 * participants.
 * 
 * @since 3.1
 */
public interface ISearchEngineResultCollector {
	/**
	 * Adds a new search result object.
	 * 
	 * @param searchResult
	 *            the new search result
	 */
	void add(ISearchEngineResult searchResult);

	/**
	 * Adds an array of new search results.
	 * 
	 * @param searchResults
	 *            an array of search result objects
	 */
	void add(ISearchEngineResult[] searchResults);
}
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
package org.eclipse.search.ui.text;

import org.eclipse.ui.IViewPart;

import org.eclipse.search.ui.ISearchResult;


/**
 * This interface is not to be implemented.
 * An extension of ISearchResult that contains textual matches.
 * This API is preliminary and subject to change at any time.
 * @since 3.0
 */
public interface ITextSearchResult extends ISearchResult {
	/**
	 * Adds a match to this search result.
	 * If the search result already contains the match, does nothing.
	 * @param match
	 */
	void addMatch(Match match);
	/**
	 * Removes the given match from this search result. Does nothing
	 * if the search result doesn't contain the match. 
	 * @param match
	 */
	void removeMatch(Match match);
	/**
	 * Removes all matches from this search result.
	 */
	void removeAll();
	/**
	 * @return The number of matches in this search result.
	 */
	int getMatchCount();
	/**
	 * Returns the number of matches reported against the given 
	 * element.
	 * @param element The element to get the match count for.
	 * @return The number of matches.
	 */
	int getMatchCount(Object element);
	/**
	 * Returns an array of all elements occurring in the matches
	 * in this search result.
	 * @return The array of elements in this search result.
	 */
	public Object[] getElements();
	/**
	 * Returns an array of all matches reported against the 
	 * given element.
	 * @param element The element to get the matches for.
	 * @return All matches reported against the given element.
	 */
	Match[] getMatches(Object element);
	/**
	 * Creates the element presentation for this search result.
	 * @param searchView The view the presentation will be used in.
	 * @return A new presentation instance.
	 */
	ISearchElementPresentation createElementPresentation(IViewPart searchView);
	/**
	 * Returns the structure provider for this search result.
	 * Note that this does not have to be a new instance per call.
	 * @return The structure provider for this search result.
	 */
	IStructureProvider getStructureProvider();
}

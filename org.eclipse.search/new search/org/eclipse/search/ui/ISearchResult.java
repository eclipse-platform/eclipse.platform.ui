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
package org.eclipse.search.ui;

import org.eclipse.ui.IViewPart;

/**
 * Represents the result of a search. No assumptions about the
 * structure of these results is made at this level.
 * Clients may implement this interface. In fact the choice of 
 * which org.eclipse.search2.ui.ISearchResultsPage is used
 * to present the ISearchResult is based on the type of search
 * result a client creates.
 *
 * This API is preliminary and subject to change at any time.
 * @since 3.0
 */
public interface ISearchResult {
	/**
	 * @return Some user data object defined by the creator of the
	 * 			ISearchResult. Typically used to remember stuff like
	 * 			the description of the search result.
	 */
	Object getUserData();
	/**
	 * Creates a new search result presentation. May be called multiple times.
	 * @see ISearchResultPresentation
	 * @param searchView
	 * @return
	 */
	ISearchResultPresentation createPresentation(IViewPart searchView);
	/**
	 * Will be called at the start of a search job that fills
	 * A corresponding SearchJobEvent should be sent out.
	 * this ISearchResult.
	 */
	void jobStarted();
	/**
	 * Will be called at the end of a search job that fills
	 * this ISearchResult.
	 * A corresponding SearchJobEvent should be sent out.
	 */
	void jobFinished();
	/**
	 * @return Whether a search is presently running on this
	 * search result.
	 */
	boolean isRunning();
	/**
	 * Adds a ISearchResultChangedListener.
	 * @param l
	 */
	public void addListener(ISearchResultChangedListener l);
	/**
	 * Removes a ISearchResultChangedListener.
	 * @param l
	 */
	public void removeListener(ISearchResultChangedListener l);
}

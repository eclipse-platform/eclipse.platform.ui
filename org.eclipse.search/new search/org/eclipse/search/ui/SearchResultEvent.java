/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.ui;
/**
 * The common superclass of all events sent from ISearchResults. This class is
 * supposed to be subclassed to provide more specific notification.
 * @see org.eclipse.search.ui.ISearchResultListener#searchResultChanged(SearchResultEvent)
 * 
 * This API is preliminary and subject to change at any time.
 * 
 * @since 3.0
 */
public abstract class SearchResultEvent {
	protected ISearchResult fSearchResult;
	protected SearchResultEvent(ISearchResult searchResult) {
		fSearchResult= searchResult;
	}
	protected SearchResultEvent() {
		this(null);
	}
	/**
	 * Gets the <code>ISearchResult</code> for this event.
	 * 
	 * @return The source of this event.
	 */
	public ISearchResult getSearchResult() {
		return fSearchResult;
	}
	/**
	 * Sets the search result for this event.
	 * @param searchResult The searchResult to set.
	 */
	protected void setSearchResult(ISearchResult searchResult) {
		fSearchResult= searchResult;
	}
}

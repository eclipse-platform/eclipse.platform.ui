/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.ui;

import java.util.EventObject;

/**
 * The common superclass of all events sent from <code>ISearchResults</code>.
 * This class is supposed to be subclassed to provide more specific
 * notification.
 *
 * @see org.eclipse.search.ui.ISearchResultListener#searchResultChanged(SearchResultEvent)
 *
 * @since 3.0
 */
public abstract class SearchResultEvent extends EventObject {

	private static final long serialVersionUID= -4877459368182725252L;


	/**
	 * Creates a new search result event for the given search result.
	 *
	 * @param searchResult the source of the event
	 */
	protected SearchResultEvent(ISearchResult searchResult) {
		super(searchResult);
	}

	/**
	 * Gets the <code>ISearchResult</code> for this event.
	 *
	 * @return the source of this event
	 */
	public ISearchResult getSearchResult() {
		return (ISearchResult) getSource();
	}
}

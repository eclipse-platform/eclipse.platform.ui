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
package org.eclipse.search.ui;

import org.eclipse.jface.resource.ImageDescriptor;

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
	 * Adds a ISearchResultChangedListener. Has no effect when the 
	 * listener has already been added.
	 * @param l
	 */
	public void addListener(ISearchResultListener l);
	/**
	 * Removes a ISearchResultChangedListener. Has no effect when the
	 * listener hasn't previously been added.
	 * @param l
	 */
	public void removeListener(ISearchResultListener l);
	/**
	 * Returns a user readeable label for the given ISearchResult.
	 * @return The label for this search result.
	 */
	String getText();
	/**
	 * Returns a tooltip for the given ISearchResult.
	 * @return A user readeable String.
	 */
	public String getTooltip();
	/**
	 * Returns an image descriptor for the given ISearchResult.
	 * The image descriptor will be used for rendereing in menus, 
	 * view titles, etc.
	 * @return An image representing this search result or <code>null</code>.
	 */
	ImageDescriptor getImageDescriptor();
	
	/**
	 * Returns the query that is responsible for putting matches for this
	 * search result.
	 * @return The query producing this result or <code>null</code>
	 */
	ISearchQuery getQuery();
}

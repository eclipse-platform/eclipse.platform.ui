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
 * Represents the result of a search. 
 * 
 * TODO 
 *   - what is the result of a search. Some words that the representation of matches
 *     are up to implementor of this calls.
 * 
 * The class of an <code>ISearchResult</code> 
 * is used to determine which <code>ISearchResultPage</code> is used
 * to display it, by matching the <code>targetClass</code> attribute in the 
 * <code>searchResultViewPages</code> extension point to the class of the search 
 * result.
 * 
 * TODO This is not really understandable... I would say something like. To present
 *  search results to the user implementors of this interface must also provide 
 *  an extension for the extension point <code>org.eclipse.search.searchResultViewPage</code>
 * 
 * @see org.eclipse.search.ui.ISearchResultPage
 * 
 *  * Clients may implement this interface.
 *
 * This API is preliminary and subject to change at any time.
 * @since 3.0
 */
public interface ISearchResult {
	/**
	 * Adds a <code>ISearchResultListener</code>. Has no effect when the 
	 * listener has already been added.
	 * @param l The listener to be added
	 */
	public void addListener(ISearchResultListener l);
	/**
	 * Removes a <code>ISearchResultChangedListener</code>. Has no effect when the
	 * listener hasn't previously been added.
	 * @param l The listener to be removed.
	 */
	public void removeListener(ISearchResultListener l);
	/**
	 * Returns a user readable label for this search result.
	 * @return The label for this search result.
	 */
	String getLabel();
	/**
	 * Returns a tooltip to be used when this search result is shown in the UI.
	 * @return A user readable String.
	 */
	public String getTooltip();
	/**
	 * Returns an image descriptor for the given ISearchResult.
	 * @return An image representing this search result or <code>null</code>.
	 */
	ImageDescriptor getImageDescriptor();
	
	/**
	 * Returns the query that produced this search result.
	 * @return The query producing this result.
	 */
	ISearchQuery getQuery();
}

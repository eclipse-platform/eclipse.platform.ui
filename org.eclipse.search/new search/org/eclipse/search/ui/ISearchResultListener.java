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

/**
 * A listener for changes to the set of ISearchResults
 * This API is preliminary and subject to change at any time.
 *
 * @since 3.0
 */
public interface ISearchResultListener {
	/**
	 * Called when an ISearchResult has been added to the 
	 * ISearchResultManager.
	 * @param search The search result that has been added
	 */
	void searchResultAdded(ISearchResult search);
	/**
	 * Called when an ISearchResult has been removed from the 
	 * ISearchResultManager.
	 * @param search The search result that has been removed
	 */
	void searchResultRemoved(ISearchResult search);
}	

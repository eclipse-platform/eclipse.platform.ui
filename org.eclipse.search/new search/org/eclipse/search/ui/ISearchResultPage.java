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

import org.eclipse.ui.part.IPageBookViewPage;


/**
 * Extensions of extension point org.eclipse.search.ui.searchResultViewPages
 * must implement this interface
 * This API is preliminary and subject to change at any time.
 * @since 3.0
 */
public interface ISearchResultPage extends IPageBookViewPage {
	/**
	 * Sets the search result to be shown in this search 
	 * results page.
	 * @param search The search to be shown
	 * @param viewState The previously saved view state.
	 * @return Returns the visual state for the previously shown search.
	 * 		   May return null if no view state can or should be saved.
	 */
	void setInput(ISearchResult search, Object uiState);
	/**
	 * returns an Object representing the current state of the page
	 * UI.
	 * @return An object representing the UI state
	 */
	Object getUIState();
	/**
	 * Sets the search view this search results page is shown
	 * in.
	 * @param part The parent search view.
	 */
	void setViewPart(ISearchResultPageContainer part);
}

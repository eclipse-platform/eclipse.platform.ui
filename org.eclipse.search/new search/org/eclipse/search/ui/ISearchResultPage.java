/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.ui;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.part.IPageBookViewPage;
/**
 * TODO 
 *  - what is a search result page. Its presneted in the search result view..
 * 
 * Extensions of extension point org.eclipse.search.ui.searchResultViewPages
 * must implement this interface.
 * When the user selects an <code>ISearchResult</code> in the search results
 * view, the corresponding (as configured in the extension) <code>ISearchResultPage</code>
 * will be used to display the search result.
 * Clients may implement this interface.
 * 
 * This API is preliminary and subject to change at any time.
 * 
 * @since 3.0
 */
public interface ISearchResultPage extends IPageBookViewPage {
	/**
	 * Returns an Object representing the current user interface state of the page. For
	 * example, the current selection in a viewer. The UI state will be later
	 * passed into the <code>setInput()</code> method when the currently
	 * shown <code>ISearchResult</code> is shown again.
	 * 
	 * @return an object representing the UI state of this page
	 */
	Object getUIState();
	/**
	 * Sets the search result to be shown in this search results page. Implementers
	 * should restore UI state (e.g. selection) from the previously saved <code>uiState</code>
	 * object.
	 * @see ISearchResultPage#getUIState()
	 * 
	 * @param search the search result to be shown
	 * @param uiState the previously saved UI state
	 */
	void setInput(ISearchResult search, Object uiState);
	/**
	 * Sets the search view this search results page is shown in. This method will be called before the page is shown
	 * for the first time (i.e. before the page control is created).
	 * 
	 * @param part the parent search view.
	 */
	void setViewPart(ISearchResultViewPart part);
	
	/**
	 * Restores the page state. Note that this applies only to state that is saved
	 * across sessions. For restoring state that is transient: 
	 * 
	 * @see #setInput(ISearchResult, Object)
	 * 
	 * @param memento a memento to restore the page state from or <code>null</code>
	 *                if no previous state was saved.
	 */
	void restoreState(IMemento memento);
	
	/**
	 * Saves the page state in a memento. Note that this applies to state that
	 * should persist across sessions. For transient state: 
	 * 
	 * @see #getUIState()
	 * 
	 * @param memento a memento to receive the object state
	 */
	void saveState(IMemento memento);
	
	/**
	 * Sets the id for this page. This method will be called before any other initialization is done. 
	 * @param id assigns an id to this page.
	 */
	void setID(String id);
	/**
	 * Returns the id set via <code>setID</code>.
	 * @return the id of this page
	 */
	String getID();
}

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.ui;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IViewPart;
/**
 * Interface for the search result view. The search result view part is responsible
 * for managing the set of search result and delegates display of search results
 * to the appropriate <code>ISearchResultPage</code>.
 * This insterface must not be implemented by clients.
 * 
 * TODO the standard sentence is
 *  - This interface is not intended to be implemented by clients.
 *  - How do I access an instance of the search result view part. NewSearchUI ??
 * 
 * This API is preliminary and subject to change at any time.
 * 
 * @since 3.0
 */
public interface ISearchResultViewPart extends IViewPart {
	/**
	 * Search result pages should call this method to have the search results view
	 * contribute to their context menus.
	 * 
	 * @param menuManager the menu manager the search result view should contribute to.
	 */
	void fillContextMenu(IMenuManager menuManager);
	/**
	 * Returns the <code>ISearchResultPage</code> currently shown in this 
	 * search view. Returns <code>null</code> if no page is currently shown.
	 * @return the active <code>ISearchResultPage</code> or <code>null</code>
	 */
	ISearchResultPage getActivePage();
}

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.ui;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IInputSelectionProvider;

/**
 * Specify how clients can add menu items
 * to the context menu of the search result view.
 * A class that contributes context menu items
 * must implement this interface and pass an
 * instance of itself to the search result view.
 * 
 * @see	ISearchResultView#searchStarted
 */
public interface IContextMenuContributor {

	/**
	 * Contributes menu items to the given context menu appropriate for the
	 * given selection.
	 *
	 * @param menu		the menu to which the items are added
	 * @param inputProvider	the selection and input provider
	 */
	public void fill(IMenuManager menu, IInputSelectionProvider inputProvider);
}
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
 * @deprecated Subclass {@link org.eclipse.search.ui.text.AbstractTextSearchViewPage} instead.
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

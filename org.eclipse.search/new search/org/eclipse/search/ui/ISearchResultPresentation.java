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

import org.eclipse.jface.resource.ImageDescriptor;


/**
 * A search result presentation allows the search UI to 
 * create UI elements for a give ISearchResult.
 * This API is preliminary and subject to change at any time.
 * @since 3.0
 */
public interface ISearchResultPresentation {
	/**
	 * Returns a user readeable label for the given ISearchResult.
	 * @param search A user readeable String.
	 * @return
	 */
	String getText(ISearchResult search);
	/**
	 * Returns a tooltip label for the given ISearchResult.
	 * @param search A user readeable String.
	 * @return
	 */
	String getTooltip(ISearchResult search);
	/**
	 * Returns an image descriptor for the given ISearchResult.
	 * The image descriptor will be used for rendereing in menus, 
	 * view titles, etc.
	 * @param search An image descriptor.
	 * @return
	 */
	ImageDescriptor getImageDescriptor(ISearchResult search);
	/**
	 * Free all resources owned by this ISearchResultPresentation
	 */
	void dispose();
}

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
package org.eclipse.search.ui.text;

import org.eclipse.ui.IViewPart;

import org.eclipse.search.ui.ISearchResultPresentation;


/**
 * A factory class for search and search element presentations
 * This API is preliminary and subject to change at any time.
 * @since 3.0
 */
public interface IPresentationFactory {
	/**
	 * Creates a new ISearchResultPresentation.  
	 * @param view The view the presentation will be used in.
	 * @return A new instance of the presentation.
	 * @see ISearchResultPresentation
	 */
	public ISearchResultPresentation createSearchResultPresentation(IViewPart view);
	/**
	 * Creates a new ISearchElementPresentation.  
	 * @param view The view the presentation will be used in.
	 * @return A new instance of the presentation.
	 * @see ISearchElementPresentation
	 */
	public ISearchElementPresentation createElementPresentation(IViewPart view);
}

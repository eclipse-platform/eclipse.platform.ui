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

import org.eclipse.search2.internal.ui.text.SearchResult;

/**
 * Factory class to construct instances of ITextSearchResult.
 * This API is preliminary and subject to change at any time.
 * @since 3.0
 */
public class TextSearchResultFactory {

	/**
	 * creates a new ITextSearchResult instance.
	 * @param structureProvider The structure provider to be used
	 * @param factory The presentation factory to be used.
	 * @param userData An arbitrary object to be stored with the search result.
	 * @return
	 */
	public static ITextSearchResult createTextSearchResult(IStructureProvider structureProvider, IPresentationFactory factory, Object userData) {
		return new SearchResult(structureProvider, factory, userData);
	}

}

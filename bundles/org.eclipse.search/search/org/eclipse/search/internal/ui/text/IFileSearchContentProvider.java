/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import org.eclipse.search.ui.text.AbstractTextSearchResult;

public interface IFileSearchContentProvider {

	public abstract void elementsChanged(Object[] updatedElements);

	public abstract void clear();

	/**
	 * Counts the leaf elements the viewer shows, elements hidden because the element
	 * limit
	 * ({@link org.eclipse.search.ui.text.AbstractTextSearchViewPage#getElementLimit()})
	 * is exceeded are not counted. Depending on the layout and the kind of the
	 * search a leaf element is either a file (flat layout or file name search) or a
	 * matching line (tree layout of a text search).
	 * <p>
	 * The count is computed on the model and not on the viewer, so it is also
	 * correct if the viewer isn't populated (yet).
	 * </p>
	 *
	 * @param parentElement parent element or input
	 * @return number of leaf elements shown in the viewer
	 */
	public abstract int getLeafCount(Object parentElement);

	/**
	 * @param parentElement parent element or input
	 * @return <code>true</code> if the viewer doesn't show all elements because the
	 *         element limit is exceeded
	 */
	public abstract boolean isTruncated(Object parentElement);

	/**
	 * Returns the number of matches represented by the elements currently shown in
	 * the viewer. Matches are not counted if their element is not shown because the
	 * element limit is exceeded, or if they are hidden by an active match filter
	 * ({@link AbstractTextSearchResult#getActiveMatchFilters()}).
	 *
	 * @param result the search result shown in the viewer
	 * @return number of matches represented by the shown elements
	 */
	public abstract int getShownMatchCount(AbstractTextSearchResult result);

}

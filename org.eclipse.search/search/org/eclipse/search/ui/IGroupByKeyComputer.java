/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.search.ui;

import org.eclipse.core.resources.IMarker;

/**
 * Computes the key by which the markers in the search result view
 * are grouped.
 *
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @deprecated Part of the old ('classic') search result view. Since 3.0 clients can create their own search result view pages (see {@link ISearchResultPage}), leaving it up to the page
 * how to group search results.
 */
@Deprecated
public interface IGroupByKeyComputer {

	/**
	 * Computes and returns key by which the given marker is grouped.
	 *
	 * @param	marker	the marker for which the key must be computed
	 * @return	an object that will be used as the key for that marker,
	 *			<code>null</code> if the marker seems to be invalid
	 */
	public Object computeGroupByKey(IMarker marker);
}

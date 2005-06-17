/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @deprecated Implement {@link org.eclipse.search.ui.text.AbstractTextSearchResult} instead.
 */
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

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.ui;

import org.eclipse.core.resources.IMarker;

/**
 * Computes the key by which the markers in the search result view
 * are grouped.
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

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui.text;

import org.eclipse.core.resources.IMarker;

import org.eclipse.search.ui.IGroupByKeyComputer;

class GroupByKeyComputer implements IGroupByKeyComputer {

	public Object computeGroupByKey(IMarker marker) {
		if (marker == null)
			return null;
		else
			return marker.getResource();
	}
}
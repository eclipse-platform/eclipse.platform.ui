package org.eclipse.search.internal.ui.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
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
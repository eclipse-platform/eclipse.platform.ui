package org.eclipse.search.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
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
	 * @return	an object that will be used as the key for that marker
	 *		<code>UNKNOWN</code> if this computer cannot decide
	 */
	public Object computeGroupByKey(IMarker marker);
}

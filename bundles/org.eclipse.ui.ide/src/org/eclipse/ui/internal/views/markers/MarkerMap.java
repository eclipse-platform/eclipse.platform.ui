/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * The MarkerMap is a helper class that manages the mapping between a set of
 * {@link IMarker} and thier {@link MarkerEntry} wrappers.
 * 
 * @since 3.4
 * 
 */
class MarkerMap {

	static final MarkerMap EMPTY_MAP = new MarkerMap();
	private MarkerEntry[] markers;

	// markerToEntryMap is a lazily created map from the markers to thier
	// corresponding entry
	private Map markerToEntryMap = null;
	private Integer[] markerCounts;

	/**
	 * Creates an initially empty marker map
	 */
	public MarkerMap() {
		this(new MarkerEntry[0]);
	}

	/**
	 * Create an instance of the receiver from markers.
	 * 
	 * @param markers
	 */

	public MarkerMap(MarkerEntry[] markers) {
		this.markers = markers;
	}

	/**
	 * Return the entry at index
	 * 
	 * @param index
	 * @return MarkerEntry
	 */
	public MarkerEntry elementAt(int index) {
		return markers[index];
	}

	/**
	 * Returns an array of marker counts where getMarkerCounts()[severity] is
	 * the number of markers in the list with the given severity.
	 * 
	 * @return an array of {@link Integer} where index indicates
	 *         [errors,warnings,infos,others]
	 */
	Integer[] getMarkerCounts() {
		if (markerCounts == null) {
			int[] ints = new int[] { 0, 0, 0, 0 };

			for (int idx = 0; idx < markers.length; idx++) {
				MarkerEntry marker = markers[idx];
				int severity = marker.getAttributeValue(IMarker.SEVERITY, -1);
				if (severity >= IMarker.SEVERITY_INFO) {
					ints[marker.getAttributeValue(IMarker.SEVERITY, -1)]++;
				}else{
					ints[3]++;
				}
			}

			markerCounts = new Integer[] { new Integer(ints[2]),
					new Integer(ints[1]), new Integer(ints[0]), new Integer(ints[3]) };

		}

		return markerCounts;
	}

	/**
	 * Return the {@link MarkerItem} that maps to marker.
	 * 
	 * @param marker
	 * @return {@link MarkerItem}
	 */
	public MarkerItem getMarkerItem(IMarker marker) {
		if (markerToEntryMap == null) {
			markerToEntryMap = new HashMap();
			for (int i = 0; i < markers.length; i++) {
				IMarker nextMarker = markers[i].getMarker();
				if (nextMarker != null)
					markerToEntryMap.put(nextMarker, markers[i]);
			}
		}

		if (markerToEntryMap.containsKey(marker))
			return (MarkerItem) markerToEntryMap.get(marker);

		return null;
	}

	/**
	 * Get the size of the entries
	 * 
	 * @return int
	 */
	public int getSize() {
		return markers.length;
	}

	/**
	 * Return the entries as an array.
	 * 
	 * @return MarkerEntry[]
	 */
	public MarkerEntry[] toArray() {
		return markers;
	}

	/**
	 * Clear the caches for the markers.
	 */
	void clearAttributeCaches() {
		for (int i = 0; i < markers.length; i++) {
			markers[i].clearCaches();
		}
		
	}
}

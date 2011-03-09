/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.internal.MarkerGroup;
import org.eclipse.ui.views.markers.internal.MarkerGroupingEntry;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * The Markers object contains the MarkerEntry(s) collected and updated by the
 * system, also maintains the categories that markers are grouped into.
 * 
 * @since 3.6
 * 
 */
class Markers {

	static final MarkerCategory[] EMPTY_CATEGORY_ARRAY = new MarkerCategory[0];
	static final MarkerEntry[] EMPTY_ENTRY_ARRAY = new MarkerEntry[0];

	// the marker entries
	private MarkerEntry[] markerEntryArray = EMPTY_ENTRY_ARRAY;
	// the categories
	private MarkerCategory[] categories = EMPTY_CATEGORY_ARRAY;

	private CachedMarkerBuilder builder;

	private boolean inChange = false;

	// markerToEntryMap is a lazily created map from the markers to thier
	// corresponding entry
	private Map markerToEntryMap = null;
	private Integer[] markerCounts;

	Markers(CachedMarkerBuilder builder) {
		this.builder = builder;
		inChange = false;
	}

	/**
	 * Update with newly collected markers
	 * 
	 * @param markerEntries
	 *            the new marker entries
	 * @param sortAndGroup
	 *            true sort and group them
	 * @param monitor
	 */
	synchronized boolean updateWithNewMarkers(Collection markerEntries,
			boolean sortAndGroup, IProgressMonitor monitor) {
		boolean initialVal = inChange;
		try {
			inChange = true;
			if (markerToEntryMap != null) {
				markerToEntryMap.clear();
				markerToEntryMap = null;
			}
			markerCounts = null;
			if (markerEntries.size() == 0) {
				categories = EMPTY_CATEGORY_ARRAY;
				markerEntryArray = EMPTY_ENTRY_ARRAY;
				return true;
			}
			if (monitor.isCanceled()) {
				return false;
			}
			markerEntryArray = new MarkerEntry[markerEntries.size()];
			markerEntries.toArray(markerEntryArray);
			if (sortAndGroup) {
				if (monitor.isCanceled()) {
					return false;
				}
				sortAndMakeCategories(monitor);

				if (monitor.isCanceled()) {
					return false;
				}
			} else {
				categories = EMPTY_CATEGORY_ARRAY;
			}
			return true;
		} finally {
			inChange = initialVal;
		}
	}

	/**
	 * Sort the contained marker entries and build categories if required.
	 * 
	 * @param monitor
	 */
	synchronized boolean sortAndMakeCategories(IProgressMonitor monitor) {
		boolean initialVal = inChange;
		try {
			inChange = true;
			// Sort by Category first
			if (builder.isShowingHierarchy()) {
				MarkerCategory[] markerCategories = groupIntoCategories(
						monitor, markerEntryArray);
				categories = markerCategories;
			} else {
				categories = EMPTY_CATEGORY_ARRAY;
			}

			if (monitor.isCanceled()) {
				return false;
			}
			monitor.subTask(MarkerMessages.MarkerView_processUpdates);

			return sortMarkerEntries(monitor);
		} finally {
			inChange = initialVal;
		}
	}

	/**
	 * @param monitor
	 */
	synchronized boolean sortMarkerEntries(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return false;
		}
		boolean initialVal = inChange;
		try {
			inChange = true;
			if (builder.isShowingHierarchy()) {
				Comparator comparator = builder.getComparator()
						.getFieldsComparator();
				for (int i = 0; i < categories.length; i++) {
					if (monitor.isCanceled()) {
						return false;
					}
					// sort various categories
					MarkerCategory category = categories[i];
					category.children = null; // reset cached children
					int avaliable = category.end - category.start + 1;
					int effLimit = getShowingLimit(avaliable);
					MarkerSortUtil.sortStartingKElement(markerEntryArray,
							comparator, category.start, category.end, effLimit,
							monitor);
				}
			} else {
				if (monitor.isCanceled()) {
					return false;
				}
				int avaialble = markerEntryArray.length - 1;
				int effLimit = getShowingLimit(avaialble);
				MarkerSortUtil.sortStartingKElement(markerEntryArray,
						builder.getComparator(), effLimit, monitor);
			}
			if (monitor.isCanceled()) {
				return false;
			}
			monitor.worked(50);
			return true;
		} finally {
			inChange = initialVal;
		}
	}

	/**
	 * get marker limit to show, if any.
	 * 
	 * @param available
	 */
	private int getShowingLimit(int available) {
		
		boolean limitsEnabled = builder.getGenerator().isMarkerLimitsEnabled();
		if(!limitsEnabled)
			return available;
		
		int limit = builder.getGenerator().getMarkerLimits();
		int effLimit = limit;
		if (available < effLimit || limit <= 0) {
			effLimit = available;
		}
		return effLimit;
	}

	/**
	 * Sort Markers according to groups, and Group them into categories
	 * 
	 * @param monitor
	 * @param newMarkers
	 * @return MarkerCategory
	 */
	MarkerCategory[] groupIntoCategories(IProgressMonitor monitor,
			MarkerEntry[] newMarkers) {
		Map boundaryInfoMap = groupMarkerEntries(newMarkers,
				builder.getCategoryGroup(), newMarkers.length - 1, monitor);
		Iterator iterator = boundaryInfoMap.keySet().iterator();
		int start = 0;
		MarkerCategory[] markerCategories = new MarkerCategory[boundaryInfoMap
				.size()];
		int i = 0;
		int end = 0;
		while (iterator.hasNext()) {
			Object key = iterator.next();
			end = ((Integer) boundaryInfoMap.get(key)).intValue();
			markerCategories[i++] = new MarkerCategory(this, start, end,
					builder.getCategoryGroup().getMarkerField()
							.getValue(newMarkers[start]));
			start = end + 1;
		}
		return markerCategories;
	}

	/**
	 * Sorts/groups the markers in O(N) comparisons and returns the boundary
	 * indices in the map. The O(N) complexity requires the use of a few data
	 * structures. But the speed benefit is tremendous at a very small price of
	 * few extra references.
	 * 
	 * @param entries
	 * @param group
	 * @param k
	 * @return {@link Map}
	 * 
	 */
	private Map groupMarkerEntries(MarkerEntry[] entries, MarkerGroup group,
			int k, IProgressMonitor monitor) {
		TreeMap map = new TreeMap(group.getEntriesComparator());
		for (int i = 0; i <= k; i++) {
			IMarker marker = entries[i].getMarker();
			if (marker == null) {
				continue;// skip stale markers
			}
			if (monitor.isCanceled()) {
				map.clear();
				return map;
			}
			try {
				MarkerGroupingEntry groupingEntry = group.findGroupValue(
						marker.getType(), marker);
				List list = (List) map.get(groupingEntry);
				if (list == null) {
					list = new ArrayList();
					map.put(groupingEntry, list);
				}
				list.add(entries[i]);
			} catch (CoreException e) {
				entries[i].checkIfMarkerStale();
			}
		}
		Iterator keys = map.keySet().iterator();
		int i = 0;
		while (keys.hasNext()) {
			if (monitor.isCanceled()) {
				map.clear();
				return map;
			}
			Object key = keys.next();
			List list = (List) map.get(key);
			Iterator iterator = list.iterator();
			while (iterator.hasNext()) {
				MarkerEntry entry = (MarkerEntry) iterator.next();
				entries[i++] = entry;
			}
			map.put(key, new Integer(i - 1));
		}
		return map;
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
			markerCounts = getMarkerCounts(markerEntryArray);

		}
		return markerCounts;
	}

	/**
	 * Returns an array of marker counts for the given MarkerEntry array , where
	 * getMarkerCounts()[severity] is the number of markers in the list with the
	 * given severity.
	 * 
	 * @return an array of {@link Integer} where index indicates
	 *         [errors,warnings,infos,others]
	 */
	static Integer[] getMarkerCounts(MarkerEntry[] entries) {
		int[] ints = new int[] { 0, 0, 0, 0 };
		for (int idx = 0; idx < entries.length; idx++) {
			IMarker marker = entries[idx].getMarker();
			int severity = -1;
			Object value = null;
			try {
				value = marker.getAttribute(IMarker.SEVERITY);
			} catch (CoreException e) {
				entries[idx].checkIfMarkerStale();
			}
			if (value instanceof Integer) {
				severity = ((Integer) value).intValue();
			}
			if (severity >= IMarker.SEVERITY_INFO) {
				ints[severity]++;
			} else {
				ints[3]++;
			}
		}

		return new Integer[] { new Integer(ints[2]), new Integer(ints[1]),
				new Integer(ints[0]), new Integer(ints[3]) };
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
			for (int i = 0; i < markerEntryArray.length; i++) {
				IMarker nextMarker = markerEntryArray[i].getMarker();
				if (nextMarker != null)
					markerToEntryMap.put(nextMarker, markerEntryArray[i]);
			}
		}

		if (markerToEntryMap.containsKey(marker))
			return (MarkerItem) markerToEntryMap.get(marker);

		return null;
	}

	/**
	 * @return Returns the markerEntryArray.
	 */
	MarkerEntry[] getMarkerEntryArray() {
		return markerEntryArray;
	}

	/**
	 * @return Returns the categories.
	 */
	MarkerCategory[] getCategories() {
		return categories;
	}

	/**
	 * @return MarkerSupportItem[]
	 */
	public MarkerSupportItem[] getElements() {
		if (builder.isShowingHierarchy()) {
			return categories;
		}
		return markerEntryArray;
	}

	/**
	 * @return Returns the builder.
	 */
	CachedMarkerBuilder getBuilder() {
		return builder;
	}

	/**
	 * Use clone where thread safety is concerned. The method is non-blocking.
	 */
	Markers getClone() {
		Markers markers = new Markers(builder);
		if (!inChange) {
			markers.markerEntryArray = markerEntryArray;
			markers.categories = categories;
		}
		return markers;
	}

	/**
	 * @return Returns true if markers are changing.
	 */
	boolean isInChange() {
		return inChange;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((builder == null) ? 0 : builder.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Markers)) {
			return false;
		}
		Markers other = (Markers) obj;
		if (builder == null) {
			if (other.builder != null) {
				return false;
			}
		} else if (!builder.equals(other.builder)) {
			return false;
		}
		return true;
	}
}

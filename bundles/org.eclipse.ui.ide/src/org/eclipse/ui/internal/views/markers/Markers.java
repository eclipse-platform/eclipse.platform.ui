/*******************************************************************************
 * Copyright (c) 2009, 2018 IBM Corporation and others.
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
 *     Mickael Istria (Red Hat Inc.) - Bug 486901
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
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
	private volatile MarkerEntry[] markerEntryArray = EMPTY_ENTRY_ARRAY;
	// the categories
	private volatile MarkerCategory[] categories = EMPTY_CATEGORY_ARRAY;

	private CachedMarkerBuilder builder;

	private volatile boolean inChange;

	// markerToEntryMap is a lazily created map from the markers to thier
	// corresponding entry
	private Map<IMarker, MarkerEntry> markerToEntryMap;
	private Integer[] markerCounts;

	Markers(CachedMarkerBuilder builder) {
		this.builder = builder;
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
	synchronized boolean updateWithNewMarkers(Collection<MarkerEntry> markerEntries,
			boolean sortAndGroup, IProgressMonitor monitor) {
		boolean initialVal = inChange;
		try {
			inChange = true;
			if (markerToEntryMap != null) {
				markerToEntryMap.clear();
				markerToEntryMap = null;
			}
			markerCounts = null;
			if (markerEntries.isEmpty()) {
				categories = EMPTY_CATEGORY_ARRAY;
				markerEntryArray = EMPTY_ENTRY_ARRAY;
				return true;
			}
			if (monitor.isCanceled()) {
				return false;
			}
			MarkerEntry[] markerArray = new MarkerEntry[markerEntries.size()];
			markerEntries.toArray(markerArray);
			markerEntryArray = markerArray;
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
	private synchronized boolean sortAndMakeCategories(IProgressMonitor monitor) {
		boolean initialVal = inChange;
		try {
			inChange = true;
			// Sort by Category first
			if (builder.isShowingHierarchy()) {
				MarkerCategory[] markerCategories = groupIntoCategories(monitor, markerEntryArray);
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
		MarkerComparator markerComparator = builder.getComparator();
		MarkerCategory lastCategory = null;
		try {
			inChange = true;
			if (builder.isShowingHierarchy()) {
				Comparator<MarkerItem> comparator = markerComparator.getFieldsComparator();
				for (MarkerCategory category : categories) {
					if (monitor.isCanceled()) {
						return false;
					}
					lastCategory = category;
					// sort various categories
					category.resetChildren(); // reset cached children
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
						markerComparator, effLimit, monitor);
			}
			if (monitor.isCanceled()) {
				return false;
			}
			monitor.worked(50);
			return true;
		} catch (IllegalArgumentException e) {
			StringBuilder err = new StringBuilder("Bug 371586: broken comparator. "); //$NON-NLS-1$
			if (lastCategory != null) {
				err.append(lastCategory);
			} else {
				err.append(markerComparator.getCategory());
			}
			err.append(", fields: "); //$NON-NLS-1$
			err.append(Arrays.toString(markerComparator.getFields()));
			IDEWorkbenchPlugin.log(err.toString(), e);
			return false;
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
		if(!limitsEnabled) {
			return available;
		}

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
	private MarkerCategory[] groupIntoCategories(IProgressMonitor monitor, MarkerEntry[] newMarkers) {
		Map<MarkerGroupingEntry, Integer> boundaryInfoMap = groupMarkerEntries(newMarkers,
				builder.getCategoryGroup(), newMarkers.length - 1, monitor);
		int start = 0;
		MarkerCategory[] markerCategories = new MarkerCategory[boundaryInfoMap.size()];
		int i = 0;
		int end = 0;
		for (Entry<MarkerGroupingEntry, Integer> entry : boundaryInfoMap.entrySet()) {
			end = entry.getValue();
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
	private Map<MarkerGroupingEntry, Integer> groupMarkerEntries(MarkerEntry[] entries, MarkerGroup group,
			int k, IProgressMonitor monitor) {
		TreeMap<MarkerGroupingEntry, List<MarkerEntry>> map = new TreeMap<>(
				group.getEntriesComparator());
		for (int i = 0; i <= k; i++) {
			IMarker marker = entries[i].getMarker();
			if (marker == null) {
				continue;// skip stale markers
			}
			if (monitor.isCanceled()) {
				return Collections.emptyMap();
			}
			try {
				MarkerGroupingEntry groupingEntry = group.findGroupValue(marker.getType(), marker);
				List<MarkerEntry> list = map.get(groupingEntry);
				if (list == null) {
					list = new ArrayList<>();
					map.put(groupingEntry, list);
				}
				list.add(entries[i]);
			} catch (CoreException e) {
				entries[i].checkIfMarkerStale();
			}
		}
		TreeMap<MarkerGroupingEntry, Integer> result = new TreeMap<>(
				group.getEntriesComparator());
		int i = 0;
		for (Entry<MarkerGroupingEntry, List<MarkerEntry>> mapEntry : map.entrySet()) {
			if (monitor.isCanceled()) {
				return Collections.emptyMap();
			}
			MarkerGroupingEntry key = mapEntry.getKey();
			for (MarkerEntry entry : mapEntry.getValue()) {
				entries[i++] = entry;
			}
			result.put(key, i - 1);
		}
		return result;
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
		for (MarkerEntry entry : entries) {
			IMarker marker = entry.getMarker();
			int severity = -1;
			Object value = null;
			try {
				value = marker.getAttribute(IMarker.SEVERITY);
			} catch (CoreException e) {
				entry.checkIfMarkerStale();
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
		return new Integer[] { ints[2], ints[1], ints[0], ints[3] };
	}

	/**
	 * Return the {@link MarkerItem} that maps to marker.
	 *
	 * @param marker
	 * @return {@link MarkerItem}
	 */
	public synchronized MarkerItem getMarkerItem(IMarker marker) {
		if (markerToEntryMap == null) {
			markerToEntryMap = new HashMap<>();
			for (MarkerEntry markerEntry : markerEntryArray) {
				IMarker nextMarker = markerEntry.getMarker();
				if (nextMarker != null) {
					markerToEntryMap.put(nextMarker, markerEntry);
				}
			}
		}
		return markerToEntryMap.get(marker);
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
			markers.markerEntryArray = markerEntryArray.clone();
			markers.categories = categories.clone();
		}
		return markers;
	}

	/**
	 * @return Returns true if markers are changing.
	 */
	boolean isInChange() {
		return inChange;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(builder);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Markers)) {
			return false;
		}
		Markers other = (Markers) obj;
		return Objects.equals(builder, other.builder);
	}
}

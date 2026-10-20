/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
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

package org.eclipse.ui.internal.views.markers;

import java.util.Objects;

import org.eclipse.core.resources.IMarker;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

class MarkerCategory extends MarkerSupportItem {

	final int start;

	final int end;

	private volatile MarkerEntry[] children;

	private final String name;

	private int severity = -1;

	private final Markers markers;

	/**
	 * Caches the last result of {@link #getShownChildrenCount()}, which is
	 * needed for every label update of this category but is O(children) to
	 * compute while a search filter is active.
	 */
	private volatile ShownChildrenCount shownChildrenCount;

	/**
	 * The number of children shown for a given search pattern and a given
	 * (identical) children array, both of which the count only stays valid for.
	 *
	 * @param pattern  the search box pattern the count was computed for
	 * @param children the children array the count was computed from, compared
	 *                 by identity since a new array is created whenever the
	 *                 markers of this category change
	 * @param count    the number of children shown
	 */
	private record ShownChildrenCount(String pattern, MarkerSupportItem[] children, int count) {

		boolean isValidFor(String currentPattern, MarkerSupportItem[] currentChildren) {
			return children == currentChildren && pattern.equals(currentPattern);
		}
	}

	/**
	 * Create a new instance of the receiver that has the markers between
	 * startIndex and endIndex showing.
	 *
	 * @param endIndex
	 *            the builder used to generate the children lazily.
	 */
	MarkerCategory(Markers markers, int startIndex,
			int endIndex, String categoryName) {
		this.markers = markers;
		start = startIndex;
		end = endIndex;
		name = categoryName;
	}

	@Override
	MarkerSupportItem[] getChildren() {
		MarkerEntry[] myChildren = children;
		if (myChildren != null) {
			return myChildren;
		}
		MarkerItem[] allMarkers = markers.getMarkerEntryArray();
		int markersLength = allMarkers.length;
		if (start >= markersLength || end >= markersLength) {
			// NB: the array can be changed after our creation via
			// markers::updateWithNewMarkers so that the expected array size doesn't match
			// anymore to our start/end values. Just return nothing in this case and let the
			// "children" be null to avoid persistence of inconsistent data
			return new MarkerEntry[0];
		}
		int totalSize = getChildrenCount();
		myChildren = new MarkerEntry[totalSize];
		System.arraycopy(allMarkers, start, myChildren, 0, totalSize);
		for (MarkerEntry markerEntry : myChildren) {
			markerEntry.setCategory(this);
		}
		children = myChildren;
		return myChildren;
	}

	void resetChildren() {
		children = null;
		shownChildrenCount = null;
	}

	@Override
	int getChildrenCount() {
		return end - start + 1;
	}

	/**
	 * Returns how many children of this category are currently shown, i.e. how
	 * many of them survive both the marker limit truncation and the view's
	 * search box filter. The result is cached, since it is needed for every
	 * label update of this category.
	 *
	 * @return the number of children currently shown
	 */
	int getShownChildrenCount() {
		ExtendedMarkersView view = markers.getBuilder().getView();
		String pattern = view.getSearchFilterPattern();
		if (pattern == null) {
			// no search filter: no need to materialize and inspect the children
			return view.applyMarkerLimit(getChildrenCount());
		}
		MarkerSupportItem[] myChildren = getChildren();
		ShownChildrenCount cached = shownChildrenCount;
		if (cached != null && cached.isValidFor(pattern, myChildren)) {
			return cached.count();
		}
		int count = view.countMatchingSearchFilter(myChildren);
		shownChildrenCount = new ShownChildrenCount(pattern, myChildren, count);
		return count;
	}

	@Override
	String getDescription() {
		//see Bug 294959
		//if(refreshing){
		//	//see Bug 294959
		//	return NLS.bind(MarkerMessages.Category_building,
		//			new Object[] { getName() });
		//}
		MarkerContentGenerator generator = markers.getBuilder().getGenerator();
		boolean limitsEnabled = generator.isMarkerLimitsEnabled();
		int limit = generator.getMarkerLimits();

		int totalSize = getChildrenCount();

		// Reflect what is actually shown for this category, i.e. the markers
		// left after the marker limit truncation and after the search box's
		// quick filter, so that the category label stays in sync with its
		// visible children while searching. With an empty search box this is
		// the same value the marker limit alone would yield.
		int shownSize = getShownChildrenCount();

		if (limitsEnabled && totalSize > limit) {
			return NLS.bind(MarkerMessages.Category_Limit_Label, name, String.valueOf(shownSize),
					String.valueOf(totalSize));
		}
		if (shownSize == 1) {
			return NLS.bind(MarkerMessages.Category_One_Item_Label,
					new Object[] { name });
		}

		return NLS.bind(MarkerMessages.Category_Label, new Object[] { name,
				String.valueOf(shownSize) });

	}

	/**
	 * Get the highest severity in the receiver.
	 *
	 * @return int
	 */
	int getHighestSeverity() {
		if (severity >= 0) {
			return severity;
		}
		severity = 0;// Reset to info
		for (MarkerSupportItem supportItem : getChildren()) {
			if (supportItem.isConcrete()) {
				int elementSeverity = supportItem.getAttributeValue(IMarker.SEVERITY, -1);
				if (elementSeverity > severity) {
					severity = elementSeverity;
				}
				if (severity == IMarker.SEVERITY_ERROR) { // As bad as it gets
					return severity;
				}
			}
		}
		return severity;
	}

	/**
	 * Return the name of the receiver.
	 *
	 * @return String
	 */
	String getName() {
		return name;
	}

	@Override
	MarkerSupportItem getParent() {
		return null;
	}

	@Override
	boolean isConcrete() {
		return false;
	}

	/**
	 * Clear the cached values for performance reasons.
	 */
	@Override
	void clearCache() {
		shownChildrenCount = null;
		for (MarkerSupportItem supportItem : getChildren()) {
			supportItem.clearCache();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ Objects.hashCode(markers);
		return prime * result + Objects.hashCode(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MarkerCategory other = (MarkerCategory) obj;
		return Objects.equals(markers, other.markers) && Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MarkerCategory [name="); //$NON-NLS-1$
		builder.append(name);
		builder.append(", severity="); //$NON-NLS-1$
		builder.append(severity);
		builder.append(", start="); //$NON-NLS-1$
		builder.append(start);
		builder.append(", end="); //$NON-NLS-1$
		builder.append(end);
		builder.append(']');
		return builder.toString();
	}

}

/*******************************************************************************
 * Copyright (c) 2026 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Raghunandana Murthappa - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.e4.ui.dialogs.filteredtree.PatternFilter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * Matches a marker against the values of every field currently visible in a
 * Markers view, so that typing in the view's search box filters the markers by
 * any of the values shown in their columns (description, resource, path,
 * location, type, ...).
 */
class MarkerPatternFilter extends PatternFilter {

	private final ExtendedMarkersView view;

	/**
	 * The pattern currently applied, kept to be able to tell an empty filter
	 * from an active one and to let callers detect pattern changes.
	 */
	private volatile String patternString;

	MarkerPatternFilter(ExtendedMarkersView view) {
		this.view = view;
		// so that a marker matches when the typed text occurs anywhere in one of
		// its values, not only at the beginning of it
		setIncludeLeadingWildcard(true);
	}

	@Override
	public void setPattern(String patternString) {
		this.patternString = patternString;
		super.setPattern(patternString);
	}

	/**
	 * Returns the pattern currently applied, or <code>null</code> if none is.
	 */
	String getPatternString() {
		return patternString;
	}

	public boolean isFilterEmpty() {
		String pattern = patternString;
		return pattern == null || pattern.isEmpty();
	}

	/**
	 * Returns whether the given marker item matches the current pattern.
	 * <p>
	 * In contrast to {@link #select(Viewer, Object, Object)} this neither asks
	 * the content provider for the item's children nor populates the caches
	 * {@link PatternFilter} maintains for the tree filtering - those caches are
	 * only cleared when the pattern changes and would otherwise retain the
	 * marker items handed in here, although they are recreated on every marker
	 * rebuild.
	 * </p>
	 */
	boolean matches(MarkerSupportItem item) {
		// the viewer is unused by isLeafMatch(...) below, which never delegates
		// to the label provider based implementation of PatternFilter
		return isLeafMatch(null, item);
	}

	@Override
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		if (isFilterEmpty()) {
			return true;
		}
		if (!(element instanceof MarkerSupportItem item) || !item.isConcrete()) {
			// categories are never a leaf match themselves, they stay visible
			// through isParentMatch(...) as long as any of their markers match
			return false;
		}
		for (MarkerField field : view.getVisibleFields()) {
			if (matches(field, item)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether the given item's value for the given field matches the
	 * current pattern.
	 */
	private boolean matches(MarkerField field, MarkerItem item) {
		String value;
		try {
			value = field.getValue(item);
		} catch (RuntimeException e) {
			// a field can fail for markers it does not understand, or whose
			// underlying resource vanished meanwhile - it simply does not match
			// then, filtering must never break the view
			return false;
		}
		return value != null && wordMatches(value);
	}
}

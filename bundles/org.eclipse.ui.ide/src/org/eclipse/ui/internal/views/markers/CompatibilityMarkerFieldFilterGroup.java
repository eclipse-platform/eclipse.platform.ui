/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.ui.views.markers.MarkerFieldFilter;
import org.eclipse.ui.views.markers.internal.ProblemFilter;

/**
 * CompatibilityMarkerFieldFilterGroup is a filter group that uses a
 * {@link ProblemFilter}.
 *
 * @since 3.4
 *
 */
public class CompatibilityMarkerFieldFilterGroup extends MarkerFieldFilterGroup {

	ProblemFilter problemFilter;

	/**
	 * Create a new instance of the receiver based on the ProblemFilter.
	 */
	public CompatibilityMarkerFieldFilterGroup(ProblemFilter filter,
			MarkerContentGenerator generator) {
		super(null, generator);
		problemFilter = filter;
		setEnabled(filter.isEnabled());
		setScope(filter.getOnResource());
	}

	@Override
	public String getID() {
		return problemFilter.getId();
	}

	@Override
	public String getName() {
		return problemFilter.getName();
	}

	@Override
	public boolean isSystem() {
		return true;
	}

	@Override
	MarkerFieldFilterGroup makeWorkingCopy() {

		CompatibilityMarkerFieldFilterGroup clone = new CompatibilityMarkerFieldFilterGroup(
				this.problemFilter, this.generator);
		if (populateClone(clone))
			return clone;
		return null;

	}

	@Override
	protected void calculateFilters() {
		super.calculateFilters();
		// Now initialize with the ProblemFilter
		for (MarkerFieldFilter fieldFilter : fieldFilters) {
			if (fieldFilter instanceof CompatibilityFieldFilter)
				((CompatibilityFieldFilter) fieldFilter).initialize(problemFilter);
		}
	}

}

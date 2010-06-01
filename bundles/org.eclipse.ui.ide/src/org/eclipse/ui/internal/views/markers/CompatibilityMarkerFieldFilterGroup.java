/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

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
	 * 
	 * @param filter
	 * @param cachedMarkerBuilder
	 */
	public CompatibilityMarkerFieldFilterGroup(ProblemFilter filter,
			MarkerContentGenerator generator) {
		super(null, generator);
		problemFilter = filter;
		setEnabled(filter.isEnabled());
		setScope(filter.getOnResource());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerFieldFilterGroup#getID()
	 */
	public String getID() {
		return problemFilter.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerFieldFilterGroup#getName()
	 */
	public String getName() {
		return problemFilter.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerFieldFilterGroup#isSystem()
	 */
	public boolean isSystem() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerFieldFilterGroup#makeWorkingCopy()
	 */
	MarkerFieldFilterGroup makeWorkingCopy() {

		CompatibilityMarkerFieldFilterGroup clone = new CompatibilityMarkerFieldFilterGroup(
				this.problemFilter, this.generator);
		if (populateClone(clone))
			return clone;
		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerFieldFilterGroup#calculateFilters()
	 */
	protected void calculateFilters() {
		super.calculateFilters();
		// Now initialize with the ProblemFilter
		for (int i = 0; i < fieldFilters.length; i++) {
			if (fieldFilters[i] instanceof CompatibilityFieldFilter)
				((CompatibilityFieldFilter) fieldFilters[i])
						.initialize(problemFilter);
		}
	}

}

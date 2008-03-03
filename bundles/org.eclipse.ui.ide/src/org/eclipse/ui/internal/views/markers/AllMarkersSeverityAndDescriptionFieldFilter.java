/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.views.markers.MarkerFieldFilter;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * AllMarkersSeverityAndDescriptionFieldFilter is a
 * {@link SeverityAndDescriptionFieldFilter} that handles the no severity case.
 * 
 * @since 3.4
 * 
 */
public class AllMarkersSeverityAndDescriptionFieldFilter extends
		SeverityAndDescriptionFieldFilter {

	private boolean filterOnSeverity = false;
	private static String FILTER_ON_SEVERITY = "FILTER_ON_SEVERITY"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 */
	public AllMarkersSeverityAndDescriptionFieldFilter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.SeverityAndDescriptionFieldFilter#select(org.eclipse.ui.views.markers.MarkerItem)
	 */
	public boolean select(MarkerItem item) {
		if (filterOnSeverity) {

			IMarker marker = item.getMarker();
			if (marker == null)
				return false;

			if (!checkSeverity(item.getAttributeValue(IMarker.SEVERITY, -1)))
				return false;
		}
		return super.select(item);
	}

	/**
	 * Return whether or not we are filtering on severity.
	 * 
	 * @return boolean
	 */
	boolean getFilterOnSeverity() {
		return filterOnSeverity;
	}

	/**
	 * Set the whether or not we are filtering on severity
	 * 
	 * @param filter
	 */
	void setFilterOnSeverity(boolean filter) {
		filterOnSeverity = filter;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.SeverityAndDescriptionFieldFilter#loadSettings(org.eclipse.ui.IMemento)
	 */
	public void loadSettings(IMemento memento) {
		super.loadSettings(memento);

		Boolean filtering = memento.getBoolean(FILTER_ON_SEVERITY);
		if (filtering != null)
			filterOnSeverity = filtering.booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.SeverityAndDescriptionFieldFilter#saveSettings(org.eclipse.ui.IMemento)
	 */
	public void saveSettings(IMemento memento) {
		super.saveSettings(memento);
		memento.putBoolean(FILTER_ON_SEVERITY, filterOnSeverity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.SeverityAndDescriptionFieldFilter#populateWorkingCopy(org.eclipse.ui.views.markers.MarkerFieldFilter)
	 */
	public void populateWorkingCopy(MarkerFieldFilter copy) {
		super.populateWorkingCopy(copy);
		((AllMarkersSeverityAndDescriptionFieldFilter) copy).filterOnSeverity = filterOnSeverity;
	}

}

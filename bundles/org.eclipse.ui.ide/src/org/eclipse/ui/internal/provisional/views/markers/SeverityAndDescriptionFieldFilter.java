/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.provisional.views.markers;

import java.util.Map;

import org.eclipse.core.resources.IMarker;

/**
 * SeverityAndDescriptionFieldFilter is the filter for the severity and
 * descripton field.
 * 
 * @since 3.4
 * 
 */
public class SeverityAndDescriptionFieldFilter extends MarkerFieldFilter {

	int selectedSeverity = -1;

	/**
	 * Create a new instance of the receiver.
	 */
	public SeverityAndDescriptionFieldFilter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter#select(org.eclipse.core.resources.IMarker)
	 */
	public boolean select(IMarker marker) {

		return (marker.getAttribute(IMarker.SEVERITY, -1) & selectedSeverity) > 0;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter#initialize(java.util.Map)
	 */
	public void initialize(Map values) {
		Object value = values.get(IMarker.SEVERITY);
		if (value != null && value instanceof Integer) {
			selectedSeverity = ((Integer) value).intValue();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter#makeWorkingCopy()
	 */
	public MarkerFieldFilter makeWorkingCopy() {
		SeverityAndDescriptionFieldFilter clone = new SeverityAndDescriptionFieldFilter();
		clone.selectedSeverity = this.selectedSeverity;
		return clone;
	}

}

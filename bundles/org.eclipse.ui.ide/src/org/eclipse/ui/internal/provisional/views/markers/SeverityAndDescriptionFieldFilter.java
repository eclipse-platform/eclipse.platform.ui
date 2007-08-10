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
 * description field.
 * 
 * @since 3.4
 * 
 */
public class SeverityAndDescriptionFieldFilter extends MarkerFieldFilter {

	static final String CONTAINS = "CONTAINS"; //$NON-NLS-1$
	static String DOES_NOT_CONTAIN = "DOES_NOT_CONTAIN"; //$NON-NLS-1$
	int selectedSeverities = IMarker.SEVERITY_ERROR | IMarker.SEVERITY_WARNING
			| IMarker.SEVERITY_INFO;
	String containsModifier = CONTAINS;
	String containsText = MarkerUtilities.EMPTY_STRING;

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

		return (marker.getAttribute(IMarker.SEVERITY, -1) & selectedSeverities) > 0;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter#initialize(java.util.Map)
	 */
	public void initialize(Map values) {
		Object value = values.get(IMarker.SEVERITY);
		if (value != null && value instanceof Integer) {
			selectedSeverities = ((Integer) value).intValue();
		}
		Object modifier = values.get(MarkerUtilities.CONTAINS_MODIFIER_TOKEN);
		if (modifier != null && modifier instanceof String)
			containsModifier = (String) modifier;

		Object text = values.get(MarkerUtilities.CONTAINS_TEXT_TOKEN);
		if (text != null && text instanceof String)
			containsText = (String) text;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter#populateWorkingCopy(org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter)
	 */
	public void populateWorkingCopy(MarkerFieldFilter copy) {
		super.populateWorkingCopy(copy);
		SeverityAndDescriptionFieldFilter clone = (SeverityAndDescriptionFieldFilter) copy;
		clone.selectedSeverities = this.selectedSeverities;
		clone.containsModifier = this.containsModifier;
		clone.containsText = this.containsText;
	}

	/**
	 * Return the contains modifier.
	 * 
	 * @return One of {@link #CONTAINS} or {@link #DOES_NOT_CONTAIN}
	 */
	public String getContainsModifier() {
		return containsModifier;
	}

	/**
	 * Set the contains modifier.
	 * 
	 * @param containsString
	 *            One of {@link #CONTAINS} or {@link #DOES_NOT_CONTAIN}
	 */
	public void setContainsModifier(String containsString) {
		this.containsModifier = containsString;
	}

	/**
	 * Return the text to apply the containsModifier to.
	 * 
	 * @return String
	 */
	public String getContainsText() {
		return containsText;
	}

	/**
	 * Set the text to apply the containsModifier to.
	 * 
	 * @param containsText
	 *            String
	 */
	public void setContainsText(String containsText) {
		this.containsText = containsText;
	}

}

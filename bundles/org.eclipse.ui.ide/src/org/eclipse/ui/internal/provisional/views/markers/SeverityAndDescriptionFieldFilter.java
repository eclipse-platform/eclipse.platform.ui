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
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerSupportConstants;

/**
 * SeverityAndDescriptionFieldFilter is the filter for the severity and
 * description field.
 * 
 * @since 3.4
 * 
 */
public class SeverityAndDescriptionFieldFilter extends MarkerFieldFilter {

	static final String CONTAINS = "CONTAINS"; //$NON-NLS-1$
	final static int SEVERITY_ERROR = 1 << IMarker.SEVERITY_ERROR;
	final static int SEVERITY_WARNING = 1 << IMarker.SEVERITY_WARNING;
	final static int SEVERITY_INFO = 1 << IMarker.SEVERITY_INFO;
	
	private static final String TAG_SELECTED_SEVERITIES = "selectedSeverities"; //$NON-NLS-1$
	private static final String TAG_CONTAINS_MODIFIER = "containsModifier"; //$NON-NLS-1$
	private static final String TAG_CONTAINS_TEXT = "containsText"; //$NON-NLS-1$
	static String DOES_NOT_CONTAIN = "DOES_NOT_CONTAIN"; //$NON-NLS-1$
	int selectedSeverities = IMarker.SEVERITY_ERROR + IMarker.SEVERITY_WARNING
			+ IMarker.SEVERITY_INFO;
	String containsModifier = CONTAINS;
	String containsText = MarkerSupportConstants.EMPTY_STRING;

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

		
		int markerSeverity = marker.getAttribute(IMarker.SEVERITY, -1);
		if(markerSeverity < 0)
			return false;
		//Convert from the marker to the filter
		return (1 << markerSeverity & selectedSeverities) > 0;

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
		Object modifier = values.get(MarkerSupportInternalUtilities.CONTAINS_MODIFIER_TOKEN);
		if (modifier != null && modifier instanceof String)
			containsModifier = (String) modifier;

		Object text = values.get(MarkerSupportInternalUtilities.CONTAINS_TEXT_TOKEN);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter#saveSettings(org.eclipse.ui.IMemento)
	 */
	public void saveSettings(IMemento memento) {

		memento.putInteger(TAG_SELECTED_SEVERITIES, selectedSeverities);
		memento.putString(TAG_CONTAINS_MODIFIER, containsModifier);
		memento.putString(TAG_CONTAINS_TEXT, containsText);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter#loadSettings(org.eclipse.ui.IMemento)
	 */
	public void loadSettings(IMemento memento) {
		
		selectedSeverities = memento.getInteger(TAG_SELECTED_SEVERITIES).intValue();
		containsModifier = memento.getString(TAG_CONTAINS_MODIFIER);
		containsText = memento.getString(TAG_CONTAINS_TEXT);
	}

}

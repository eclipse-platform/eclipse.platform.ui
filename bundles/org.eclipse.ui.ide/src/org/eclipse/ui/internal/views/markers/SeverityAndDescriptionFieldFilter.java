/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.views.markers.MarkerFieldFilter;

/**
 * SeverityAndDescriptionFieldFilter is the filter for the severity and
 * description field.
 * 
 * @since 3.4
 * 
 */
public abstract class SeverityAndDescriptionFieldFilter extends DescriptionFieldFilter {

	final static int SEVERITY_ERROR = 1 << IMarker.SEVERITY_ERROR;
	final static int SEVERITY_WARNING = 1 << IMarker.SEVERITY_WARNING;
	final static int SEVERITY_INFO = 1 << IMarker.SEVERITY_INFO;
	protected int selectedSeverities = SEVERITY_ERROR + SEVERITY_WARNING + SEVERITY_INFO;
	private static final String TAG_SELECTED_SEVERITIES = "selectedSeverities"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 */
	public SeverityAndDescriptionFieldFilter() {
		super();
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
		Object modifier = values
				.get(MarkerSupportInternalUtilities.CONTAINS_MODIFIER_TOKEN);
		if (modifier != null && modifier instanceof String)
			containsModifier = (String) modifier;

		Object text = values
				.get(MarkerSupportInternalUtilities.CONTAINS_TEXT_TOKEN);
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
		((SeverityAndDescriptionFieldFilter) copy).selectedSeverities = this.selectedSeverities;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter#saveSettings(org.eclipse.ui.IMemento)
	 */
	public void saveSettings(IMemento memento) {
		super.saveSettings(memento);
		memento.putInteger(TAG_SELECTED_SEVERITIES, selectedSeverities);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter#loadSettings(org.eclipse.ui.IMemento)
	 */
	public void loadSettings(IMemento memento) {
		super.loadSettings(memento);
		selectedSeverities = memento.getInteger(TAG_SELECTED_SEVERITIES)
				.intValue();
	}

	/**
	 * Compare the selected severity and the severity of the marker to see if
	 * they match
	 * 
	 * @param markerSeverity
	 */
	protected boolean checkSeverity(int markerSeverity) {
		// Convert from the marker to the filter
		return (1 << markerSeverity & selectedSeverities) > 0;
	
	}

}

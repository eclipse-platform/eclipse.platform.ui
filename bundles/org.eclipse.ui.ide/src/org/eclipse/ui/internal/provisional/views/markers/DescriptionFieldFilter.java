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

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerSupportConstants;

/**
 * DescriptionFieldFilter is the filter for descriptions.
 * 
 * @since 3.4
 * 
 */
public class DescriptionFieldFilter extends MarkerFieldFilter {

	static final String CONTAINS = "CONTAINS"; //$NON-NLS-1$
	static final String TAG_CONTAINS_MODIFIER = "containsModifier"; //$NON-NLS-1$
	static final String TAG_CONTAINS_TEXT = "containsText"; //$NON-NLS-1$
	static String DOES_NOT_CONTAIN = "DOES_NOT_CONTAIN"; //$NON-NLS-1$

	String containsModifier = CONTAINS;
	String containsText = MarkerSupportConstants.EMPTY_STRING;

	/**
	 * Create a new instance of the receiver.
	 */
	public DescriptionFieldFilter() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter#loadSettings(org.eclipse.ui.IMemento)
	 */
	public void loadSettings(IMemento memento) {
		String modifier = memento.getString(TAG_CONTAINS_MODIFIER);
		if(modifier == null)
			return;
		String contains = memento.getString(TAG_CONTAINS_TEXT);
		if(contains == null)
			return;
		containsText = contains;
		containsModifier = modifier;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter#saveSettings(org.eclipse.ui.IMemento)
	 */
	public void saveSettings(IMemento memento) {
		memento.putString(TAG_CONTAINS_MODIFIER, containsModifier);
		memento.putString(TAG_CONTAINS_TEXT, containsText);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter#select(org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem)
	 */
	public boolean select(MarkerItem item) {
		if (containsText.length() == 0)
			return true;

		String value = getField().getValue(item);
		if (containsModifier.equals(CONTAINS))
			return value.indexOf(containsText) >= 0;
		return value.indexOf(containsText) < 0;

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter#populateWorkingCopy(org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter)
	 */
	public void populateWorkingCopy(MarkerFieldFilter copy) {
		super.populateWorkingCopy(copy);
		DescriptionFieldFilter clone = (DescriptionFieldFilter) copy;
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

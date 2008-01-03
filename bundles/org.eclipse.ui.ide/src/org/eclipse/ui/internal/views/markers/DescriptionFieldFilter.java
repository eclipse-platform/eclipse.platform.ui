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
package org.eclipse.ui.internal.views.markers;

import java.util.Map;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.views.markers.MarkerFieldFilter;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.MarkerSupportConstants;
import org.eclipse.ui.views.markers.internal.ProblemFilter;

/**
 * DescriptionFieldFilter is the filter for descriptions.
 * 
 * @since 3.4
 * 
 */
public class DescriptionFieldFilter extends CompatibilityFieldFilter {

	static final String TAG_CONTAINS_MODIFIER = "containsModifier"; //$NON-NLS-1$
	static final String TAG_CONTAINS_TEXT = "containsText"; //$NON-NLS-1$

	String containsModifier = MarkerSupportConstants.CONTAINS_KEY;
	String containsText = MarkerSupportConstants.EMPTY_STRING;

	/**
	 * Create a new instance of the receiver.
	 */
	public DescriptionFieldFilter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter#loadSettings(org.eclipse.ui.IMemento)
	 */
	public void loadSettings(IMemento memento) {
		String modifier = memento.getString(TAG_CONTAINS_MODIFIER);
		if (modifier == null)
			return;
		String contains = memento.getString(TAG_CONTAINS_TEXT);
		if (contains == null)
			return;
		containsText = contains;
		containsModifier = modifier;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.CompatibilityFieldFilter#loadLegacySettings(org.eclipse.ui.IMemento)
	 */
	public void loadLegacySettings(IMemento memento) {
		String setting = memento.getString(ProblemFilter.TAG_CONTAINS);

		if (setting != null) {
			containsModifier = Boolean.valueOf(setting).booleanValue() ? MarkerSupportConstants.CONTAINS_KEY
					: MarkerSupportConstants.DOES_NOT_CONTAIN_KEY;

		}

		setting = memento.getString(ProblemFilter.TAG_DESCRIPTION);

		if (setting != null) {
			containsText = new String(setting);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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
		if (containsModifier.equals(MarkerSupportConstants.CONTAINS_KEY))
			return value.indexOf(containsText) >= 0;
		return value.indexOf(containsText) < 0;

	}

	/*
	 * (non-Javadoc)
	 * 
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
	 * @return One of {@link MarkerSupportConstants#CONTAINS_KEY} or
	 *         {@link MarkerSupportConstants#DOES_NOT_CONTAIN_KEY}
	 */
	public String getContainsModifier() {
		return containsModifier;
	}

	/**
	 * Set the contains modifier.
	 * 
	 * @param containsString
	 *            One of {@link MarkerSupportConstants#CONTAINS_KEY} or
	 *            {@link MarkerSupportConstants#DOES_NOT_CONTAIN_KEY}
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
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter#initialize(java.util.Map)
	 */
	public void initialize(Map values) {
		super.initialize(values);
		if (values.containsKey(MarkerSupportConstants.CONTAINS_KEY)) {
			setContainsText((String) values
					.get(MarkerSupportConstants.CONTAINS_KEY));
			setContainsModifier(MarkerSupportConstants.CONTAINS_KEY);
		} else if (values
				.containsKey(MarkerSupportConstants.DOES_NOT_CONTAIN_KEY)) {
			setContainsText((String) values
					.get(MarkerSupportConstants.DOES_NOT_CONTAIN_KEY));
			setContainsModifier(MarkerSupportConstants.DOES_NOT_CONTAIN_KEY);
		}
	}
}

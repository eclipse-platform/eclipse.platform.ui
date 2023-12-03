/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
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
 *     Mickael Istria (Red Hat Inc.) - Bug 486901
 *******************************************************************************/
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
 */
public class DescriptionFieldFilter extends CompatibilityFieldFilter {

	static final String TAG_CONTAINS_MODIFIER = "containsModifier"; //$NON-NLS-1$
	static final String TAG_CONTAINS_TEXT = "containsText"; //$NON-NLS-1$

	String containsModifier = MarkerSupportConstants.CONTAINS_KEY;
	String containsText = MarkerSupportInternalUtilities.EMPTY_STRING;

	/**
	 * Create a new instance of the receiver.
	 */
	public DescriptionFieldFilter() {
		super();
	}

	@Override
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

	@Override
	void loadLegacySettings(IMemento memento, MarkerContentGenerator generator) {

		String setting = memento.getString(ProblemFilter.TAG_CONTAINS);

		if (setting != null) {
			containsModifier = Boolean.parseBoolean(setting) ? MarkerSupportConstants.CONTAINS_KEY
					: MarkerSupportConstants.DOES_NOT_CONTAIN_KEY;

		}

		setting = memento.getString(ProblemFilter.TAG_DESCRIPTION);

		if (setting != null) {
			containsText = setting;
		}
	}


	@Override
	public void saveSettings(IMemento memento) {
		memento.putString(TAG_CONTAINS_MODIFIER, containsModifier);
		memento.putString(TAG_CONTAINS_TEXT, containsText);
	}


	@Override
	public boolean select(MarkerItem item) {
		if (containsText.isEmpty())
			return true;

		String value = getField().getValue(item);
		if (containsModifier.equals(MarkerSupportConstants.CONTAINS_KEY))
			return value.contains(containsText);
		return !value.contains(containsText);

	}


	@Override
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
	String getContainsModifier() {
		return containsModifier;
	}

	/**
	 * Set the contains modifier.
	 *
	 * @param containsString
	 *            One of {@link MarkerSupportConstants#CONTAINS_KEY} or
	 *            {@link MarkerSupportConstants#DOES_NOT_CONTAIN_KEY}
	 */
	void setContainsModifier(String containsString) {
		this.containsModifier = containsString;
	}

	/**
	 * Return the text to apply the containsModifier to.
	 *
	 * @return String
	 */
	String getContainsText() {
		return containsText;
	}

	/**
	 * Set the text to apply the containsModifier to.
	 *
	 * @param containsText
	 *            String
	 */
	void setContainsText(String containsText) {
		this.containsText = containsText;
	}


	@Override
	public void initialize(Map values) {
		super.initialize(values);
		String value = (String) values.get(MarkerSupportConstants.CONTAINS_KEY);
		if (value != null) {
			setContainsText(value);
			setContainsModifier(MarkerSupportConstants.CONTAINS_KEY);
		} else {
			value = (String) values.get(MarkerSupportConstants.DOES_NOT_CONTAIN_KEY);
			if (value != null) {
				setContainsText(value);
				setContainsModifier(MarkerSupportConstants.DOES_NOT_CONTAIN_KEY);
			}
		}
	}

	@Override
	public void initialize(ProblemFilter problemFilter) {
		containsModifier = problemFilter.getContains() ? MarkerSupportConstants.CONTAINS_KEY
				: MarkerSupportConstants.DOES_NOT_CONTAIN_KEY;
		containsText = problemFilter.getDescription();

	}
}

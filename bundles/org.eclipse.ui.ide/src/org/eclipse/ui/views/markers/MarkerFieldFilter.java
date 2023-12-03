/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.views.markers;

import java.util.Map;

import org.eclipse.ui.IMemento;

/**
 * A MarkerFieldFilter is a filter on a particular marker field.
 * @since 3.4
 */
public abstract class MarkerFieldFilter {

	private MarkerField field;

	/**
	 * Return whether or not marker should be filtered by the receiver.
	 *
	 * @param item item to test
	 * @return boolean <code>true</code> if the marker should be shown.
	 */
	public abstract boolean select(MarkerItem item);

	/**
	 * Initialise the receiver with the values in the values Map.
	 *
	 * @param values filter specific arguments
	 * @see FiltersContributionParameters
	 */
	public void initialize(Map/*<String, String>*/ values) {
		//Do nothing by default
	}

	/**
	 * Populate the working copy with the copy of whatever fields are required.
	 *
	 * @param copy object to populate
	 */
	public void populateWorkingCopy(MarkerFieldFilter copy){
		copy.field = this.field;
	}

	/**
	 * Set the field for the receiver.
	 *
	 * @param markerField the field
	 */
	public final void setField(MarkerField markerField) {
		field = markerField;

	}

	/**
	 * Get the field for the receiver.
	 * @return MarkerField
	 */
	public final MarkerField getField() {
		return field;
	}

	/**
	 * Save any of the relevant state for the receiver in the memento so that it can
	 * be used to restore the user settings.
	 *
	 * @param memento the memento to save in
	 * @see #loadSettings(IMemento)
	 */
	public abstract void saveSettings(IMemento memento) ;

	/**
	 * Load any settings for the receiver from the memento.
	 *
	 * @param memento the memento to load from
	 * @see #saveSettings(IMemento)
	 */
	public abstract void loadSettings(IMemento memento) ;
}

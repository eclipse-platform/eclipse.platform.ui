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

/**
 * MarkerState is the object that handles the state of an ExtendedMarkersView.
 * 
 * @since 3.4
 * 
 */
public class MarkerState {

	private IMemento memento;

	/**
	 * Create a new instance of the receiver from the supplied memento.
	 * 
	 * @param memento
	 */
	public MarkerState(IMemento memento) {
		this.memento = memento;
	}

	/**
	 * Return whether or not the receiver was the primary sort field.
	 * 
	 * @param markerField
	 * @return
	 */
	public boolean isPrimarySortField(MarkerField markerField) {
		// TODO Hook this up to the memento
		return markerField instanceof MarkerSeverityAndMessageField;
	}

	/**
	 * Return whether or not we are set to the default.
	 * @return
	 */
	public boolean useDefaults() {
		return memento == null;
	}

	/**
	 * Return the integer value for the tag.
	 * @param tag
	 * @return
	 */
	public Integer getInteger(String tag) {
		return memento.getInteger(tag);
	}

	/**
	 * Return the String associated with tag.
	 * @param tag
	 * @return String
	 */
	public String getString(String tag) {
		return memento.getString(tag);
	}

	/**
	 * Return the sort direction stored for the field.
	 * @param field
	 * @return
	 */
	public int getSortDirection(MarkerField field) {
		return field.getDefaultDirection();
	}

}

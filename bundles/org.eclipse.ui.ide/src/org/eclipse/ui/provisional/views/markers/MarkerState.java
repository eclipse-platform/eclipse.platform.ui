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

package org.eclipse.ui.provisional.views.markers;

/**
 * MarkerState is the class that manages the state of an instnace of a marker view.
 * @since 3.3
 *
 */
class MarkerState {

	private IMarkerProvider provider;

	/**
	 * Get the current sort direction for the field/
	 * @param field
	 * @return int one of {@link IMarkerField#DESCENDING} or {@link IMarkerField#ASCENDING}
	 */
	public int getSortDirection(IMarkerField field) {
		//TODO handle mementos here
		return field.getDefaultDirection();
	}

	/**
	 * Return whether or not this is the primary sort field for the receiver.
	 * @param markerField
	 * @return boolean <code>true</code> if it is the primary field.
	 */
	public boolean isPrimarySortField(IMarkerField markerField) {
		//TODO handle mementos here
		return provider.isPrimarySortField(markerField);
	}

	/**
	 * Get the currently displayed fields for the receiver.
	 * @return {@link IMarkerField}[]
	 */
	public IMarkerField[] getFields() {
		return provider.getFields();
	}

}

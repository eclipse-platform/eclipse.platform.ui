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

import org.eclipse.ui.views.markers.internal.FieldMarkerGroup;

/**
 * MarkerGroupField is the field that wrappers a FieldMarkerGroup.
 * @since 3.4
 *
 */
class MarkerGroupField extends MarkerField {

	private FieldMarkerGroup markerGroup;

	/**
	 * Create an instance of the receiver.
	 * @param group
	 */
	public MarkerGroupField(FieldMarkerGroup group) {
		markerGroup = group;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return markerGroup.getColumnHeaderText();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		return null;
	}

}

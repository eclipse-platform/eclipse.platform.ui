/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * MarkerLocationField is the field for the location field.
 * 
 * @since 3.4
 * 
 */
public class MarkerLocationField extends MarkerField {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		return item.getLocation();
	}

	/**
	 * Get the number of characters that should be reserved for the receiver.
	 * 
	 * @param control
	 *            the control to scale from
	 * @return int
	 */
	public int getDefaultColumnWidth(Control control) {
		return 15 * MarkerSupportInternalUtilities.getFontWidth(control);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.MarkerField#compare(org.eclipse.ui.views.markers.MarkerItem,
	 *      org.eclipse.ui.views.markers.MarkerItem)
	 */
	public int compare(MarkerItem item1, MarkerItem item2) {

		//See if location got overridden
		String location1 = item1.getAttributeValue(IMarker.LOCATION,
				MarkerSupportInternalUtilities.EMPTY_STRING);
		String location2 = item2.getAttributeValue(IMarker.LOCATION,
				MarkerSupportInternalUtilities.EMPTY_STRING);

		if (location1.length() == 0 || location2.length() == 0)
			return item1.getAttributeValue(IMarker.LINE_NUMBER, -1)
					- item2.getAttributeValue(IMarker.LINE_NUMBER, -1);

		return location1.compareTo(location2);

	}
}

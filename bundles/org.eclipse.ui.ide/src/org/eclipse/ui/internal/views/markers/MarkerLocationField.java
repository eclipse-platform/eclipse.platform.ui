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

package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * MarkerLocationField is the field for the location field.
 *
 * @since 3.4
 */
public class MarkerLocationField extends MarkerField {

	@Override
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
	@Override
	public int getDefaultColumnWidth(Control control) {
		return 15 * MarkerSupportInternalUtilities.getFontWidth(control);
	}

	@Override
	public int compare(MarkerItem item1, MarkerItem item2) {

		//See if location got overridden
		String location1 = item1.getAttributeValue(IMarker.LOCATION, MarkerItemDefaults.LOCATION_DEFAULT);
		String location2 = item2.getAttributeValue(IMarker.LOCATION, MarkerItemDefaults.LOCATION_DEFAULT);
		int c = Boolean.compare(location1.isEmpty(), location2.isEmpty());
		if (c != 0) {
			return c;
		}
		c = location1.compareTo(location2);
		if (c != 0) {
			return c;
		}
		return Integer.compare(item1.getAttributeValue(IMarker.LINE_NUMBER, -1),
				item2.getAttributeValue(IMarker.LINE_NUMBER, -1));
	}
}

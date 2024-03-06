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
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * MarkerSeverityField is the field for showing severity categories.
 *
 * @since 3.4
 */
public class MarkerSeverityField extends MarkerField {

	@Override
	public String getValue(MarkerItem item) {

		switch (item.getAttributeValue(IMarker.SEVERITY, -1)) {
		case 2:
			return MarkerMessages.filtersDialog_severityError;
		case 1:
			return MarkerMessages.filtersDialog_severityWarning;
		case 0:
			return MarkerMessages.filtersDialog_severityInfo;

		default:
			return MarkerSupportInternalUtilities.EMPTY_STRING;
		}

	}


	@Override
	public int compare(MarkerItem item1, MarkerItem item2) {
		return Integer.compare(MarkerSupportInternalUtilities.getSeverity(item1),
				MarkerSupportInternalUtilities.getSeverity(item2));
	}
}

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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerField;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerSupportConstants;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.internal.MarkerGroup;
import org.eclipse.ui.views.markers.internal.MarkerGroupingEntry;

/**
 * MarkerGroupField is the field that wrappers a FieldMarkerGroup.
 * 
 * @since 3.4
 * 
 */
class MarkerGroupField extends MarkerField {

	private MarkerGroup markerGroup;

	/**
	 * Create an instance of the receiver.
	 * 
	 * @param group
	 */
	public MarkerGroupField(MarkerGroup group) {
		markerGroup = group;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return markerGroup.getColumnHeaderText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		if (item.isConcrete()) {
			IMarker marker = ((MarkerEntry) item).getMarker();			
			MarkerGroupingEntry groupingEntry;
			try {
				groupingEntry = markerGroup.findGroupValue(marker.getType(), marker);
				return groupingEntry.getLabel();
			} catch (CoreException e) {
				StatusManager.getManager().handle(e.getStatus());
				return MarkerSupportConstants.EMPTY_STRING;
			}
			
		}
		return item.getDescription();
	}

}

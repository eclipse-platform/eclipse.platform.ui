/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.MarkerViewUtil;

/**
 * MarkerResourceField is the field that specifies the resource column.
 * 
 * @since 3.4
 * 
 */
public class MarkerResourceField extends MarkerField {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.IMarkerField#getValue(org.eclipse.ui.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		if (item.getMarker() == null)
			return MarkerSupportInternalUtilities.EMPTY_STRING;

		return TextProcessor.process(item.getAttributeValue(MarkerViewUtil.NAME_ATTRIBUTE,
				item.getMarker().getResource().getName()));

	}
}

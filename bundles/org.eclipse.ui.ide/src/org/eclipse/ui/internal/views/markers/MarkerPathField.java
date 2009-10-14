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
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * MarkerPathField is the field for the paths column.
 * 
 * @since 3.4
 * 
 */
public class MarkerPathField extends MarkerField {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.IMarkerField#compare(org.eclipse.ui.internal.provisional.views.markers.MarkerItem,
	 *      org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public int compare(MarkerItem item1, MarkerItem item2) {
		if (item1.getMarker() == null || item2.getMarker() == null)
			return 0;

		return item1.getPath().compareTo(item2.getPath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#getDefaultColumnWidth(org.eclipse.swt.widgets.Control)
	 */
	public int getDefaultColumnWidth(Control control) {
		return 20 * MarkerSupportInternalUtilities.getFontWidth(control);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.IMarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		return TextProcessor.process(item.getPath());
	}

}

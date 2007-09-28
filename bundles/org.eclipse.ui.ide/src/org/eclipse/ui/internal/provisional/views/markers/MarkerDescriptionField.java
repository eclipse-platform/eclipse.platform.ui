package org.eclipse.ui.internal.provisional.views.markers;
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

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerField;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerSupportConstants;

import com.ibm.icu.text.CollationKey;

/**
 * MarkerDescriptionField is the field for showing the description of a marker.
 * @since 3.4 
 *
 */
public class MarkerDescriptionField extends MarkerField {

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkerDescriptionField() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#compare(org.eclipse.ui.internal.provisional.views.markers.MarkerItem,
	 *      org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public int compare(MarkerItem item1, MarkerItem item2) {
		return getDescriptionKey(item1).compareTo(getDescriptionKey(item2));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#getDefaultColumnWidth(org.eclipse.swt.widgets.Control)
	 */
	public int getDefaultColumnWidth(Control control) {
		return 50 * getFontWidth(control);
	}

	/**
	 * Return the collation key for the description.
	 * 
	 * @param element
	 * @return CollationKey
	 */
	private CollationKey getDescriptionKey(Object element) {
		if (element instanceof MarkerEntry)
			return ((MarkerItem) element).getCollationKey(IMarker.MESSAGE,
					MarkerSupportConstants.EMPTY_STRING);
		return MarkerSupportInternalUtilities.EMPTY_COLLATION_KEY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		return item.getAttributeValue(IMarker.MESSAGE,
				MarkerSupportConstants.EMPTY_STRING);
	}

}

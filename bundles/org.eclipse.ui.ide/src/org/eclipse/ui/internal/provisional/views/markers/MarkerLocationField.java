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

import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerField;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;


/**
 * MarkerLocationField is the field for the location field.
 * @since 3.4
 *
 */
public class MarkerLocationField extends MarkerField {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		return item.getLocation();
	}

	/**
	 * Get the number of characters that should be reserved for the receiver.
	 * 
	 * @param control the control to scale from
	 * @return int
	 */
	public int getDefaultColumnWidth(Control control) {
		return 10 * getFontWidth(control);
	}
}

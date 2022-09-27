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

	@Override
	public int compare(MarkerItem item1, MarkerItem item2) {
		if (item1.getMarker() == null && item2.getMarker() == null)
			return 0;
		if (item1.getMarker() == null)
			return 1;
		if (item2.getMarker() == null)
			return -1;

		return item1.getPath().compareTo(item2.getPath());
	}

	@Override
	public int getDefaultColumnWidth(Control control) {
		return 20 * MarkerSupportInternalUtilities.getFontWidth(control);
	}

	@Override
	public String getValue(MarkerItem item) {
		return TextProcessor.process(item.getPath());
	}

}

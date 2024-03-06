/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * MarkerSeverityAndDescriptionField can handle severities for all markers.
 *
 * @since 3.4
 */
public class MarkerSeverityAndDescriptionField extends MarkerDescriptionField {

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkerSeverityAndDescriptionField() {
		super();
	}

	@Override
	public int compare(MarkerItem item1, MarkerItem item2) {
		int c = Integer.compare(MarkerSupportInternalUtilities.getSeverity(item1),
				MarkerSupportInternalUtilities.getSeverity(item2));
		if (c != 0) {
			return c;
		}
		return super.compare(item1, item2);
	}

	/**
	 * Return the image for item.
	 *
	 * @return Image or <code>null</code>
	 */
	private Image getImage(MarkerItem item) {
		if (item.getMarker() == null) {
			int severity = ((MarkerCategory) item).getHighestSeverity();
			if (severity >= IMarker.SEVERITY_WARNING)
				return MarkerSupportInternalUtilities.getSeverityImage(severity);
			return null;
		}
		int severity = MarkerSupportInternalUtilities.getSeverity(item);
		return MarkerSupportInternalUtilities.getSeverityImage(severity);
	}

	@Override
	public void update(ViewerCell cell) {
		super.update(cell);

		MarkerItem item = (MarkerItem) cell.getElement();
		cell.setImage(annotateImage(item, getImage(item)));
	}
}

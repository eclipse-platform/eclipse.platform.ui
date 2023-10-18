/*******************************************************************************
 * Copyright (c) 2007,2015 IBM Corporation and others.
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

import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * MarkerSeverityAndMessageField is the field for severity and messages.
 *
 * @since 3.4
 *
 */
public class MarkerProblemSeverityAndMessageField extends
		MarkerDescriptionField {

	@Override
	public int compare(MarkerItem item1, MarkerItem item2) {

		int severity1 = MarkerSupportInternalUtilities.getSeverity(item1);
		int severity2 = MarkerSupportInternalUtilities.getSeverity(item2);
		if (severity1 == severity2)
			return super.compare(item1, item2);
		return severity2 - severity1;
	}

	/**
	 * Return the image for the receiver.
	 *
	 * @param item
	 * @return Image or <code>null</code>
	 */
	private Image getImage(MarkerItem item) {

		MarkerSupportItem supportItem = (MarkerSupportItem) item;

		int severity = -1;
		if (supportItem.isConcrete())
			severity = MarkerSupportInternalUtilities.getSeverity(item);
		else
			severity = ((MarkerCategory) supportItem).getHighestSeverity();

		if (severity >= 0)
			return MarkerSupportInternalUtilities.getSeverityImage(severity);
		try {
			if (supportItem.isConcrete())
				return null;
			return JFaceResources
					.getResources()
					.createImageWithDefault(
							IDEInternalWorkbenchImages
									.getImageDescriptor(IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEM_CATEGORY));
		} catch (DeviceResourceException e) {
			return null;
		}

	}

	@Override
	public void update(ViewerCell cell) {
		super.update(cell);

		if (cell.getElement() instanceof MarkerItem item) {
			cell.setImage(annotateImage(item, getImage(item)));
		}
	}
}

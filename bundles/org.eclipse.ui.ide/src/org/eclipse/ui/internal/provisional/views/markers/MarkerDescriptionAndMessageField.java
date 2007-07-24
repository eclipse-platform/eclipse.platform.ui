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

import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;


/**
 * MarkerDescriptionAndMessageField is the field for severity and messages.
 * 
 * @since 3.3
 * 
 */
public class MarkerDescriptionAndMessageField extends MarkerDescriptionField {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.IMarkerField#compare(org.eclipse.ui.provisional.views.markers.MarkerItem,
	 *      org.eclipse.ui.provisional.views.markers.MarkerItem)
	 */
	public int compare(MarkerItem item1, MarkerItem item2) {

		int severity1 = getSeverity(item1);
		int severity2 = getSeverity(item2);
		if (severity1 == severity2)
			return super.compare(item1, item2);
		return severity2 - severity1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.IMarkerField#getImage(org.eclipse.ui.provisional.views.markers.MarkerItem)
	 */
	public Image getImage(MarkerItem item) {
		if (item.isConcrete())
			return MarkerUtilities.getSeverityImage(getSeverity(item));

		try {
			return JFaceResources
					.getResources()
					.createImageWithDefault(
							IDEInternalWorkbenchImages
									.getImageDescriptor(IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEM_CATEGORY));
		} catch (DeviceResourceException e) {
			return null;
		}

	}

}

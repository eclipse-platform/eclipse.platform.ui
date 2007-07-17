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
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

import com.ibm.icu.text.CollationKey;

/**
 * MarkerSeverityAndMessageField is the field for severity and messages.
 * 
 * @since 3.3
 * 
 */
public class MarkerSeverityAndMessageField extends MarkerField {

	/**
	 * Return the collation key for the description.
	 * 
	 * @param element
	 * @return
	 */
	private CollationKey getDescriptionKey(Object element) {
		if (element instanceof MarkerEntry)
			return ((MarkerItem) element).getCollationKey(IMarker.MESSAGE,
					MarkerUtilities.EMPTY_STRING);
		return MarkerUtilities.EMPTY_COLLATION_KEY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.IMarkerField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return MarkerMessages.description_message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.IMarkerField#getColumnWeight()
	 */
	public float getColumnWeight() {
		return 4;
	}


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
			return getDescriptionKey(item1).compareTo(getDescriptionKey(item2));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.IMarkerField#getValue(org.eclipse.ui.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		return item.getAttributeValue(IMarker.MESSAGE,
				MarkerUtilities.EMPTY_STRING);
	}

}

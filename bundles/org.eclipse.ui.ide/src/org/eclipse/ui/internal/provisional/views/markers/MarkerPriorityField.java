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
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerField;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerSupportConstants;

/**
 * MarkerPriorityField is the field for task priority.
 * @since 3.4
 *
 */
public class MarkerPriorityField extends MarkerField {
	
    static final String HIGH_PRIORITY_IMAGE_PATH = "obj16/hprio_tsk.gif"; //$NON-NLS-1$

    static final String LOW_PRIORITY_IMAGE_PATH = "obj16/lprio_tsk.gif"; //$NON-NLS-1$


	/**
	 * Return a new priority field.
	 */
	public MarkerPriorityField() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		return MarkerSupportConstants.EMPTY_STRING;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return MarkerSupportConstants.EMPTY_STRING;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getColumnWeight()
	 */
	public float getColumnWeight() {
		return 0.25f;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getImage(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public Image getImage(MarkerItem item) {
        try {
            int priority = item.getAttributeValue(IMarker.PRIORITY,
                    IMarker.PRIORITY_NORMAL);
            if (priority == IMarker.PRIORITY_HIGH) {
                return MarkerSupportInternalUtilities.createImage(HIGH_PRIORITY_IMAGE_PATH);
            }
            if (priority == IMarker.PRIORITY_LOW) {
                return MarkerSupportInternalUtilities.createImage(LOW_PRIORITY_IMAGE_PATH);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }
	
}

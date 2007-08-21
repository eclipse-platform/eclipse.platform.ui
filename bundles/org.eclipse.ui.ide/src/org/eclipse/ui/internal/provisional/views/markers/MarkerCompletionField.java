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
 * MarkerCompletionField is the class that specifies the completion entry.
 * 
 * @since 3.4
 * 
 */
public class MarkerCompletionField extends MarkerField {

	static final String COMPLETE_IMAGE_PATH = "$nl$/icons/full/obj16/complete_tsk.gif"; //$NON-NLS-1$

	static final String INCOMPLETE_IMAGE_PATH = "$nl$/icons/full/obj16/incomplete_tsk.gif"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkerCompletionField() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getColumnWeight()
	 */
	public float getColumnWeight() {
		return 0.25f;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return MarkerSupportConstants.EMPTY_STRING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		return MarkerSupportConstants.EMPTY_STRING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getImage(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public Image getImage(MarkerItem item) {

		int done = -1;

		if (item.getAttributeValue(IMarker.USER_EDITABLE, true)) {
			done = 0;
			if (item.getAttributeValue(IMarker.DONE, false)) {
				done = 1;
			}
		}
		if (done == -1)
			return null;

		if (done == 1) {
			return MarkerSupportInternalUtilities.createImage(COMPLETE_IMAGE_PATH);
		}
		return MarkerSupportInternalUtilities.createImage(INCOMPLETE_IMAGE_PATH);
	}

	
}

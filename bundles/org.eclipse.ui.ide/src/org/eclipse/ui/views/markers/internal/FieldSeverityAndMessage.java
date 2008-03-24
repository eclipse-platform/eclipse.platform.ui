/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;

/**
 * The FieldSeverityAndMessage is the field that
 * displays severities and messages.
 * 
 */
public class FieldSeverityAndMessage extends FieldMessage {

	private String description;

	/**
	 * Create a new instance of the receiver.
	 */
	public FieldSeverityAndMessage() {
		description = MarkerMessages.problemSeverity_description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		if (obj == null || !(obj instanceof MarkerNode)) {
			return null;
		}

		MarkerNode node = (MarkerNode) obj;
		if (node.isConcrete()) {
			if (node instanceof ProblemMarker) {
				return Util.getImage(((ProblemMarker) obj).getSeverity());
			}
			return null;
		}

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
	 * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object,
	 *      java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2) {
		if (obj1 == null || obj2 == null || !(obj1 instanceof ProblemMarker)
				|| !(obj2 instanceof ProblemMarker)) {
			return 0;
		}
		
		ProblemMarker marker1 = (ProblemMarker) obj1;
		ProblemMarker marker2 = (ProblemMarker) obj2;

		int severity1 = marker1.getSeverity();
		int severity2 = marker2.getSeverity();
		if(severity1 == severity2)
			return marker1.getDescriptionKey().compareTo(
					marker2.getDescriptionKey());
		return severity2 - severity1;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderImage()
	 */
	public Image getColumnHeaderImage() {
		return getImage(FieldDone.DESCRIPTION_IMAGE_PATH);
	}


}

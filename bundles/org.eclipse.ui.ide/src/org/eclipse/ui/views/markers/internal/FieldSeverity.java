/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;

import org.eclipse.swt.graphics.Image;

public class FieldSeverity implements IField {
	
	private static final String IMAGE_ERROR_PATH = "obj16/error_tsk.gif"; //$NON-NLS-1$
	private static final String IMAGE_WARNING_PATH = "obj16/warn_tsk.gif"; //$NON-NLS-1$
	private static final String IMAGE_INFO_PATH = "obj16/info_tsk.gif"; //$NON-NLS-1$
	
	private String description;
	private Image image;
	
	public FieldSeverity() {
		description = Messages.getString("problemSeverity.description"); //$NON-NLS-1$
		image = null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescriptionImage()
	 */
	public Image getDescriptionImage() {
		return image;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return ""; //$NON-NLS-1$
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderImage()
	 */
	public Image getColumnHeaderImage() {
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getValue(java.lang.Object)
	 */
	public String getValue(Object obj) {
		return "" + ((ProblemMarker)obj).getSeverity(); //$NON-NLS-1$
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		if (obj == null || !(obj instanceof ProblemMarker)) {
			return null;
		}
		
		
		int severity = ((ProblemMarker)obj).getSeverity();
		if (severity == IMarker.SEVERITY_ERROR) {
			return ImageFactory.getImage(IMAGE_ERROR_PATH);
		}
		if (severity == IMarker.SEVERITY_WARNING) {
			return ImageFactory.getImage(IMAGE_WARNING_PATH);
		}
		if (severity == IMarker.SEVERITY_INFO) {
			return ImageFactory.getImage(IMAGE_INFO_PATH);
		}
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2) {
		if (obj1 == null || obj2 == null || !(obj1 instanceof ProblemMarker) || !(obj2 instanceof ProblemMarker)) {
			return 0;
		}

		int severity1 = ((ProblemMarker)obj1).getSeverity();
		int severity2 = ((ProblemMarker)obj2).getSeverity();
		return severity1 - severity2;
	}

}

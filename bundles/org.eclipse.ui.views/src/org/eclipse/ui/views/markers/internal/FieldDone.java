/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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

public class FieldDone implements IField {
	
	public static final String COMPLETION = "completion"; //$NON-NLS-1$
	
	static final String DESCRIPTION_IMAGE_PATH = "obj16/header_complete.gif"; //$NON-NLS-1$
	static final String COMPLETE_IMAGE_PATH = "obj16/complete_tsk.gif"; //$NON-NLS-1$
	static final String INCOMPLETE_IMAGE_PATH = "obj16/incomplete_tsk.gif"; //$NON-NLS-1$

	private String name;
	private String description;
	private Image image;
	
	public FieldDone() {
		name = COMPLETION;
		description = Messages.getString(name + ".description"); //$NON-NLS-1$
		image = ImageFactory.getImage(DESCRIPTION_IMAGE_PATH);
	}

	/**
	 * @see org.eclipse.ui.views.markerview.IField#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.eclipse.ui.views.markerview.IField#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @see org.eclipse.ui.views.markerview.IField#getDescriptionImage()
	 */
	public Image getDescriptionImage() {
		return image;
	}

	/**
	 * @see org.eclipse.ui.views.markerview.IField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.ui.views.markerview.IField#getColumnHeaderImage()
	 */
	public Image getColumnHeaderImage() {
		return image;
	}

	/**
	 * @see org.eclipse.ui.views.markerview.IField#getValue(java.lang.Object)
	 */
	public String getValue(Object obj) {
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.ui.views.markerview.IField#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		if (obj == null || !(obj instanceof IMarker)) {
			return null;
		}
		IMarker marker = (IMarker) obj;
		if (!marker.getAttribute(IMarker.USER_EDITABLE, true)) {
			return null;
		}
		if (marker.getAttribute(IMarker.DONE, false)) {
			return ImageFactory.getImage(COMPLETE_IMAGE_PATH);
		}
		return ImageFactory.getImage(INCOMPLETE_IMAGE_PATH);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (!(other instanceof IField)) {
			return false;
		}
		IField otherProperty = (IField) other;
		return (this.name.equals(otherProperty.getName()));
	}

	/**
	 * @see org.eclipse.ui.views.markerview.IField#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2) {
		if (obj1 == null || obj2 == null || !(obj1 instanceof IMarker) || !(obj2 instanceof IMarker)) {
			return 0;
		}
		IMarker marker1 = (IMarker) obj1;
		IMarker marker2 = (IMarker) obj2;
		int value1 = -1;
		if (marker1.getAttribute(IMarker.USER_EDITABLE, true)) {
			value1 = 0;
			if (marker1.getAttribute(IMarker.DONE, false)) {
				value1 = 1;
			}
		}
		int value2 = -1;
		if (marker2.getAttribute(IMarker.USER_EDITABLE, true)) {
			value2 = 0;
			if (marker2.getAttribute(IMarker.DONE, false)) {
				value2 = 1;
			}
		}
		return value1 - value2;
	}

}

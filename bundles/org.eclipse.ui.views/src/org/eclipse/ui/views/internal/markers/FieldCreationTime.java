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

package org.eclipse.ui.views.internal.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;


/**
 * Creation time field. Designed to display and compare creation times of IMarker objects.
 */
public class FieldCreationTime implements IField {

	public static final String CREATION_TIME = "creationTime"; //$NON-NLS-1$

	private String name;
	private String description;
	private Image image;
	
	/**
	 * The constructor
	 */
	public FieldCreationTime() {
		name = CREATION_TIME;
		description = Messages.getString("description." + name); //$NON-NLS-1$
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
		return description;
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
		if (obj == null || !(obj instanceof IMarker)) {
			return ""; //$NON-NLS-1$
		}
		IMarker marker = (IMarker) obj;
		return Util.getCreationTime(marker);
	}

	/**
	 * @see org.eclipse.ui.views.markerview.IField#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		return null;
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
		try {
			long value = marker1.getCreationTime() - marker2.getCreationTime();
			return (new Long(value)).intValue();
		}
		catch (CoreException e) {
		}
		return 0;
	}

}

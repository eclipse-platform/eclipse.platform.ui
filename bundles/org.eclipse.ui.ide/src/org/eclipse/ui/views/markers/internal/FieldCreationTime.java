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

import org.eclipse.swt.graphics.Image;


/**
 * Creation time field. Designed to display and compare creation times of IMarker objects.
 */
public class FieldCreationTime implements IField {

	private String description;
	private Image image;
	
	/**
	 * The constructor
	 */
	public FieldCreationTime() {
		description = Messages.getString("description.creationTime"); //$NON-NLS-1$
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
		if (obj == null || !(obj instanceof ConcreteMarker)) {
			return ""; //$NON-NLS-1$
		}
		ConcreteMarker marker = (ConcreteMarker)obj;
		return Util.getCreationTime(marker.getCreationTime());
	}

	/**
	 * @see org.eclipse.ui.views.markerview.IField#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		return null;
	}

	/**
	 * @see org.eclipse.ui.views.markerview.IField#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2) {
		if (obj1 == null || obj2 == null || !(obj1 instanceof ConcreteMarker) || !(obj2 instanceof ConcreteMarker)) {
			return 0;
		}
		
		ConcreteMarker marker1 = (ConcreteMarker) obj1;
		ConcreteMarker marker2 = (ConcreteMarker) obj2;
		
		long value = marker1.getCreationTime() - marker2.getCreationTime();
		if (value < 0) {
			return -1;
		} else if (value > 0) {
			return 1;
		} else return 0;
	}

}

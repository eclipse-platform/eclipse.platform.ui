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

public class FieldPriority implements IField {
	
	static final String DESCRIPTION_IMAGE_PATH = "obj16/header_priority.gif"; //$NON-NLS-1$
	static final String HIGH_PRIORITY_IMAGE_PATH = "obj16/hprio_tsk.gif";  //$NON-NLS-1$
	static final String LOW_PRIORITY_IMAGE_PATH = "obj16/lprio_tsk.gif";  //$NON-NLS-1$
	
	private String name;
	private String description;
	private Image image;
	
	public FieldPriority() {
		name = IMarker.PRIORITY;
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
		try {
			int priority = Integer.parseInt(Util.getProperty(name, (IMarker) obj));
			if (priority == IMarker.PRIORITY_HIGH) {
				return ImageFactory.getImage(HIGH_PRIORITY_IMAGE_PATH);
			}
			if (priority == IMarker.PRIORITY_LOW) {
				return ImageFactory.getImage(LOW_PRIORITY_IMAGE_PATH);
			}
		}
		catch (NumberFormatException e) {		
		}
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
		int priority1 = -1;
		try {
			priority1 = Integer.parseInt(Util.getProperty(name, (IMarker) obj1));
		}
		catch (NumberFormatException e) {
		}
		int priority2 = -1;
		try {
			priority2 = Integer.parseInt(Util.getProperty(name, (IMarker) obj2));
		}
		catch (NumberFormatException e) {
		}
		return priority1 - priority2;
	}

}

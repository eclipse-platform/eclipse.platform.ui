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

public class FieldLineNumber implements IField {

	private String name;
	private String description;
	private Image image;
	
	public FieldLineNumber() {
		name = IMarker.LINE_NUMBER;
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
		String lineNumber = Util.getProperty(name, marker);
		if (lineNumber.equals("")) { //$NON-NLS-1$
			return lineNumber;
		}
		return Messages.format("label." + name, new String[] {lineNumber}); //$NON-NLS-1$
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
		int line1 = marker1.getAttribute(IMarker.LINE_NUMBER, -1);
		int line2 = marker2.getAttribute(IMarker.LINE_NUMBER, -1);
		return line1 - line2;
	}

}

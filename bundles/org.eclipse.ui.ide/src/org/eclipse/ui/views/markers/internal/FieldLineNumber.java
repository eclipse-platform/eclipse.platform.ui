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

public class FieldLineNumber implements IField {

	private String description;
	private Image image;
	
	public FieldLineNumber() {
		description = Messages.getString("description.lineNumber"); //$NON-NLS-1$
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
		return description;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderImage()
	 */
	public Image getColumnHeaderImage() {
		return image;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getValue(java.lang.Object)
	 */
	public String getValue(Object obj) {
		if (obj == null || !(obj instanceof ConcreteMarker)) {
			return ""; //$NON-NLS-1$
		}
		ConcreteMarker marker = (ConcreteMarker) obj;
		
		if (marker.getLine() < 0) {
			return ""; //$NON-NLS-1$
		}
		
		return Messages.format("label.lineNumber", new String[] {Integer.toString(marker.getLine())}); //$NON-NLS-1$
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2) {
		if (obj1 == null || obj2 == null || !(obj1 instanceof ConcreteMarker) || !(obj2 instanceof ConcreteMarker)) {
			return 0;
		}
		
		ConcreteMarker marker1 = (ConcreteMarker) obj1;
		ConcreteMarker marker2 = (ConcreteMarker) obj2;
		return marker1.getLine() - marker2.getLine();
	}

}

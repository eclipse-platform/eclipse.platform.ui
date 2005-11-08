package org.eclipse.ui.views.markers.internal;
/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import org.eclipse.swt.graphics.Image;

/**
 * FieldCategory is the field to support categories
 * added via ICategoryProvider.
 *
 */
public class FieldSubCategory implements IField {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescription()
	 */
	public String getDescription() {
		return MarkerMessages.description_subcategory;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescriptionImage()
	 */
	public Image getDescriptionImage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		
		return Util.EMPTY_STRING;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderImage()
	 */
	public Image getColumnHeaderImage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getValue(java.lang.Object)
	 */
	public String getValue(Object obj) {
		MarkerNode node = (MarkerNode) obj;
		if(node.isConcrete())
			return ((ConcreteMarker) obj).getSubCategory();
		return Util.EMPTY_STRING;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2) {
		   if (obj1 == null || obj2 == null || !(obj1 instanceof ConcreteMarker)
	                || !(obj2 instanceof ConcreteMarker)) {
	            return 0;
	        }

	        ConcreteMarker marker1 = (ConcreteMarker) obj1;
	        ConcreteMarker marker2 = (ConcreteMarker) obj2;
	        
	        return marker1.getSubCategory().compareTo(marker2.getSubCategory());
	}
}

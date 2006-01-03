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
 * The FieldCategory is the field for the category entries.
 *
 */
public class FieldHierarchy extends AbstractField {

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
		if(obj instanceof MarkerNode){
			MarkerNode node = (MarkerNode) obj;
			if(node.isConcrete())
				return Util.EMPTY_STRING;
			return node.getDescription();
		}
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
		if (obj1 == null || obj2 == null || !(obj1 instanceof MarkerNode)
				|| !(obj2 instanceof MarkerNode)) {
			return 0;
		}

		MarkerNode marker1 = (MarkerNode) obj1;
		MarkerNode marker2 = (MarkerNode) obj2;
		
		if(marker1.isConcrete() || marker2.isConcrete())
			return 0;	

		return marker1.getDescription().compareTo(
				marker2.getDescription());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDefaultDirection()
	 */
	public int getDefaultDirection() {
		return TableSorter.ASCENDING;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getPreferredWidth()
	 */
	public int getPreferredWidth() {
		return 150;
	}
}

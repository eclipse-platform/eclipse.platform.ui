/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.provisional.views.markers;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.markers.MarkerViewUtil;

/**
 * MarkerResourceField is the field that specifies the resource column.
 * 
 * @since 3.4
 * 
 */
public class MarkerResourceField implements IMarkerField {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.IMarkerField#compare(org.eclipse.ui.provisional.views.markers.MarkerItem,
	 *      org.eclipse.ui.provisional.views.markers.MarkerItem)
	 */
	public int compare(MarkerItem item1, MarkerItem item2) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.IMarkerField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.IMarkerField#getColumnWeight()
	 */
	public float getColumnWeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.IMarkerField#getDefaultDirection()
	 */
	public int getDefaultDirection() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.IMarkerField#getImage(org.eclipse.ui.provisional.views.markers.MarkerItem)
	 */
	public Image getImage(MarkerItem item) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.IMarkerField#getValue(org.eclipse.ui.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		if (item.isConcrete()) {
			return item.getAttributeValue(MarkerViewUtil.NAME_ATTRIBUTE, item
					.getConcreteRepresentative().getMarker().getResource()
					.getName());
		}
		return MarkerUtilities.EMPTY_STRING;
	}
}

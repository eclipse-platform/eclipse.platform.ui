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

package org.eclipse.ui.internal.provisional.views.markers;

import java.util.Comparator;

/**
 * The MarkerComparator is the class that handles the comparison of markers for
 * a specific content provider.
 * 
 * @since 3.4
 * 
 */
class MarkerComparator implements Comparator {

	private IMarkerField category;
	private IMarkerField[] fields;

	/**
	 * Create a new instance of the receiver categorised by categoryField
	 * 
	 * @param categoryField
	 *            May be <code>null/<code>
	 * @param mainFields in order of compare significance
	 */
	public MarkerComparator(IMarkerField categoryField,
			IMarkerField[] mainFields) {
		category = categoryField;
		fields = mainFields;
	}

	/**
	 * Return whether not the hierarchy is being shown.
	 * 
	 * @return boolean if there is a category field.
	 */
	public boolean isShowingHierarchy() {
		return category != null;
	}

	/**
	 * Compare the two objects to see if they have the same category value
	 * 
	 * @param object1
	 * @param object2
	 * @return int
	 * @see Comparable#compareTo(Object)
	 */
	public int compareCategory(Object object1, Object object2) {
		if (category == null)
			return 0;
		return category.compare((MarkerItem) object1, (MarkerItem) object2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1) {
		MarkerItem item0 = (MarkerItem) arg0;
		MarkerItem item1 = (MarkerItem) arg1;
		for (int i = 0; i < fields.length; i++) {
			int value = fields[i].compare(item0, item1);
			if (value == 0)
				continue;
			return value;
		}
		return 0;
	}

}

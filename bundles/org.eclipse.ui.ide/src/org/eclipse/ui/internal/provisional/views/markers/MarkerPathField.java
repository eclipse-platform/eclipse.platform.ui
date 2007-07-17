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

import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * MarkerPathField is the field for the paths column.
 * 
 * @since 3.4
 * 
 */
public class MarkerPathField extends MarkerField {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.IMarkerField#compare(org.eclipse.ui.internal.provisional.views.markers.MarkerItem,
	 *      org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public int compare(MarkerItem item1, MarkerItem item2) {
		if (!(item1.isConcrete() && item2.isConcrete()))
			return 0;

		return item1.getPath().compareTo(item2.getPath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.IMarkerField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return MarkerMessages.description_folder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.IMarkerField#getColumnWeight()
	 */
	public float getColumnWeight() {
		return 2;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.IMarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		return item.getPath();
	}

}

/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.Date;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

import com.ibm.icu.text.DateFormat;

/**
 * MarkerCreationTimeField is the field that shows the creation time of a field.
 * 
 * @since 3.4
 * 
 */
public class MarkerCreationTimeField extends MarkerField {

	private DateFormat dateFormat=DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		long creationTime = ((MarkerSupportItem) item).getCreationTime();
		if (creationTime < 0)
			return MarkerSupportInternalUtilities.EMPTY_STRING;
		return String.valueOf(creationTime);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#compare(org.eclipse.ui.internal.provisional.views.markers.MarkerItem,
	 *      org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public int compare(MarkerItem item1, MarkerItem item2) {
		return (int) (((MarkerSupportItem) item1).getCreationTime() - ((MarkerSupportItem) item2)
				.getCreationTime());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#update(org.eclipse.jface.viewers.ViewerCell)
	 */
	public void update(ViewerCell cell) {
		Object element=cell.getElement();
		String timeStr=MarkerSupportInternalUtilities.EMPTY_STRING;;
		if(element instanceof MarkerEntry){
			long creationTime=((MarkerEntry)element).getCreationTime();
			if (creationTime > 0){
				Date date=new Date(creationTime);
				timeStr=dateFormat.format(date);
			}
		}
		cell.setText(timeStr);
	}

}

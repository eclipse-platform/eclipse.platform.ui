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

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * The message field is the field for the description
 * of the marker.
 * @since 3.1
 *
 */
public class FieldMessage implements IField {


	/**
	 * Create an instance of the receiver.
	 */
	public FieldMessage() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescription()
	 */
	public String getDescription() {
		return  MarkerMessages.description_message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescriptionImage()
	 */
	public Image getDescriptionImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return MarkerMessages.description_message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderImage()
	 */
	public Image getColumnHeaderImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getValue(java.lang.Object)
	 */
	public String getValue(Object obj) {
		if (obj == null)
			return MarkerMessages.FieldMessage_NullMessage;

		if (obj instanceof MarkerNode){
			MarkerNode node = (MarkerNode) obj;
	    	if(node.isConcrete())
	    		return node.getDescription();
	    	return Util.EMPTY_STRING;
		}
		
		if(obj instanceof IWorkbenchAdapter)
			return ((IWorkbenchAdapter) obj).getLabel(obj);
		
		if(obj instanceof IMarker)
			 Util.getProperty(IMarker.MESSAGE, (IMarker) obj); 
		
		return NLS.bind(MarkerMessages.FieldMessage_WrongType,obj.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object,
	 *      java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2) {
		if (obj1 == null || obj2 == null || !(obj1 instanceof ConcreteMarker)
				|| !(obj2 instanceof ConcreteMarker)) {
			return 0;
		}

		ConcreteMarker marker1 = (ConcreteMarker) obj1;
		ConcreteMarker marker2 = (ConcreteMarker) obj2;

		return marker1.getDescriptionKey().compareTo(
				marker2.getDescriptionKey());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getCategoryValue(java.lang.Object)
	 */
	public String getCategoryValue(Object obj) {
		return getValue(obj);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#isCategoryField()
	 */
	public boolean isCategoryField() {
		return false;
	}

}

/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * MarkerSupportItem is the internal abstract superclass of the markerSupport {@link MarkerItem}.
 * @since 3.4
 *
 */
abstract class MarkerSupportItem extends MarkerItem {

	/**
	 * Get the String value of the attribute in the enclosed marker.
	 * 
	 * @param attribute
	 * @param defaultValue
	 *            the defaultValue if the value is not set
	 * @return String
	 */
	public String getAttributeValue(String attribute, String defaultValue) {
		// All items have messages
		if (attribute == IMarker.MESSAGE)
			return getDescription();
		return super.getAttributeValue(attribute, defaultValue);

	}

	/**
	 * Get the children of the node.
	 * 
	 * @return MarkerNode[]
	 */
	abstract MarkerSupportItem[] getChildren();

	/**
	 * Return the number of elements that are children of the receiver
	 * @return int
	 */
	int getChildrenCount() {
		return 0;
	}

	/**
	 * Return the creation time for the receiver.
	 * @return long
	 */
	long getCreationTime() {
		return -1;
	}

	/**
	 * Return the description of the receiver.
	 * 
	 * @return String
	 */
	abstract String getDescription();

	/**
	 * Return the ID of the receiver
	 * @return String
	 */
	long getID() {
		return -1;
		
	}

	/**
	 * Get the human readable name of the type/
	 * @return String
	 */
	String getMarkerTypeName() {
		return MarkerSupportInternalUtilities.EMPTY_STRING;
	}

	/**
	 * Return the parent node or <code>null</code> if this is a top level
	 * element.
	 * 
	 * @return MarkerSupportItem
	 */
	abstract MarkerSupportItem getParent();
	
	/**
	 * Return whether or not this is a concrete node
	 * 
	 * @return boolean
	 */
	abstract boolean isConcrete();
	
	/**
	 * Clear the cached values for performance reasons.
	 */
	abstract void clearCache();
	
}

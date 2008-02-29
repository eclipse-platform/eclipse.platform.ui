/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.internal.views.markers.MarkerEntry;

import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;

/**
 * The MarkerItem class is the class that represents the objects displayed in
 * the ExtendedMarkersView.
 * 
 */
public abstract class MarkerItem {

	private CollationKey collationKey;

	/**
	 * Return the boolean associated with attributeName or defaultValue if
	 * it is not found.
	 * @param attribute
	 * @param defaultValue
	 * @return <code>boolean</code>
	 */
	public boolean getAttributeValue(String attribute, boolean defaultValue) {
		// There are no boolean values by default
		return defaultValue;

	}

	/**
	 * Get the value of the attribute in the enclosed marker.
	 * 
	 * @param attribute
	 * @param defaultValue
	 *            the defaultValue if the value is not set
	 * @return int
	 */
	public int getAttributeValue(String attribute, int defaultValue) {
		// There are no integer values by default
		return defaultValue;

	}

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
		return defaultValue;

	}

	/**
	 * Get the children of the node.
	 * 
	 * @return MarkerNode[]
	 */
	public abstract MarkerItem[] getChildren();

	/**
	 * Get the CollationKey for the string attribute.
	 * 
	 * @param attribute
	 * @param defaultValue
	 *            the defaultValue if the value is not set
	 * @return CollationKey
	 */
	public CollationKey getCollationKey(String attribute, String defaultValue) {
		if (collationKey == null)
			collationKey = Collator.getInstance().getCollationKey(
					getDescription());
		return collationKey;
	}

	/**
	 * Get a concrete marker from the receiver. If the receiver is concrete
	 * return the receiver otherwise return one of the concrete markers it
	 * contains.
	 * 
	 * @return MarkerEntry
	 */
	public abstract MarkerEntry getConcreteRepresentative();

	/**
	 * Return the creation time for the receiver.
	 * @return long
	 */
	public long getCreationTime() {
		return -1;
	}

	/**
	 * Return the description of the receiver.
	 * 
	 * @return String
	 */
	public abstract String getDescription();

	/**
	 * Return the ID of the receiver
	 * @return String
	 */
	public long getID() {
		return -1;
		
	}

	/**
	 * Get the location string for the receiver.
	 * 
	 * @return String
	 * @see IMarker#LOCATION
	 */
	public String getLocation() {
		//There is no location by default
		return MarkerSupportConstants.EMPTY_STRING;

	}
	
	/**
	 * Return the marker for the receiver.
	 * @return IMarker
	 */
	public IMarker getMarker() {
		return null;
	}
	
	/**
	 * Return the name of the receiver.
	 * 
	 * @return String
	 */
	public String getName() {
		return getDescription();
	}

	/**
	 * Return the parent node or <code>null</code> if this is a top level
	 * element.
	 * 
	 * @return MarkerNode
	 */
	public abstract MarkerItem getParent();

	/**
	 * Get the path string for the receiver.
	 * 
	 * @return String
	 * @see MarkerViewUtil#PATH_ATTRIBUTE
	 */
	public String getPath() {
		//There is no path by default
		return MarkerSupportConstants.EMPTY_STRING;

	}

	/**
	 * Return whether or not this is a concrete node
	 * 
	 * @return boolean
	 */
	public abstract boolean isConcrete();

	/**
	 * Get the human readable name of the type/
	 * @return String
	 */
	public String getMarkerTypeName() {
		return MarkerSupportConstants.EMPTY_STRING;
	}

	/**
	 * Return the number of elements that are children of the receiver
	 * @return int
	 */
	public int getChildrenCount() {
		return 0;
	}

}

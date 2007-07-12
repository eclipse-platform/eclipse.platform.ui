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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;

import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;


/**
 * The MarkerEntry is the class that wrappers an {@link IMarker} for display in
 * an {@link ExtendedMarkersView}.
 * 
 * @since 3.4
 * 
 */
public class MarkerEntry extends MarkerItem {

	IMarker marker;
	private MarkerCategory category;
	Map attributeCache = new HashMap(0);
	Map collationKeys = new HashMap(0);

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param marker
	 */
	public MarkerEntry(IMarker marker) {
		this.marker = marker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.MarkerItem#getChildren()
	 */
	public MarkerItem[] getChildren() {
		return MarkerUtilities.EMPTY_MARKER_ITEM_ARRAY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.MarkerItem#getDescription()
	 */
	public String getDescription() {
		return getAttributeValue(IMarker.MESSAGE, MarkerUtilities.EMPTY_STRING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.MarkerItem#getParent()
	 */
	public MarkerItem getParent() {
		return category;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.MarkerItem#isConcrete()
	 */
	public boolean isConcrete() {
		return true;
	}

	/**
	 * Return the Marker that the receiver is wrapping.
	 * 
	 * @return {@link IMarker}
	 */
	public IMarker getMarker() {
		return marker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.provisional.views.markers.MarkerItem#getConcreteRepresentative()
	 */
	public MarkerEntry getConcreteRepresentative() {
		return this;
	}

	/**
	 * Set the category to markerCategory.
	 * 
	 * @param markerCategory
	 */
	public void setCategory(MarkerCategory markerCategory) {
		category = markerCategory;

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
		if (!attributeCache.containsKey(attribute))
			attributeCache.put(attribute, new Integer(marker.getAttribute(
					attribute, defaultValue)));
		return ((Integer) attributeCache.get(attribute)).intValue();
	
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
		if (!attributeCache.containsKey(attribute))
			attributeCache.put(attribute, new Integer(marker.getAttribute(
					attribute, defaultValue)));
		return (String) attributeCache.get(attribute);
	
	}

	/**
	 * Get the CollationKey for the string attribute.
	 * 
	 * @param attribute
	 * @param defaultValue
	 *            the defaultValue if the value is not set
	 * @return
	 */
	public CollationKey getCollationKey(String attribute, String defaultValue) {
		if (collationKeys.containsKey(attribute))
			return (CollationKey) collationKeys.get(attribute);
		String attributeValue = getAttributeValue(attribute, defaultValue);
		if (attributeValue.isEmpty())
			return MarkerUtilities.EMPTY_COLLATION_KEY;
		CollationKey key = Collator.getInstance().getCollationKey(
				attributeValue);
		collationKeys.put(attribute, key);
		return key;
	}

}

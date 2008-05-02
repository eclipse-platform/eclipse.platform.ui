/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.internal.views.markers.MarkerSupportInternalUtilities;

/**
 * The MarkerItem class is the class that represents the objects displayed a 
 * {@link MarkerSupportView}. This class is not intended to be sub-classed by clients,
 * @since 3.4
 * 
 */
public abstract class MarkerItem {

	/**
	 * Return the boolean associated with attributeName or defaultValue if it is
	 * not found.
	 * 
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
		return defaultValue;

	}

	/**
	 * Get the location string for the receiver. This method is provided for
	 * convenience purposes as the location can be inferred from a line number
	 * or location attribute if the {@link MarkerItem} has an associated
	 * {@link IMarker}.
	 * 
	 * @return String
	 * @see IMarker#LOCATION
	 * @see IMarker#LINE_NUMBER
	 */
	public String getLocation() {
		return MarkerSupportInternalUtilities.EMPTY_STRING;
	}

	/**
	 * Get the path string for the receiver. This method is provided for
	 * convenience as a path can be inferred from the location of an
	 * {@link IResource} or the path attribute if the {@link MarkerItem} has an
	 * associated {@link IMarker}.
	 * 
	 * @return String
	 * @see MarkerViewUtil#PATH_ATTRIBUTE
	 * @see IResource#getLocation()
	 */
	public String getPath() {
		// There is no path by default
		return MarkerSupportInternalUtilities.EMPTY_STRING;
	}

	/**
	 * Return the marker for the receiver.
	 * 
	 * @return IMarker
	 */
	public IMarker getMarker() {
		return null;
	}
}

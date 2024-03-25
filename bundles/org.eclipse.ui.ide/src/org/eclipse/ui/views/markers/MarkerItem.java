/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - ongoing support
 *******************************************************************************/

package org.eclipse.ui.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.internal.views.markers.MarkerItemDefaults;

/**
 * The MarkerItem class is the class that represents the objects displayed at
 * {@link IMarker} related views. This class is not intended to be sub-classed
 * by clients.
 *
 * @since 3.4
 */
public abstract class MarkerItem {

	/**
	 * Return the boolean associated with attributeName or defaultValue if it is not
	 * found.
	 *
	 * @param attribute    the attribute
	 * @param defaultValue the defaultValue if the value is not set
	 * @return <code>boolean</code>
	 */
	public boolean getAttributeValue(String attribute, boolean defaultValue) {
		// There are no boolean values by default
		return defaultValue;

	}

	/**
	 * Get the value of the attribute in the enclosed marker.
	 *
	 * @param attribute    the attribute
	 * @param defaultValue the defaultValue if the value is not set
	 * @return int
	 */
	public int getAttributeValue(String attribute, int defaultValue) {
		// There are no integer values by default
		return defaultValue;

	}

	/**
	 * Get the String value of the attribute in the enclosed marker.
	 *
	 * @param attribute    the attribute
	 * @param defaultValue the defaultValue if the value is not set
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
		return MarkerItemDefaults.LOCATION_DEFAULT;
	}

	/**
	 * Get the path string for the receiver. This method is provided for convenience
	 * as a path can be inferred from the location of an {@link IResource} or the
	 * path attribute if the {@link MarkerItem} has an associated {@link IMarker}.
	 *
	 * @return String
	 * @see MarkerItemDefaults#PATH_ATTRIBUTE
	 * @see IResource#getLocation()
	 */
	public String getPath() {
		// There is no path by default
		return MarkerItemDefaults.PATH_DEFAULT;
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

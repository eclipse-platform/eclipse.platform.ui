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

import java.util.Hashtable;

import org.eclipse.core.resources.IMarker;

/**
 * The AttributeCategoryProvider is the type that defines the mapping
 * between a type and the attribute it is registered to check.
 * @since 3.2
 *
 */
public class AttributeCategoryProvider {
	
	String attributeName;
	Hashtable mappings = new Hashtable();

	/**
	 * Create a new instance of the receiver on attribute.
	 * @param attribute
	 */
	public AttributeCategoryProvider(String attribute) {
		attributeName = attribute;
	}

	/**
	 * Get the String that this the category for marker. If there
	 * is no mapping then return <code>null</code>.
	 * @param marker
	 * @return String
	 */
	public String categoryFor(IMarker marker) {
		String attribute = marker.getAttribute(attributeName,Util.EMPTY_STRING);
		if(attribute.length() == 0 || !mappings.contains(attribute))
			return null;
		return (String) mappings.get(attribute);
		
	}

	/**
	 * Add label to the mappings at the key value.
	 * @param value the value of the attribute this mapping is for
	 * @param label the label this results in
	 */
	public void put(String value, String label) {
		mappings.put(value, label);
		
	}

}

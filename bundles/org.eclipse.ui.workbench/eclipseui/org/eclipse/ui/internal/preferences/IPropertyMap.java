/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.preferences;

import java.util.Set;

/**
 * Eclipse provides many different classes and interfaces that map ids onto
 * values and send property change notifications (some examples are themes,
 * preference stores, xml nodes, property providers, and others). This interface
 * is intended to provide interoperability between these various classes.
 *
 * @since 3.1
 */
public interface IPropertyMap {

	/**
	 * Returns the set of keys that are recognized by this property map (optional
	 * operation).
	 *
	 * @return the set of valid keys for this map
	 * @throws UnsupportedOperationException if this type of property map cannot
	 *                                       compute the set of valid keys
	 * @since 3.1
	 */
	Set<String> keySet();

	/**
	 * Returns the value of the given property. Returns null if the given property
	 * does not exist, cannot be converted into the expected type, or if the value
	 * of the property is null.
	 *
	 * @param propertyId   property ID to query
	 * @param propertyType type of the expected return value
	 * @return an object of the given propertyType or null if the property does not
	 *         exist or has the wrong type
	 * @since 3.1
	 */
	Object getValue(String propertyId, Class propertyType);

	/**
	 * If this map represents the union of multiple property maps, this returns true
	 * iff the property existed in every map in the union. Always returns true if
	 * this map was not computed from the union of multiple maps.
	 *
	 * @return true iff the given property existed in every child map
	 * @since 3.1
	 */
	boolean isCommonProperty(String propertyId);

	/**
	 * Returns true iff the given property exists.
	 *
	 * @return true iff the given property exists in this map
	 * @since 3.1
	 */
	boolean propertyExists(String propertyId);

	/**
	 * Sets the value of the given property to the given value (optional operation).
	 *
	 * @throws UnsupportedOperationException if this type of property map is
	 *                                       read-only
	 * @since 3.1
	 */
	void setValue(String propertyId, Object newValue);
}

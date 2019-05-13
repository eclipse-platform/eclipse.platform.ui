/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.ide.registry;

import java.util.Arrays;

/**
 * Instances of this class represent the result of a specific marker
 * query. Specifically they contain an ordered collection of marker
 * attribute values.
 */

public class MarkerQueryResult {
	/**
	 * An ordered collection of marker attribute values.
	 */
	private String[] values;

	/**
	 * Cached hash code value
	 */
	private int hashCode;

	/**
	 * Creates a new marker query result with the given values.
	 * <p>
	 * The values may not be empty.
	 * </p>
	 *
	 * @param markerAttributeValues the target marker's attribute values
	 */
	public MarkerQueryResult(String[] markerAttributeValues) {
		if (markerAttributeValues == null) {
			throw new IllegalArgumentException();
		}
		values = markerAttributeValues;
		computeHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MarkerQueryResult)) {
			return false;
		}

		if (o == this) {
			return true;
		}

		MarkerQueryResult mqr = (MarkerQueryResult) o;
		return Arrays.equals(values, mqr.values);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	/**
	 * Computes the hash code for this instance.
	 */
	public void computeHashCode() {
		hashCode = Arrays.hashCode(values);
	}
}

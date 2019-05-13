/*******************************************************************************
 * Copyright (c) 2000, 2015, 2019 IBM Corporation and others.
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
 *     Tim Neumann <tim.neumann@advantest.com> - Bug 543570
 *******************************************************************************/
package org.eclipse.ui.internal.ide.registry;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.internal.ide.Policy;

/**
 * Instances of this class hold a marker type id and/or
 * a series of marker attributes. This information may be used
 * to determine if a given marker is of the same marker
 * type and determine its values for the attributes.
 */
public class MarkerQuery {
	/**
	 * The marker type targetted by this query.
	 * May be <code>null</code>.
	 */
	private String type;

	/**
	 * A sorted list of the attributes targetted by this query.
	 * The list is sorted from least to greatest according to
	 * <code>Sting.compare</code>
	 */
	private String[] attributes;

	/**
	 * Whether this query also targets all children of {@link #type}.
	 */
	private boolean matchTypeChildren;

	/**
	 * Cached hash code value
	 */
	private int hashCode;

	/**
	 * Creates a new marker query with the given type and attributes, only matching
	 * the exact marker type.
	 * <p>
	 * The type may be <code>null</code>. The attributes may be empty, but not
	 * <code>null</code>.
	 * </p>
	 *
	 * @param markerType       the targetted marker type
	 * @param markerAttributes the targetted marker attributes
	 */
	public MarkerQuery(String markerType, String[] markerAttributes) {
		this(markerType, markerAttributes, false);
	}

	/**
	 * Creates a new marker query with the given type and attributes.
	 * <p>
	 * The type may be <code>null</code>. The attributes may be empty, but not
	 * <code>null</code>.
	 * </p>
	 *
	 * @param markerType        the targetted marker type
	 * @param markerAttributes  the targetted marker attributes
	 * @param matchTypeChildren whether to also target children of the markerType
	 */
	public MarkerQuery(String markerType, String[] markerAttributes, boolean matchTypeChildren) {
		if (markerAttributes == null) {
			throw new IllegalArgumentException();
		}

		type = markerType;
		attributes = markerAttributes;
		this.matchTypeChildren = matchTypeChildren;
		computeHashCode();
	}

	/**
	 * Performs a query against the given marker.
	 * <p>
	 * Returns a <code>MarkerQueryResult</code> if the marker
	 * is appropriate for this query (correct type and has
	 * all of the query attributes), otherwise <code>null</code>
	 * is returned.
	 *
	 * @param marker the marker to perform the query against
	 * @return a marker query result or <code>null</code>
	 */
	public MarkerQueryResult performQuery(IMarker marker) {
		// Check type
		try {
			if (type != null) {
				if (matchTypeChildren) {
					if(!marker.isSubtypeOf(type)) {
						return null;
					}
				} else {
					if(!type.equals(marker.getType())) {
						return null;
					}
				}
			}
		} catch (CoreException e) {
			Policy.handle(e);
			return null;
		}

		// Check attributes
		String[] values = new String[attributes.length];
		for (int i = 0; i < attributes.length; i++) {
			try {
				Object value = marker.getAttribute(attributes[i]);
				if (value == null) {
					return null;
				}
				values[i] = value.toString();
			} catch (CoreException e) {
				Policy.handle(e);
				return null;
			}
		}

		// Create and return the result
		return new MarkerQueryResult(values);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MarkerQuery)) {
			return false;
		}

		if (o == this) {
			return true;
		}

		MarkerQuery mq = (MarkerQuery) o;
		return Objects.equals(type, mq.type) && Objects.equals(matchTypeChildren, mq.matchTypeChildren)
				&& Arrays.equals(attributes, mq.attributes);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	/**
	 * Computes the hash code for this instance.
	 */
	public void computeHashCode() {
		hashCode = 19;

		if (type != null) {
			hashCode = hashCode * 37 + type.hashCode();
		}

		hashCode = hashCode * 37 + (matchTypeChildren ? 1 : 2);

		for (String attribute : attributes) {
			hashCode = hashCode * 37 + attribute.hashCode();
		}
	}

	/**
	 * Returns the targetted marker type. May be
	 * <code>null</code>
	 *
	 * @return the targetted marker type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the targetted attributes.
	 * The array may be empty.
	 *
	 * @return the targetted attributes
	 */
	public String[] getAttributes() {
		return attributes;
	}
}


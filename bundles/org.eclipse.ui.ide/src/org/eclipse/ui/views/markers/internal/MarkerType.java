/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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

package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a marker type.
 */
public class MarkerType {
	private MarkerTypesModel model;

	private String id;

	private String label;

	private String[] supertypeIds;

	/**
	 * Creates a new marker type.
	 */
	public MarkerType(MarkerTypesModel model, String id, String label, String[] supertypeIds) {
		this.model = model;
		this.id = id;
		this.label = label;
		this.supertypeIds = supertypeIds;
	}

	/**
	 * Returns all this type's supertypes.
	 *
	 * @return never null
	 */
	public MarkerType[] getAllSupertypes() {
		ArrayList<MarkerType> result = new ArrayList<>();
		getAllSupertypes(result);
		return result.toArray(new MarkerType[result.size()]);
	}

	/**
	 * Appends all this type's supertypes to the given list.
	 */
	private void getAllSupertypes(ArrayList<MarkerType> result) {
		for (MarkerType sup : getSupertypes()) {
			if (!result.contains(sup)) {
				result.add(sup);
				sup.getAllSupertypes(result);
			}
		}
	}

	/**
	 * @return the marker type id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the human-readable label for this marker type.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Returns the types which have this type as a direct supertype.
	 *
	 * @return the direct subtypes of this type
	 */
	public MarkerType[] getSubtypes() {
		MarkerType[] types = model.getTypes();
		ArrayList<MarkerType> result = new ArrayList<>();
		for (MarkerType type : types) {
			for (String supertypeId : type.getSupertypeIds()) {
				if (supertypeId.equals(id)) {
					result.add(type);
				}
			}
		}
		return result.toArray(new MarkerType[result.size()]);
	}

	/**
	 * @return never null
	 */
	public MarkerType[] getAllSubTypes() {
		List<MarkerType> subTypes = new ArrayList<>();
		addSubTypes(subTypes, this);
		MarkerType[] subs = new MarkerType[subTypes.size()];
		subTypes.toArray(subs);
		return subs;
	}

	private void addSubTypes(List<MarkerType> list, MarkerType superType) {
		for (MarkerType subType : superType.getSubtypes()) {
			if (!list.contains(subType)) {
				list.add(subType);
			}
			addSubTypes(list, subType);
		}
	}

	/**
	 * @return the marker type ids for this type's supertypes.
	 */
	public String[] getSupertypeIds() {
		return supertypeIds;
	}

	/**
	 * @return this type's direct supertypes, never null.
	 */
	public MarkerType[] getSupertypes() {
		ArrayList<MarkerType> result = new ArrayList<>();
		for (String supertypeId : supertypeIds) {
			MarkerType sup = model.getType(supertypeId);
			if (sup != null) {
				result.add(sup);
			}
		}
		return result.toArray(new MarkerType[result.size()]);
	}

	/**
	 * Returns whether this marker type is considered to be a subtype of the
	 * given marker type.
	 *
	 * @param superType
	 *
	 * @return boolean <code>true</code>if this type is the same as (or a
	 *         subtype of) the given type
	 */
	public boolean isSubtypeOf(MarkerType superType) {
		if (id.equals(superType.getId())) {
			return true;
		}
		for (String supertypeId : supertypeIds) {
			MarkerType sup = model.getType(supertypeId);
			if (sup != null && sup.isSubtypeOf(superType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MarkerType)) {
			return false;
		}
		return ((MarkerType) other).getId().equals(this.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}

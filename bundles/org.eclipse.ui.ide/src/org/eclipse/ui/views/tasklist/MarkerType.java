/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.tasklist;

import java.util.ArrayList;

/**
 * Represents a marker type.
 */
class MarkerType {
    private MarkerTypesModel model;

    private String id;

    private String label;

    private String[] supertypeIds;

    /**
     * Creates a new marker type.
     */
    public MarkerType(MarkerTypesModel model, String id, String label,
            String[] supertypeIds) {
        this.model = model;
        this.id = id;
        this.label = label;
        this.supertypeIds = supertypeIds;
    }

    /**
     * Returns all this type's supertypes.
     */
    public MarkerType[] getAllSupertypes() {
        ArrayList result = new ArrayList();
        getAllSupertypes(result);
        return (MarkerType[]) result.toArray(new MarkerType[result.size()]);
    }

    /**
     * Appends all this type's supertypes to the given list.
     */
    private void getAllSupertypes(ArrayList result) {
		for (MarkerType sup : getSupertypes()) {
            if (!result.contains(sup)) {
                result.add(sup);
                sup.getAllSupertypes(result);
            }
        }
    }

    /**
     * Returns the marker type id.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the human-readable label for this marker type.
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
        ArrayList result = new ArrayList();
		for (MarkerType markerType : model.getTypes()) {
			for (String supertypeId : markerType.getSupertypeIds()) {
				if (supertypeId.equals(id)) {
					result.add(markerType);
                }
            }
        }
        return (MarkerType[]) result.toArray(new MarkerType[result.size()]);
    }

    /**
     * Returns the marker type ids for this type's supertypes.
     */
    public String[] getSupertypeIds() {
        return supertypeIds;
    }

    /**
     * Returns this type's direct supertypes.
     */
    public MarkerType[] getSupertypes() {
        ArrayList result = new ArrayList();
        for (String supertypeId : supertypeIds) {
            MarkerType sup = model.getType(supertypeId);
            if (sup != null) {
                result.add(sup);
            }
        }
        return (MarkerType[]) result.toArray(new MarkerType[result.size()]);
    }

    /**
     * Returns whether this marker type is considered to be a subtype of
     * the given marker type.
     *
     * @return boolean <code>true</code>if this type is the same as (or a subtype of) the given type
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
}

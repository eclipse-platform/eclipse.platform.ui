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

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;

/**
 * The TypeMarkerGroup is a MarkerGroup used for the sorting by type which
 * cannot be expressed currently using the markerSupport extension point.
 * 
 * @since 3.4
 * 
 */
public class TypeMarkerGroup extends MarkerGroup {

	/**
	 * TypeMarkerField is the MarkerField used for MarkerGroupungs
	 * 
	 * @since 3.4
	 * 
	 */
	class TypeMarkerField extends GroupMarkerField {


		/**
		 * Create a new instance of the receiver.
		 * @param group
		 */
		TypeMarkerField(MarkerGroup group) {
			super(group);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem)
		 */
		public String getValue(MarkerItem item) {

			if (item.isConcrete()) {
				IMarker marker = item.getMarker();
				if (marker == null || !marker.exists())
					return MarkerMessages.FieldCategory_Uncategorized;
				String groupName = MarkerSupportRegistry.getInstance()
						.getCategory(marker);
				if (groupName == null) {

					String typeId;
					try {
						typeId = marker.getType();
					} catch (CoreException e) {
						Util.log(e);
						return MarkerMessages.FieldCategory_Uncategorized;
					}
					MarkerType type = MarkerTypesModel.getInstance().getType(
							typeId);
					groupName = type.getLabel();
				}
				return groupName;
			}

			return Util.EMPTY_STRING;
		}

	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param name
	 */
	public TypeMarkerGroup(String name) {
		super(name, Util.TYPE_MARKER_GROUPING_ID);
	}

	/**
	 * Create the fields for the marker views.
	 */
	protected void createFields() {
		field = new FieldCategory();
		markerField = new TypeMarkerField(this);
	}

}

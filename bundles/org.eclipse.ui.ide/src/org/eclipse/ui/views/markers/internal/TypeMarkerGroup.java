/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * The TypeMarkerGroup is a MarkerGroup used for the sorting by type which
 * cannot be expressed currently using the markerSupport extension point.
 *
 * @since 3.4
 *
 */
public class TypeMarkerGroup extends MarkerGroup {

	private Map<String, TypesMarkerGroupingEntry> entries=new HashMap<>();
	/**
	 * TypeMarkerField is the MarkerField used for MarkerGroupungs
	 *
	 * @since 3.4
	 *
	 */
	class TypeMarkerField extends GroupMarkerField {


		/**
		 * Create a new instance of the receiver.
		 */
		TypeMarkerField() {
			super();
		}

		@Override
		public String getValue(MarkerItem item) {

			if (item.getMarker() != null) {
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
						Policy.handle(e);
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

		@Override
		public int compare(MarkerItem item1, MarkerItem item2) {
			return getValue(item1).compareTo(getValue(item2));
		}

	}

	private String name;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param name
	 */
	public TypeMarkerGroup(String name) {
		super(null);
		this.name = name;
	}

	/**
	 * Create the fields for the marker views.
	 */
	@Override
	protected void createFields() {
		field = new FieldCategory();
		markerField = new TypeMarkerField();
	}

	@Override
	public MarkerGroupingEntry findGroupValue(String typeId, IMarker marker) {
		TypesMarkerGroupingEntry entry = entries
				.get(typeId);
		if (entry == null) {
			String groupName = MarkerSupportRegistry.getInstance().getCategory(
					marker);
			if (groupName == null) {
				MarkerType mkType = MarkerTypesModel.getInstance().getType(
						typeId);
				groupName = mkType.getLabel();
			}
			entry = new TypesMarkerGroupingEntry(groupName);
			entry.setGroup(this);
			entries.put(typeId, entry);
		}
		return entry;
	}

	@Override
	public String getId() {
		return Util.TYPE_MARKER_GROUPING_ID;
	}

	@Override
	public String getTitle() {
		return name;
	}

	@Override
	public Comparator<MarkerGroupingEntry> getEntriesComparator() {
		return (o1, o2) -> o1.getLabel().compareTo(o2.getLabel());
	}

	private class TypesMarkerGroupingEntry extends MarkerGroupingEntry {
		public TypesMarkerGroupingEntry(String label) {
			super(label);
		}
	}
}

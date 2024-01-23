/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.internal.views.markers.MarkerSupportInternalUtilities;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * @since 3.2
 */
public class MarkerGroup {

	/**
	 * Create a new MarkerGroup from element.
	 *
	 * @return MarkerGroup
	 */
	public static MarkerGroup createMarkerGroup(IConfigurationElement element) {
		if (element.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID).equals(Util.TYPE_MARKER_GROUPING_ID)) {
			return new TypeMarkerGroup(element.getAttribute(MarkerSupportRegistry.LABEL));
		}
		return new MarkerGroup(element);
	}

	class AttributeMapping extends EntryMapping {

		String attribute;

		String attributeValue;

		AttributeMarkerGrouping grouping;

		/**
		 * Create a mapping for an attribute with name attributeName and value
		 * value to the supplied entry.
		 */
		AttributeMapping(MarkerGroupingEntry entry, String attributeName, String value, AttributeMarkerGrouping grouping) {
			super(entry);
			attribute = attributeName;
			attributeValue = value;
			this.grouping = grouping;
		}

		@Override
		public boolean hasAttributes() {
			return true;
		}

		@Override
		public MarkerGroupingEntry testAttribute(IMarker marker) {
			Object value;

			if (!marker.exists()) {
				return null;// If the marker was deleted during the update drop it
			}

			try {
				value = marker.getAttribute(attribute);
			} catch (CoreException e) {
				Policy.handle(e);
				return null;
			}

			if (value != null && attributeValue.equals(value.toString())) {
				return groupingEntry;
			}
			return null;
		}
	}

	static class EntryMapping {
		MarkerGroupingEntry groupingEntry;

		/**
		 * Create an entry mapping for the receiver.
		 */
		EntryMapping(MarkerGroupingEntry entry) {
			groupingEntry = entry;
		}

		/**
		 * Return whether or not the receiver tests attributes.
		 *
		 * @return boolean
		 */
		public boolean hasAttributes() {
			return false;
		}

		/**
		 * Test the attribute of the marker to find a grouping.
		 *
		 * @param marker unused but used in overrides
		 *
		 * @return MarkerGroupingEntry or <code>null</code> if there is not entry.
		 */
		public MarkerGroupingEntry testAttribute(IMarker marker) {
			return null;
		}
	}

	class FieldGroup implements IField {

		MarkerGroup markerGroup;

		private boolean showing;

		FieldGroup(MarkerGroup group) {
			markerGroup = group;
		}

		@Override
		public int compare(Object obj1, Object obj2) {
			MarkerGroupingEntry entry1 = getMapping(((MarkerNode) obj1).getConcreteRepresentative());
			MarkerGroupingEntry entry2 = getMapping(((MarkerNode) obj2).getConcreteRepresentative());
			return entry2.getPriority() - entry1.getPriority();
		}

		@Override
		public Image getColumnHeaderImage() {
			return null;
		}

		@Override
		public String getColumnHeaderText() {
			return markerGroup.getTitle();
		}

		@Override
		public int getDefaultDirection() {
			return TableComparator.ASCENDING;
		}

		@Override
		public String getDescription() {
			return markerGroup.getTitle();
		}

		@Override
		public Image getDescriptionImage() {
			return null;
		}

		@Override
		public Image getImage(Object obj) {
			return null;
		}

		@Override
		public int getPreferredWidth() {
			return 75;
		}

		@Override
		public String getValue(Object obj) {
			MarkerNode node = (MarkerNode) obj;

			if (node.isConcrete()) {
				MarkerGroupingEntry groupingEntry = markerGroup.getMapping((ConcreteMarker) node);
				return groupingEntry.getLabel();
			}
			return node.getDescription();
		}

		@Override
		public boolean isShowing() {
			return this.showing;
		}

		@Override
		public void setShowing(boolean showing) {
			this.showing = showing;
		}

	}

	/**
	 * GroupMarkerField is the MarkerField used for MarkerGroupungs
	 *
	 * @since 3.4
	 */
	class GroupMarkerField extends MarkerField {

		GroupMarkerField() {
		}

		@Override
		public String getValue(MarkerItem item) {
			return MarkerSupportInternalUtilities.getGroupValue(MarkerGroup.this, item);
		}

		@Override
		public int compare(MarkerItem item1, MarkerItem item2) {
			// Elements with markers to the top are higher values
			if (item1.getMarker() == null) {
				if (item2.getMarker() == null) {
					return 0;
				}
				return 1;
			}

			// Elements with markers to the top are higher values
			if (item2.getMarker() == null) {
				return -1;
			}

			try {
				MarkerGroupingEntry groupValue2 = findGroupValue(item2.getMarker().getType(), item2.getMarker());
				MarkerGroupingEntry groupValue1 = findGroupValue(item1.getMarker().getType(), item1.getMarker());
				return (groupValue2.getPriority() - groupValue1.getPriority());

			} catch (CoreException exception) {
				Policy.handle(exception);
				return 0;
			}
		}

		@Override
		public String getName() {
			return MarkerGroup.this.getTitle();
		}

	}

	private static MarkerGroupingEntry undefinedEntry = new MarkerGroupingEntry(
			MarkerMessages.FieldCategory_Uncategorized);

	protected IField field;

	protected MarkerField markerField;

	private Map<String, Set<EntryMapping>> typesToMappings = new LinkedHashMap<>();

	private IConfigurationElement configurationElement;

	private String id;

	/**
	 * Create a new instance of the receiver called name with id identifier.
	 */
	protected MarkerGroup(IConfigurationElement element) {
		configurationElement = element;
		if (element != null) {
			id = element.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID);
		}
		createFields();
		processEntries();
	}

	/**
	 * Process the markerContentEntries for the receiver.
	 */
	private void processEntries() {
		if(configurationElement == null) {
			return;
		}

		IConfigurationElement[] markerEntryElements = configurationElement
				.getChildren(MarkerSupportRegistry.MARKER_GROUPING_ENTRY);

		IConfigurationElement[] attributeGroupingElements = configurationElement
				.getChildren(MarkerSupportRegistry.MARKER_ATTRIBUTE_GROUPING);

		Map<String, MarkerGroupingEntry> idsToEntries = new HashMap<>();
		for (IConfigurationElement markerEntryElement : markerEntryElements) {
			MarkerGroupingEntry entry = new MarkerGroupingEntry(markerEntryElement);
			entry.setGroup(this);
			idsToEntries.put(entry.getId(), entry);
		}

		for (IConfigurationElement attributeGroupingElement : attributeGroupingElements) {
			AttributeMarkerGrouping attributeGrouping = new AttributeMarkerGrouping(attributeGroupingElement);

			String defaultEntryId = attributeGrouping.getDefaultGroupingEntry();
			if (defaultEntryId != null) {
				MarkerGroupingEntry entry = idsToEntries.get(defaultEntryId);
				if (entry != null) {
					entry.setAsDefault(attributeGrouping.getMarkerType());
				} else {
					IDEWorkbenchPlugin.log(NLS.bind(
							"Reference to invalid markerGroupingEntry {0}",//$NON-NLS-1$
							defaultEntryId));
				}
			}
			IConfigurationElement[] mappings = attributeGrouping.getElement()
					.getChildren(MarkerSupportRegistry.ATTRIBUTE_MAPPING);

			for (IConfigurationElement mapping : mappings) {
				String entryId = mapping.getAttribute(MarkerSupportRegistry.MARKER_GROUPING_ENTRY);

				MarkerGroupingEntry entry = idsToEntries.get(entryId);
				if (entry != null) {
					entry.getMarkerGroup().mapAttribute(
							attributeGrouping,
							entry,
							mapping.getAttribute(MarkerSupportRegistry.VALUE));
				} else {
					IDEWorkbenchPlugin.log(NLS.bind(
							"Reference to invaild markerGroupingEntry {0}", //$NON-NLS-1$
							defaultEntryId));
				}
			}
		}
	}

	/**
	 * Create the fields for the marker views.
	 */
	protected void createFields() {
		field = new FieldGroup(this);
		markerField = new GroupMarkerField();
	}

	/**
	 * Add the entry for the markerType.
	 */
	private void addEntry(String markerType, EntryMapping entry) {
		for (MarkerType type : getMarkerTypes(markerType)) {
			Set<EntryMapping> entries = typesToMappings.get(markerType);
			if (entries == null) {
				entries = new LinkedHashSet<>();
			}
			entries.add(entry);
			typesToMappings.put(type.getId(), entries);
		}
	}

	/**
	 * Find the group value. If it cannot be found in an attribute mapping then
	 * return null;
	 *
	 * @return String or <code>null</code>
	 */
	private MarkerGroupingEntry findGroupValue(ConcreteMarker concreteMarker) {
		String type = concreteMarker.getType();
		IMarker marker = concreteMarker.getMarker();
		return findGroupValue(type, marker);
	}

	/**
	 * Find the group for the marker of the specified marker type.
	 *
	 * @return MarkerGroupingEntry
	 */
	public MarkerGroupingEntry findGroupValue(String type, IMarker marker) {
		Collection<EntryMapping> collection = typesToMappings.get(type);
		if (collection != null) {
			EntryMapping defaultMapping = null;
			Iterator<EntryMapping> mappings = collection.iterator();
			while (mappings.hasNext()) {
				EntryMapping mapping = mappings.next();
				if (mapping.hasAttributes()) {
					MarkerGroupingEntry entry = mapping.testAttribute(marker);
					if (entry != null) {
						return entry;
					}
				} else {
					// If it has no attributes it is our default
					defaultMapping = mapping;
				}
			}
			if (defaultMapping != null) {
				return defaultMapping.groupingEntry;
			}
		}
		return undefinedEntry;
	}

	/**
	 * Return the field for the receiver.
	 *
	 * @return {@link IField}
	 */
	public IField getField() {
		return field;
	}

	/**
	 * Return the id of the receiver.
	 *
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get the attribute mapping for the marker
	 *
	 * @return MarkerGroupingEntry
	 */
	private MarkerGroupingEntry getMapping(ConcreteMarker marker) {
		if (marker.getGroup() == null) {
			marker.setGroup(findGroupValue(marker));
		}
		return (MarkerGroupingEntry) marker.getGroup();
	}

	/**
	 * Return the markerField for the receiver.
	 *
	 * @return MarkerField
	 */
	public MarkerField getMarkerField() {
		return markerField;
	}

	/**
	 * Return the marker types that match and are subtypes of markerType.
	 *
	 * @return MarkerType[]
	 */
	private MarkerType[] getMarkerTypes(String markerType) {
		MarkerTypesModel model = MarkerTypesModel.getInstance();
		Collection<MarkerType> types = new LinkedHashSet<>();

		MarkerType type = model.getType(markerType);
		if (type != null) {
			types.add(type);
			types.addAll(Arrays.asList(type.getAllSubTypes()));
		}

		if (types.isEmpty()) {
			return new MarkerType[0];
		}

		MarkerType[] typesArray = new MarkerType[types.size()];
		types.toArray(typesArray);
		return typesArray;
	}

	/**
	 * Return the title for the receiver.
	 *
	 * @return String
	 */
	public String getTitle() {
		return configurationElement.getAttribute(MarkerSupportRegistry.LABEL);
	}

	/**
	 * Add an attributeMapping for the markerType.
	 */
	public void mapAttribute(AttributeMarkerGrouping attributeGrouping,
			MarkerGroupingEntry entry, String attributeValue) {
		addEntry(attributeGrouping.getMarkerType(), new AttributeMapping(entry,
				attributeGrouping.getAttribute(), attributeValue,
				attributeGrouping));
		attributeGrouping.addGroup(this);
	}

	/**
	 * Remove the entry from all of the entries in the receiver.
	 */
	public void remove(MarkerGroupingEntry entry) {
		Iterator<Set<EntryMapping>> entries = typesToMappings.values().iterator();
		List<EntryMapping> removeCollection = new ArrayList<>();
		while (entries.hasNext()) {
			Set<EntryMapping> mappings = entries.next();
			Iterator<EntryMapping> mappingsIterator = mappings.iterator();
			while (mappingsIterator.hasNext()) {
				EntryMapping next = mappingsIterator.next();
				if (next.groupingEntry.equals(entry)) {
					removeCollection.add(next);
				}

			}
			mappings.removeAll(removeCollection);
			removeCollection.clear();
		}
	}

	/**
	 * Set entry and the default entry for the supplied markerType.
	 */

	public void setAsDefault(String markerType, MarkerGroupingEntry entry) {
		addEntry(markerType, new EntryMapping(entry));
	}

	/**
	 * Unmap the attributeMarkerGrouping from the receiver.
	 */
	public void unmap(AttributeMarkerGrouping attributeMarkerGrouping) {
		String type = attributeMarkerGrouping.getMarkerType();
		Collection<EntryMapping> removed = new ArrayList<>();
		Collection<EntryMapping> entries = typesToMappings.get(type);
		Iterator<EntryMapping> mappings = entries.iterator();
		while (mappings.hasNext()) {
			EntryMapping mapping = mappings.next();
			if (mapping instanceof AttributeMapping
					&& (((AttributeMapping) mapping).grouping == attributeMarkerGrouping)) {
				removed.add(mapping);
			}
		}
		entries.removeAll(removed);
		if (entries.isEmpty()) {
			typesToMappings.remove(type);
		}
	}

	/**
	 * Returns the comparator that can be used for
	 * sorting the MarkerGroupingEntry(s) in the group.
	 * @return Comparator
	 */
	public Comparator<MarkerGroupingEntry> getEntriesComparator() {
		return (o1, o2) -> -(o1.getPriority() - o2.getPriority());
	}
}

/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

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
 * 
 */
public class MarkerGroup {

	/**
	 * Create a new MarkerGroup from element.
	 * 
	 * @param element
	 * @return MarkerGroup
	 */
	public static MarkerGroup createMarkerGroup(IConfigurationElement element) {

		if (element.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID).equals(
				Util.TYPE_MARKER_GROUPING_ID))
			return new TypeMarkerGroup(element.getAttribute(MarkerSupportRegistry.LABEL));
		return new MarkerGroup(element);
	}

	class AttributeMapping extends EntryMapping {

		String attribute;

		String attributeValue;

		AttributeMarkerGrouping grouping;

		/**
		 * Create a mapping for an attribute with name attributeName and value
		 * value to the supplied entry.
		 * 
		 * @param entry
		 * @param attributeName
		 * @param value
		 */
		AttributeMapping(MarkerGroupingEntry entry, String attributeName,
				String value, AttributeMarkerGrouping grouping) {
			super(entry);
			attribute = attributeName;
			attributeValue = value;
			this.grouping = grouping;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.FieldMarkerGroup.EntryMapping#hasAttributes()
		 */
		public boolean hasAttributes() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.FieldMarkerGroup.EntryMapping#testAttribute(org.eclipse.ui.views.markers.internal.ConcreteMarker)
		 */
		public MarkerGroupingEntry testAttribute(IMarker marker) {
			Object value;

			if (!marker.exists())
				return null;// If the marker was deleted during the update drop
			// it

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

	class EntryMapping {
		MarkerGroupingEntry groupingEntry;

		/**
		 * Create an entry mapping for the receiver.
		 * 
		 * @param entry
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
		 * @param marker
		 * @return MarkerGroupingEntry or <code>null</code> if there is not
		 *         entry.
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object,
		 *      java.lang.Object)
		 */
		public int compare(Object obj1, Object obj2) {

			MarkerGroupingEntry entry1 = getMapping(((MarkerNode) obj1)
					.getConcreteRepresentative());
			MarkerGroupingEntry entry2 = getMapping(((MarkerNode) obj2)
					.getConcreteRepresentative());
			return entry2.getPriority() - entry1.getPriority();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderImage()
		 */
		public Image getColumnHeaderImage() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderText()
		 */
		public String getColumnHeaderText() {
			return markerGroup.getTitle();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.IField#getDefaultDirection()
		 */
		public int getDefaultDirection() {
			return TableComparator.ASCENDING;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.IField#getDescription()
		 */
		public String getDescription() {
			return markerGroup.getTitle();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.IField#getDescriptionImage()
		 */
		public Image getDescriptionImage() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
		 */
		public Image getImage(Object obj) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.IField#getPreferredWidth()
		 */
		public int getPreferredWidth() {
			return 75;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.IField#getValue(java.lang.Object)
		 */
		public String getValue(Object obj) {
			MarkerNode node = (MarkerNode) obj;

			if (node.isConcrete()) {
				MarkerGroupingEntry groupingEntry = markerGroup
						.getMapping((ConcreteMarker) node);
				return groupingEntry.getLabel();
			}
			return node.getDescription();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.IField#isShowing()
		 */
		public boolean isShowing() {
			return this.showing;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.IField#setShowing(boolean)
		 */
		public void setShowing(boolean showing) {
			this.showing = showing;

		}

	}

	/**
	 * GroupMarkerField is the MarkerField used for MarkerGroupungs
	 * 
	 * @since 3.4
	 * 
	 */
	class GroupMarkerField extends MarkerField {

		GroupMarkerField() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem)
		 */
		public String getValue(MarkerItem item) {

			return MarkerSupportInternalUtilities.getGroupValue(MarkerGroup.this, item);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#compare(org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem,
		 *      org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem)
		 */
		public int compare(MarkerItem item1, MarkerItem item2) {
			// Elements with markers to the top are higher values
			if (item1.getMarker() == null) {
				if (item2.getMarker() == null)
					return 0;
				return 1;
			}

			// Elements with markers to the top are higher values
			if (item2.getMarker() == null)
				return -1;

			try {
				return (findGroupValue(item2.getMarker().getType(),
						item2.getMarker()).getPriority() - findGroupValue(
						item1.getMarker().getType(), item1.getMarker())
						.getPriority());

			} catch (CoreException exception) {
				Policy.handle(exception);
				return 0;
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#getName()
		 */
		public String getName() {
			return MarkerGroup.this.getTitle();
		}

	}

	private static MarkerGroupingEntry undefinedEntry = new MarkerGroupingEntry(
			MarkerMessages.FieldCategory_Uncategorized);

	protected IField field;

	protected MarkerField markerField;

	private Map typesToMappings = new HashMap();

	private IConfigurationElement configurationElement;

	private String id;

	/**
	 * Create a new instance of the receiver called name with id identifier.
	 * 
	 * @param element
	 */
	protected MarkerGroup(IConfigurationElement element) {
		configurationElement = element;
		if (element != null) // Is this an internal one?
			id = element.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID);
		createFields();
		processEntries();
	}

	/**
	 * Process the markerContentEntries for the reciever.
	 */
	private void processEntries() {
		
		if(configurationElement == null)
			return;
		
		IConfigurationElement[] markerEntryElements = configurationElement
				.getChildren(MarkerSupportRegistry.MARKER_GROUPING_ENTRY);

		IConfigurationElement[] attributeGroupingElements = configurationElement
				.getChildren(MarkerSupportRegistry.MARKER_ATTRIBUTE_GROUPING);

		Map idsToEntries = new HashMap();
		for (int i = 0; i < markerEntryElements.length; i++) {
			MarkerGroupingEntry entry = new MarkerGroupingEntry(
					markerEntryElements[i]);
			entry.setGroup(this);
			idsToEntries.put(entry.getId(), entry);
		}

		for (int i = 0; i < attributeGroupingElements.length; i++) {
			AttributeMarkerGrouping attributeGrouping = new AttributeMarkerGrouping(
					attributeGroupingElements[i]);

			String defaultEntryId = attributeGrouping.getDefaultGroupingEntry();
			if (defaultEntryId != null) {
				if (idsToEntries.containsKey(defaultEntryId)) {
					MarkerGroupingEntry entry = (MarkerGroupingEntry) idsToEntries
							.get(defaultEntryId);
					entry.setAsDefault(attributeGrouping.getMarkerType());
				} else {
					IDEWorkbenchPlugin.log(NLS.bind(
							"Reference to invalid markerGroupingEntry {0}",//$NON-NLS-1$
							defaultEntryId));
				}
			}
			IConfigurationElement[] mappings = attributeGrouping.getElement()
					.getChildren(MarkerSupportRegistry.ATTRIBUTE_MAPPING);

			for (int mappingIndex = 0; mappingIndex < mappings.length; mappingIndex++) {
				String entryId = mappings[mappingIndex]
						.getAttribute(MarkerSupportRegistry.MARKER_GROUPING_ENTRY);

				if (idsToEntries.containsKey(entryId)) {
					MarkerGroupingEntry entry = (MarkerGroupingEntry) idsToEntries
							.get(entryId);
					entry.getMarkerGroup().mapAttribute(
							attributeGrouping,
							entry,
							mappings[mappingIndex]
									.getAttribute(MarkerSupportRegistry.VALUE));
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
	 * 
	 * @param markerType
	 * @param entry
	 */
	private void addEntry(String markerType, EntryMapping entry) {

		MarkerType[] allDerived = getMarkerTypes(markerType);

		for (int i = 0; i < allDerived.length; i++) {
			Collection entries = new HashSet();
			MarkerType type = allDerived[i];
			if (typesToMappings.containsKey(type.getId())) {
				entries = (Collection) typesToMappings.get(markerType);
			} else {
				entries = new HashSet();
			}

			entries.add(entry);
			typesToMappings.put(type.getId(), entries);
		}

	}

	/**
	 * Find the group value. If it cannot be found in an attribute mapping then
	 * return null;
	 * 
	 * @param concreteMarker
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
	 * @param type
	 * @param marker
	 * @return MarkerGroupingEntry
	 */
	public MarkerGroupingEntry findGroupValue(String type, IMarker marker) {
		if (typesToMappings.containsKey(type)) {
			EntryMapping defaultMapping = null;
			Iterator mappings = ((Collection) typesToMappings.get(type))
					.iterator();
			while (mappings.hasNext()) {
				EntryMapping mapping = (EntryMapping) mappings.next();
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
	 * @param marker
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
	 * @param markerType
	 * @return MarkerType[]
	 */
	private MarkerType[] getMarkerTypes(String markerType) {
		MarkerTypesModel model = MarkerTypesModel.getInstance();
		Collection types = new HashSet();

		MarkerType type = model.getType(markerType);
		if (type != null) {
			types.add(type);
			MarkerType[] subs = type.getAllSubTypes();
			for (int j = 0; j < subs.length; j++) {
				types.add(subs[j]);
			}
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
	 * 
	 * @param attributeGrouping
	 * @param entry
	 * @param attributeValue
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
	 * 
	 * @param entry
	 */
	public void remove(MarkerGroupingEntry entry) {
		Iterator entries = typesToMappings.values().iterator();
		Collection removeCollection = new ArrayList();
		while (entries.hasNext()) {
			Collection mappings = (Collection) entries.next();
			Iterator mappingsIterator = mappings.iterator();
			while (mappingsIterator.hasNext()) {
				EntryMapping next = (EntryMapping) mappingsIterator.next();
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
	 * 
	 * @param markerType
	 * @param entry
	 */

	public void setAsDefault(String markerType, MarkerGroupingEntry entry) {
		addEntry(markerType, new EntryMapping(entry));

	}

	/**
	 * Unmap the attributeMarkerGrouping from the receiver.
	 * 
	 * @param attributeMarkerGrouping
	 */
	public void unmap(AttributeMarkerGrouping attributeMarkerGrouping) {
		String type = attributeMarkerGrouping.getMarkerType();
		Collection removed = new ArrayList();
		Collection entries = (Collection) typesToMappings.get(type);
		Iterator mappings = entries.iterator();
		while (mappings.hasNext()) {
			Object mapping = mappings.next();
			if (mapping instanceof AttributeMapping
					&& (((AttributeMapping) mapping).grouping == attributeMarkerGrouping)) {
				removed.add(mapping);
			}
		}
		entries.removeAll(removed);
		if (entries.size() == 0)
			typesToMappings.remove(type);

	}
	/**
	 * Returns the comparator that can be used for 
	 * sorting the MarkerGroupingEntry(s) in the group.
	 * @return Comparator
	 */
	public Comparator getEntriesComparator(){
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				return -(((MarkerGroupingEntry) o1).getPriority()
						- ((MarkerGroupingEntry) o2).getPriority());
			}
		};
	}
}

/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerField;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerSupportConstants;

/**
 * @since 3.2
 * 
 */
public class MarkerGroup {

	class AttributeMapping extends EntryMapping {

		String attribute;

		String attributeValue;

		/**
		 * Create a mapping for an attribute with name attributeName and value
		 * value to the supplied entry.
		 * 
		 * @param entry
		 * @param attributeName
		 * @param value
		 */
		AttributeMapping(MarkerGroupingEntry entry, String attributeName,
				String value) {
			super(entry);
			attribute = attributeName;
			attributeValue = value;
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
				Util.log(e);
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

		MarkerGroup markerGroup;

		GroupMarkerField(MarkerGroup group) {
			markerGroup = group;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem)
		 */
		public String getValue(MarkerItem item) {

			if (item.getMarker() != null) {

				try {
					MarkerGroupingEntry groupingEntry = findGroupValue(item
							.getMarker().getType(), item.getMarker());
					return groupingEntry.getLabel();
				} catch (CoreException exception) {
					return Util.EMPTY_STRING;
				}
			}
			return item.getDescription();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#getColumnHeaderText()
		 */
		public String getColumnHeaderText() {
			return markerGroup.getTitle();
		}

	}

	private static final String PROBLEMS_CONTENTS = "org.eclipse.ui.ide.problemsGenerator"; //$NON-NLS-1$

	private static MarkerGroupingEntry undefinedEntry = new MarkerGroupingEntry(
			MarkerMessages.FieldCategory_Uncategorized, null, 0);

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
	public MarkerGroup(IConfigurationElement element) {
		configurationElement = element;
		if (element != null) //Is this an internal one?
			id = element.getAttribute(MarkerSupportConstants.ATTRIBUTE_ID);
		createFields();
	}

	/**
	 * Create the fields for the marker views.
	 */
	protected void createFields() {
		field = new FieldGroup(this);
		markerField = new GroupMarkerField(this);
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
	 * @param markerType
	 * @param attribute
	 * @param attributeValue
	 * @param entry
	 */
	public void mapAttribute(String markerType, String attribute,
			String attributeValue, MarkerGroupingEntry entry) {
		addEntry(markerType, new AttributeMapping(entry, attribute,
				attributeValue));

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
	 * Return whether or not there is a markerSupportReference for the
	 * markerContentGenerator keyed by id.
	 * 
	 * @param id
	 * @return boolean
	 */
	public boolean isGroupingFor(String id) {

		IConfigurationElement[] references = configurationElement
				.getChildren(MarkerSupportRegistry.MARKER_SUPPORT_REFERENCE);

		// Groupings that do not refer to content providers are assumed to apply
		// to problems
		if (references.length == 0 && id.equals(PROBLEMS_CONTENTS))
			return true;

		for (int i = 0; i < references.length; i++) {

			// Does the id match?
			if (references[i].getAttribute(MarkerSupportConstants.ATTRIBUTE_ID)
					.equals(id)// Is it the right type of reference?
					&& references[i].getAttribute(
							MarkerSupportConstants.ATTRIBUTE_TYPE).equals(
							MarkerSupportRegistry.MARKER_CONTENT_GENERATOR))
				return true;
		}
		return false;
	}
}

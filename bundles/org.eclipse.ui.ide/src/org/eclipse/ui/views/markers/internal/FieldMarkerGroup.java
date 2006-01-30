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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * @since 3.2
 * 
 */
public class FieldMarkerGroup implements IField {

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
		public MarkerGroupingEntry testAttribute(ConcreteMarker marker) {
			return null;
		}
	}

	private static MarkerGroupingEntry undefinedEntry = new MarkerGroupingEntry(
			MarkerMessages.FieldCategory_Uncategorized, null, 0);

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
		public MarkerGroupingEntry testAttribute(ConcreteMarker marker) {
			Object value;
			try {
				value = marker.getMarker().getAttribute(attribute);
			} catch (CoreException e) {
				IDEWorkbenchPlugin.log(e.getLocalizedMessage(), e.getStatus());
				return null;
			}
			
			if (value != null && attributeValue.equals(value.toString()))
				return groupingEntry;
			return null;
		}
	}

	private String title;

	private String id;

	private Map typesToMappings = new HashMap();

	private boolean showing;

	/**
	 * Create a new instance of the receiver called name with id identifier.
	 * 
	 * @param name
	 * @param identifier
	 */
	public FieldMarkerGroup(String name, String identifier) {
		title = name;
		id = identifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescription()
	 */
	public String getDescription() {
		return title;
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
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return title;
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
	 * @see org.eclipse.ui.views.markers.internal.IField#getValue(java.lang.Object)
	 */
	public String getValue(Object obj) {
		MarkerNode node = (MarkerNode) obj;

		if (node.isConcrete()) {
			MarkerGroupingEntry groupingEntry = getMapping((ConcreteMarker) node);
			return groupingEntry.getLabel();
		}
		return node.getDescription();
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
	 * Find the group value. If it cannot be found in an attribute mapping then
	 * return null;
	 * 
	 * @param marker
	 * @return String or <code>null</code>
	 */
	private MarkerGroupingEntry findGroupValue(ConcreteMarker marker) {

		if (typesToMappings.containsKey(marker.getType())) {
			EntryMapping defaultMapping = null;
			Iterator mappings = ((Collection) typesToMappings.get(marker
					.getType())).iterator();
			while (mappings.hasNext()) {
				EntryMapping mapping = (EntryMapping) mappings.next();
				if (mapping.hasAttributes()) {
					MarkerGroupingEntry entry = mapping.testAttribute(marker);
					if (entry != null)
						return entry;
				} else
					// If it has no attributes it is our default
					defaultMapping = mapping;
			}
			if (defaultMapping != null)
				return defaultMapping.groupingEntry;

		}

		return undefinedEntry;

	} /*
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
	 * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object,
	 *      java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2) {

		MarkerGroupingEntry entry1 = getMapping(((MarkerNode) obj1)
				.getConcreteRepresentative());
		MarkerGroupingEntry entry2 = getMapping(((MarkerNode) obj2)
				.getConcreteRepresentative());
		return entry1.getPriority() - entry2.getPriority();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getDefaultDirection()
	 */
	public int getDefaultDirection() {
		return TableSorter.ASCENDING;
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
	 * @see org.eclipse.ui.views.markers.internal.IField#isShowing()
	 */
	public boolean isShowing() {
		return showing;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#setShowing(boolean)
	 */
	public void setShowing(boolean showing) {
		this.showing = showing;

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
			if (typesToMappings.containsKey(type.getId()))
				entries = (Collection) typesToMappings.get(markerType);
			else
				entries = new HashSet();

			entries.add(entry);
			typesToMappings.put(type.getId(), entries);
		}

	}

	/**
	 * Return the marker types that match and are subtypes of markerType.
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

		if (types.isEmpty())
			return new MarkerType[0];

		MarkerType[] typesArray = new MarkerType[types.size()];
		types.toArray(typesArray);
		return typesArray;
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
	 * Return the id of the receiver.
	 * 
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Remove the entry from all of the entries in the receiver.
	 * @param entry
	 */
	public void remove(MarkerGroupingEntry entry) {
		Iterator entries = typesToMappings.values().iterator();
		Collection removeCollection = new ArrayList();
		while(entries.hasNext()){
			Collection mappings = (Collection) entries.next();
			Iterator mappingsIterator = mappings.iterator();
			while(mappingsIterator.hasNext()){
				EntryMapping next = (EntryMapping) mappingsIterator.next();
				if(next.groupingEntry.equals(entry)){
					removeCollection.add(next);
				}
					
			}
			mappings.removeAll(removeCollection);
			removeCollection.clear();
		}
		
	}
}

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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

/**
 * @since 3.2
 * 
 */
public class FieldMarkerGroup implements IField {

	class AttributeMapping {

		// The priority in sort order. The lower the priority
		// the higher in the sort order
		int priority;

		// The value to display
		String displayString;

		/**
		 * Create a mapping definition to display label with sortIndex of
		 * sortOrder.
		 * 
		 * @param label
		 * @param sortPriority
		 */
		AttributeMapping(String label, int sortPriority) {
			priority = sortPriority;
			displayString = label;
		}
	}

	private AttributeMapping undefinedMapping = new AttributeMapping(
			MarkerMessages.FieldCategory_Uncategorized, 100000);

	private String title;

	private String markerAttribute;

	private Map valueMapping = new HashMap();

	private Collection types;

	private boolean showing;

	/**
	 * Create a new instance of the receiver called name. attribute is the
	 * attribute to be checked and whose value is looked up in providers. Apply
	 * to markerTypes[].
	 * 
	 * @param name
	 * @param attribute
	 * @param markerTypes
	 */
	public FieldMarkerGroup(String name, String attribute, String[] markerTypes) {
		title = name;
		markerAttribute = attribute;
		types = new HashSet();

		MarkerTypesModel model = MarkerTypesModel.getInstance();
		for (int i = 0; i < markerTypes.length; i++) {
			MarkerType type = model.getType(markerTypes[i]);
			if (type != null) {
				types.add(markerTypes[i]);
				MarkerType[] subs = type.getAllSubTypes();
				for (int j = 0; j < subs.length; j++) {
					types.add(subs[j].getId());
				}
			}
		}
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
			AttributeMapping mapping = getMapping((ConcreteMarker) node);
			return mapping.displayString;
		}
		return node.getDescription();
	}

	/**
	 * Get the attribute mapping for the marker
	 * 
	 * @param marker
	 * @return AttributeMapping
	 */
	private AttributeMapping getMapping(ConcreteMarker marker) {

		if (marker.getGroup() == null) {
			AttributeMapping value = findGroupValue(marker);
			if (value == null)
				marker.setGroup(undefinedMapping);
			else
				marker.setGroup(value);
		}
		return (AttributeMapping) marker.getGroup();
	}

	/**
	 * Find the group value. If it cannot be found in an attribute mapping then
	 * return null;
	 * 
	 * @param marker
	 * @return String or <code>null</code>
	 */
	private AttributeMapping findGroupValue(ConcreteMarker marker) {

		if (types.contains(marker.getType())) {
			String value;
			try {
				Object attributeValue = marker.getMarker().getAttribute(markerAttribute);
				if(attributeValue == null)
					return null;
				value = attributeValue.toString();
			} catch (CoreException e) {
				return null;
			}
			if (valueMapping.containsKey(value))
				return (AttributeMapping) valueMapping.get(value);
		}
		return null;

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

		AttributeMapping mapping1 = getMapping(((MarkerNode) obj1)
				.getConcreteRepresentative());
		AttributeMapping mapping2 = getMapping(((MarkerNode) obj2)
				.getConcreteRepresentative());
		return mapping1.priority - mapping2.priority;

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
	 * Add a mapping for the attribute value value to the string and index.
	 * 
	 * @param value
	 * @param displayString
	 * @param index
	 */
	public void addMapping(String value, String displayString, int index) {
		valueMapping.put(value, new AttributeMapping(displayString, index));

	}
}

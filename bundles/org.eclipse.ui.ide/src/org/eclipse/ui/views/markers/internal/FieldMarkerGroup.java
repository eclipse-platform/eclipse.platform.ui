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
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

/**
 * @since 3.2
 * 
 */
public class FieldMarkerGroup implements IField {

	private String title;

	private String markerAttribute;

	private Map valueMapping;

	private Collection types;

	private boolean showing;

	/**
	 * Create a new instance of the receiver called name. attribute is the
	 * attribute to be checked and whose value is looked up in providers. Apply
	 * to markerTypes[].
	 * 
	 * @param name
	 * @param attribute
	 * @param providers
	 * @param markerTypes
	 */
	public FieldMarkerGroup(String name, String attribute, Map providers,
			String[] markerTypes) {
		title = name;
		markerAttribute = attribute;
		valueMapping = providers;
		types = new HashSet();
		for (int i = 0; i < markerTypes.length; i++) {
			types.add(markerTypes[i]);
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
		if (!(obj instanceof ConcreteMarker))
			return Util.EMPTY_STRING;

		ConcreteMarker marker = (ConcreteMarker) obj;
		if (marker.getGroup() == null) {
			String value = findGroupValue(marker);
			if (value == null)
				marker.setGroup(MarkerMessages.FieldCategory_Uncategorized);
			else
				marker.setGroup(value);
		}
		return marker.getGroup();
	}

	/**
	 * Find the group value. If it cannot be found in an 
	 * attribute mapping then return null;
	 * @param marker
	 * @return String or <code>null</code> 
	 */
	private String findGroupValue(ConcreteMarker marker) {

		if (types.contains(marker.getType())) {
			String value;
			try {
				value = (String) marker.getMarker().getAttribute(
						markerAttribute);
			} catch (CoreException e) {
				return null;
			}
			if (valueMapping.containsKey(value))
				return (String) valueMapping.get(value);
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
		if (obj1 == null || obj2 == null || !(obj1 instanceof ConcreteMarker)
				|| !(obj2 instanceof ConcreteMarker)) {
			return 0;
		}

		ConcreteMarker marker1 = (ConcreteMarker) obj1;
		ConcreteMarker marker2 = (ConcreteMarker) obj2;

		return getValue(marker1).compareTo(getValue(marker2));
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
}

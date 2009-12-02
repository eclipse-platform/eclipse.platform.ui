/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.views.markers.MarkerViewUtil;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;

import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;

/**
 * The MarkerEntry is the class that wrappers an {@link IMarker} for display in
 * an {@link ExtendedMarkersView}.
 * 
 * @since 3.4
 * 
 */
class MarkerEntry extends MarkerSupportItem implements IAdaptable {
	static {
		Platform.getAdapterManager().registerAdapters(new IAdapterFactory() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang
			 * .Object, java.lang.Class)
			 */
			public Object getAdapter(Object adaptableObject, Class adapterType) {
				if (adapterType == IMarker.class
						&& adaptableObject instanceof MarkerEntry)
					return ((MarkerEntry) adaptableObject).getMarker();

				return null;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
			 */
			public Class[] getAdapterList() {
				return new Class[] { IMarker.class };
			}
		}, MarkerEntry.class);
	}
	// The key for the string we built for display
	private static final Object LOCATION_STRING = "LOCATION_STRING"; //$NON-NLS-1$
	private MarkerCategory category;
	private Map cache = null;
	
	/**
	 * Set the MarkerEntry to be stale, if discovered at any point of time
	 * of its use.This will greatly speed up a lot of parts of the view.
	 * @since 3.6
	 */
	private boolean stale;
	/**
	 * Important:
	 * access to these fields must be via methods, they must be in sync and their
	 * values should reflect correctly the state of the other
	 */
	private IMarker marker;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param marker
	 */
	public MarkerEntry(IMarker marker) {
		this.marker = marker;
		stale = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IMarker.class))
			return marker;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerItem#getAttributeValue(java.lang.String,
	 *      boolean)
	 */
	public boolean getAttributeValue(String attribute, boolean defaultValue) {
		Object value = getAttributeValue(attribute);
		if (value == null)
			return defaultValue;
		return ((Boolean) value).booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.MarkerItem#getAttributeValue(java.lang.String,
	 *      int)
	 */
	public int getAttributeValue(String attribute, int defaultValue) {

		Object value = getAttributeValue(attribute);
		if (value == null)
			return defaultValue;
		return ((Integer) value).intValue();

	}

	/**
	 * Return the Object that is the marker value for attribute. Return null if
	 * it is not found.
	 * 
	 * @param attribute
	 * @return Object or <code>null</code>
	 */
	Object getAttributeValue(String attribute) {
		Object value = getCache().get(attribute);
		if(value == null) {
			if(stale){
				return value;
			}
			try {
				value = marker.getAttribute(attribute);
			} catch (CoreException e) {
				checkIfMarkerStale() ;
				value = null;
			}
			if(value != null) {
				getCache().put(attribute, value);
			}
		}
		if (value instanceof CollationKey)
			return ((CollationKey) value).getSourceString();
		return value;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.views.markers.MarkerSupportItem#getAttributeValue(java.lang.String, java.lang.String)
	 */
	public String getAttributeValue(String attribute, String defaultValue) {

		Object value = getAttributeValue(attribute);
		if (value == null)
			return defaultValue;
		// The following toString() is a no-op for string attribute
		// values (which we expect!), but safeguards against clients
		// who used non-String objects (e.g. Integer) as attribute values,
		// see bug 218249. 
		return value.toString();
	}

	/**
	 * Get the category of the receiver.
	 * 
	 * @return {@link MarkerCategory}
	 */
	MarkerCategory getCategory() {
		return category;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerSupportItem#getChildren()
	 */
	MarkerSupportItem[] getChildren() {
		return MarkerSupportInternalUtilities.EMPTY_MARKER_ITEM_ARRAY;
	}

	/**
	 * Get the CollationKey for the string attribute.
	 * 
	 * @param attribute
	 * @param defaultValue
	 *            the defaultValue if the value is not set
	 * @return CollationKey
	 */
	CollationKey getCollationKey(String attribute, String defaultValue) {
		String attributeValue;
		Object value = getCache().get(attribute);
		if (value != null) {
			// Only return a collation key otherwise 
			//use the value to generate it
			if (value instanceof CollationKey)
				return (CollationKey) value;

			attributeValue = value.toString();
		} else {
			attributeValue = getAttributeValue(attribute, defaultValue);
		}

		if (attributeValue.length() == 0)
			return MarkerSupportInternalUtilities.EMPTY_COLLATION_KEY;
		CollationKey key = Collator.getInstance().getCollationKey(
				attributeValue);
		getCache().put(attribute, key);
		return key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerSupportItem#getCreationTime()
	 */
	long getCreationTime() {
		if(stale){
			return -1;
		}
		try {
			return marker.getCreationTime();
		} catch (CoreException e) {
			checkIfMarkerStale();
			Policy.handle(e);
			return -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerSupportItem#getDescription()
	 */
	String getDescription() {
		return getAttributeValue(IMarker.MESSAGE,
				MarkerSupportInternalUtilities.UNKNOWN_ATRRIBTE_VALUE_STRING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerSupportItem#getID()
	 */
	long getID() {
		return marker.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.MarkerItem#getLocation()
	 */
	public String getLocation() {
		if(stale||checkIfMarkerStale()){
			return MarkerSupportInternalUtilities.UNKNOWN_ATRRIBTE_VALUE_STRING;
		}
		if (getCache().containsKey(LOCATION_STRING)) {
			Object value = getCache().get(LOCATION_STRING);
			if (value instanceof CollationKey)
				return ((CollationKey) value).getSourceString();
			return (String) value;
		}


		// Is the location override set?
		String locationString = getAttributeValue(IMarker.LOCATION,
				MarkerSupportInternalUtilities.EMPTY_STRING);
		if (locationString.length() > 0) {
			getCache().put(LOCATION_STRING, locationString);
			return locationString;
		}

		// No override so use line number
		int lineNumber = getAttributeValue(IMarker.LINE_NUMBER, -1);
		String lineNumberString;
		if (lineNumber < 0)
			lineNumberString = MarkerMessages.Unknown;
		else
			lineNumberString = NLS.bind(MarkerMessages.label_lineNumber,
					Integer.toString(lineNumber));

		getCache().put(LOCATION_STRING, lineNumberString);
		return lineNumberString;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.MarkerItem#getMarker()
	 */
	public IMarker getMarker() {
		return marker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerSupportItem#getMarkerTypeName()
	 */
	String getMarkerTypeName() {
		if(stale){
			return NLS.bind(MarkerMessages.FieldMessage_WrongType, marker
					.toString());
		}
		try {
			return MarkerTypesModel.getInstance().getType(marker.getType())
					.getLabel();
		} catch (CoreException e) {
			checkIfMarkerStale() ;
			Policy.handle(e);
			return NLS.bind(MarkerMessages.FieldMessage_WrongType, marker
					.toString());
		}
	}
	String getMarkerTypeId() {
		if(stale){
			return NLS.bind(MarkerMessages.FieldMessage_WrongType, marker
					.toString());
		}
		try {
			return marker.getType();
		} catch (CoreException e) {
			checkIfMarkerStale();
			Policy.handle(e);
			return NLS.bind(MarkerMessages.FieldMessage_WrongType, marker
					.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerSupportItem#getParent()
	 */
	MarkerSupportItem getParent() {
		return category;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.MarkerItem#getPath()
	 */
	public String getPath() {
		String folder = getAttributeValue(MarkerViewUtil.PATH_ATTRIBUTE, null);
		if (folder != null) {
			return folder;
		}
		if (stale||checkIfMarkerStale()) {
			return MarkerSupportInternalUtilities.UNKNOWN_ATRRIBTE_VALUE_STRING;
		}
		IPath path = marker.getResource().getFullPath();
		int n = path.segmentCount() - 1; // n is the number of segments
		// in container, not path
		if (n <= 0) {
			return super.getPath();
		}
		folder = path.removeLastSegments(1).removeTrailingSeparator()
				.toString();
		getCache().put(MarkerViewUtil.PATH_ATTRIBUTE, folder);
		return folder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerSupportItem#isConcrete()
	 */
	boolean isConcrete() {
		return true;
	}

	/**
	 * Set the category to markerCategory.
	 * 
	 * @param markerCategory
	 */
	void setCategory(MarkerCategory markerCategory) {
		category = markerCategory;

	}

	/**
	 * Set the marker for the receiver.
	 * 
	 * @param marker
	 *            The marker to set.
	 */
	void setMarker(IMarker marker) {
		this.marker = marker;
		// reset stale
		stale = false;
		clearCache();
	}

	/**
	 * Get the cache for the receiver. Create if neccessary.
	 * 
	 * @return {@link HashMap}
	 */
	Map getCache() {
		if (cache == null)
			cache = new HashMap(2);
		return cache;
	}

	/**
	 * Clear the cached values for performance reasons.
	 */
	void clearCache() {
		cache = null;		
	}

	/**
	 * @return true if the marker does not exist
	 * 		   else false
	 */
	boolean checkIfMarkerStale() {
		if (stale) {
			return true;
		}
		if (marker == null || !marker.exists()) {
			stale = true;
		}
		return stale;
	}

	/**
	 * 
	 * @return true if the {@link MarkerEntry} is stale,i.e. the marker does not
	 *         exist. A false value can mean that marker's state of existence was
	 *         never captured or that it exists.#checkIfMarkerExists() will
	 *         accurately indicate its state.
	 */
	boolean getStaleState() {
		return stale;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((marker == null) ? 0 : marker.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MarkerEntry)) {
			return false;
		}
		MarkerEntry other = (MarkerEntry) obj;
		if (marker == null) {
			if (other.marker != null) {
				return false;
			}
		} else if (!marker.equals(other.marker)) {
			return false;
		}
		return true;
	}
}

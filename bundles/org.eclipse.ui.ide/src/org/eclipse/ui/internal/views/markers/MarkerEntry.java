/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *     Andrey Loskutov <loskutov@gmx.de> - generified interface, bug 461762
 *     Patrik Suzzi <psuzzi@itemis.com> - bug 530702
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.text.CollationKey;
import java.text.Collator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.internal.ide.model.WorkbenchMarker;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.markers.MarkerViewUtil;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;

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

			@Override
			public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
				if (adapterType == IMarker.class && adaptableObject instanceof MarkerEntry) {
					return adapterType.cast(((MarkerEntry) adaptableObject).getMarker());
				}
				return null;
			}

			@Override
			public Class<?>[] getAdapterList() {
				return new Class[] { IMarker.class };
			}
		}, MarkerEntry.class);
	}

	// The key for the string we built for display
	private static final String LOCATION_STRING = "LOCATION_STRING"; //$NON-NLS-1$
	private MarkerCategory category;
	private Map<String, Object> cache;
	private static Map<String, CollationKey> collationCache = new ConcurrentHashMap<>();

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

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(IMarker.class)) {
			return adapter.cast(marker);
		}
		if (adapter.equals(IWorkbenchAdapter.class)) {
			return adapter.cast(new WorkbenchMarker() {

				@Override
				public Object getParent(Object o) {
					return super.getParent(marker);
				}

				@Override
				public String getLabel(Object o) {
					return getMarkerTypeName();
				}

				@Override
				public ImageDescriptor getImageDescriptor(Object object) {
					return super.getImageDescriptor(marker);
				}

			});
		}
		return null;
	}

	@Override
	public boolean getAttributeValue(String attribute, boolean defaultValue) {
		Object value = getAttributeValue(attribute);
		if (value == null) {
			return defaultValue;
		}
		return ((Boolean) value).booleanValue();
	}

	@Override
	public int getAttributeValue(String attribute, int defaultValue) {
		Object value = getAttributeValue(attribute);
		if (value == null) {
			return defaultValue;
		}
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
		if (value instanceof CollationKey) {
			return ((CollationKey) value).getSourceString();
		}
		return value;
	}

	@Override
	public String getAttributeValue(String attribute, String defaultValue) {
		Object value = getAttributeValue(attribute);
		if (value == null) {
			return defaultValue;
		}
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

	@Override
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
			if (value instanceof CollationKey) {
				return (CollationKey) value;
			}
			attributeValue = value.toString();
		} else {
			attributeValue = getAttributeValue(attribute, defaultValue);
		}

		if (attributeValue.isEmpty()) {
			return MarkerSupportInternalUtilities.EMPTY_COLLATION_KEY;
		}
		CollationKey key = collationCache.computeIfAbsent(attributeValue,
				k -> Collator.getInstance().getCollationKey(attributeValue));
		getCache().put(attribute, key);
		return key;
	}

	@Override
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

	@Override
	String getDescription() {
		return getAttributeValue(IMarker.MESSAGE, MarkerSupportInternalUtilities.UNKNOWN_ATRRIBTE_VALUE_STRING);
	}

	@Override
	long getID() {
		return marker.getId();
	}

	@Override
	public String getLocation() {
		if(stale||checkIfMarkerStale()){
			return MarkerSupportInternalUtilities.UNKNOWN_ATRRIBTE_VALUE_STRING;
		}
		Object value = getCache().get(LOCATION_STRING);
		if (value != null) {
			if (value instanceof CollationKey) {
				return ((CollationKey) value).getSourceString();
			}
			return (String) value;
		}


		// Is the location override set?
		String locationString = getAttributeValue(IMarker.LOCATION, MarkerItemDefaults.LOCATION_DEFAULT);
		if (locationString.length() > 0) {
			getCache().put(LOCATION_STRING, locationString);
			return locationString;
		}

		// No override so use line number
		int lineNumber = getAttributeValue(IMarker.LINE_NUMBER, -1);
		String lineNumberString;
		if (lineNumber < 0) {
			lineNumberString = MarkerMessages.Unknown;
		} else {
			lineNumberString = NLS.bind(MarkerMessages.label_lineNumber, Integer.toString(lineNumber));
		}

		getCache().put(LOCATION_STRING, lineNumberString);
		return lineNumberString;

	}

	@Override
	public IMarker getMarker() {
		return marker;
	}

	@Override
	String getMarkerTypeName() {
		if(stale){
			return NLS.bind(MarkerMessages.FieldMessage_WrongType, marker.toString());
		}
		try {
			return MarkerTypesModel.getInstance().getType(marker.getType()).getLabel();
		} catch (CoreException e) {
			checkIfMarkerStale() ;
			Policy.handle(e);
			return NLS.bind(MarkerMessages.FieldMessage_WrongType, marker.toString());
		}
	}

	String getMarkerTypeId() {
		if(stale){
			return NLS.bind(MarkerMessages.FieldMessage_WrongType, marker.toString());
		}
		try {
			return marker.getType();
		} catch (CoreException e) {
			checkIfMarkerStale();
			Policy.handle(e);
			return NLS.bind(MarkerMessages.FieldMessage_WrongType, marker.toString());
		}
	}

	@Override
	MarkerSupportItem getParent() {
		return category;
	}

	@Override
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
		folder = path.removeLastSegments(1).removeTrailingSeparator().toString();
		getCache().put(MarkerViewUtil.PATH_ATTRIBUTE, folder);
		return folder;
	}

	@Override
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
	Map<String, Object> getCache() {
		if (cache == null) {
			cache = new HashMap<>(2);
		}
		return cache;
	}

	/**
	 * Clear the cached values for performance reasons.
	 */
	@Override
	void clearCache() {
		cache = null;
	}

	static void clearCollationCache() {
		collationCache = new ConcurrentHashMap<>();
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

	@Override
	public int hashCode() {
		return Objects.hashCode(marker);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MarkerEntry)) {
			return false;
		}
		MarkerEntry other = (MarkerEntry) obj;
		return Objects.equals(marker, other.marker);
	}
}

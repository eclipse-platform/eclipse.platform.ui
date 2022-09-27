/*******************************************************************************
 * Copyright (c) 2007, 2022 IBM Corporation and others.
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
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
	private static final Object CACHED_NULL = new Object();
	private MarkerCategory category;
	private final Map<String, Object> cache = new ConcurrentHashMap<>();
	private static Map<String, CollationKey> collationCache = new ConcurrentHashMap<>();

	/**
	 * Set the MarkerEntry to be stale, if discovered at any point of time
	 * of its use.This will greatly speed up a lot of parts of the view.
	 * @since 3.6
	 */
	private boolean stale;
	/** cached value **/
	private long creationTime;
	/** cached value **/
	private String markerType;
	/** cached value **/
	private String markerTypeName;

	/**
	 * Important: access to these fields must be via methods, they must be in sync
	 * and their values should reflect correctly the state of the other
	 */
	private final IMarker marker;

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
		Object value = getCachedValueOrCompute(attribute, () -> {
			if(stale){
				return null;
			}
			Object v;
			try {
				v = marker.getAttribute(attribute);
			} catch (CoreException e) {
				checkIfMarkerStale();
				v = null;
			}
			return v;
		});
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
		String attributeValue = getAttributeValue(attribute, defaultValue);
		if (attributeValue.isEmpty()) {
			return MarkerSupportInternalUtilities.EMPTY_COLLATION_KEY;
		}
		CollationKey key = collationCache.computeIfAbsent(attributeValue,
				k -> Collator.getInstance().getCollationKey(attributeValue));
		return key;
	}

	@Override
	long getCreationTime() {
		if (creationTime != 0) {
			return creationTime;
		}
		if(stale){
			creationTime = -1;
			return creationTime;
		}
		try {
			creationTime = marker.getCreationTime();
			return creationTime;
		} catch (CoreException e) {
			checkIfMarkerStale();
			Policy.handle(e);
			creationTime = -1;
			return creationTime;
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
		Object value = getCachedValueOrCompute(LOCATION_STRING, () -> {
			String locationString = getAttributeValue(IMarker.LOCATION, MarkerItemDefaults.LOCATION_DEFAULT);
			if (locationString.length() > 0) {
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
			return lineNumberString;
		});
		return (String) value;
	}

	@Override
	public IMarker getMarker() {
		return marker;
	}

	@Override
	String getMarkerTypeName() {
		if (markerTypeName != null) {
			return markerTypeName;
		}
		if(stale){
			markerTypeName = NLS.bind(MarkerMessages.FieldMessage_WrongType, marker.toString());
			return markerTypeName;
		}
		try {
			markerTypeName = MarkerTypesModel.getInstance().getType(marker.getType()).getLabel();
			return markerTypeName;
		} catch (CoreException e) {
			checkIfMarkerStale() ;
			Policy.handle(e);
			markerTypeName = NLS.bind(MarkerMessages.FieldMessage_WrongType, marker.toString());
			return markerTypeName;
		}
	}

	String getMarkerTypeId() {
		if (markerType != null) {
			return markerType;
		}
		if(stale){
			markerType = NLS.bind(MarkerMessages.FieldMessage_WrongType, marker.toString());
			return markerType;
		}
		try {
			markerType = marker.getType();
			return markerType;
		} catch (CoreException e) {
			checkIfMarkerStale();
			Policy.handle(e);
			markerType = NLS.bind(MarkerMessages.FieldMessage_WrongType, marker.toString());
			return markerType;
		}
	}

	@Override
	MarkerSupportItem getParent() {
		return category;
	}

	@Override
	public String getPath() {
		Object value = getCachedValueOrCompute(MarkerViewUtil.PATH_ATTRIBUTE, () -> {
			if (stale || checkIfMarkerStale()) {
				return MarkerSupportInternalUtilities.UNKNOWN_ATRRIBTE_VALUE_STRING;
			}
			return getPath(marker.getResource());
		});
		return (String) value;
	}

	protected String getPath(IResource resource) {
		IPath path = resource.getFullPath();
		int n = path.segmentCount() - 1; // n is the number of segments
		// in container, not path
		if (n <= 0) {
			return super.getPath();
		}
		String folder = path.removeLastSegments(1).removeTrailingSeparator().toString();
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

	protected Object getCachedValueOrCompute(String key, Supplier<Object> supplier) {
		Object cached = cache.computeIfAbsent(key, k -> {
			Object value = supplier.get();
			// also remember null values:
			Object toCache = (value != null) ? value : CACHED_NULL;
			return toCache;
		});
		Object value = (cached == CACHED_NULL) ? null : cached;
		return value;
	}

	/**
	 * Clear the cached values for performance reasons.
	 */
	@Override
	void clearCache() {
		cache.clear();
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

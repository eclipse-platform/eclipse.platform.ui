/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.events;

import org.eclipse.core.internal.resources.ICoreConstants;
import org.eclipse.core.internal.resources.ResourceInfo;
import org.eclipse.core.internal.watson.IElementComparator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;

/**
 * Compares two Resources and returns flags describing how
 * they have changed, for use in computing deltas.
 * Implementation note: rather than defining a partial order
 * as specified by IComparator, the compare operation returns
 * a set of flags instead.  The delta computation only cares
 * whether the comparison is zero (equal) or non-zero (not equal).
 */
public class ResourceComparator implements IElementComparator, ICoreConstants {
	// boolean indicating whether or not this comparator is to be used for
	// a notification. (as opposed to a build) Notifications include extra information
	// like marker and sync info changes.
	boolean notification;

	/* Singeton instances */
	protected static ResourceComparator markerSingleton;
	protected static ResourceComparator noMarkerSingleton;

	/**
	 * Create a comparator which compares resource infos.
	 * If includeMarkerDeltas is true, check for marker deltas.
	 */
	private ResourceComparator(boolean notification) {
		this.notification = notification;
	}

	/**
	 * Compare the ElementInfos for two resources.
	 */
	public int compare(Object o1, Object o2) {
		// == handles null, null.
		if (o1 == o2)
			return IResourceDelta.NO_CHANGE;
		int result = 0;
		if (o1 == null)
			return ((ResourceInfo) o2).isSet(M_PHANTOM) ? IResourceDelta.ADDED_PHANTOM : IResourceDelta.ADDED;
		if (o2 == null)
			return ((ResourceInfo) o1).isSet(M_PHANTOM) ? IResourceDelta.REMOVED_PHANTOM : IResourceDelta.REMOVED;
		if (!(o1 instanceof ResourceInfo && o2 instanceof ResourceInfo))
			return IResourceDelta.NO_CHANGE;
		ResourceInfo oldElement = (ResourceInfo) o1;
		ResourceInfo newElement = (ResourceInfo) o2;
		if (!oldElement.isSet(M_PHANTOM) && newElement.isSet(M_PHANTOM))
			return IResourceDelta.REMOVED;
		if (oldElement.isSet(M_PHANTOM) && !newElement.isSet(M_PHANTOM))
			return IResourceDelta.ADDED;
		if (!compareOpen(oldElement, newElement))
			result |= IResourceDelta.OPEN;
		if (!compareContents(oldElement, newElement)) {
			if (oldElement.getType() == IResource.PROJECT)
				result |= IResourceDelta.DESCRIPTION;
			else
				result |= IResourceDelta.CONTENT;
		}
		if (!compareType(oldElement, newElement))
			result |= IResourceDelta.TYPE;
		if (!compareNodeIDs(oldElement, newElement)) {
			result |= IResourceDelta.REPLACED;
			// if the node was replaced and the old and new were files, this is also a content change.
			if (oldElement.getType() == IResource.FILE && newElement.getType() == IResource.FILE)
				result |= IResourceDelta.CONTENT;
		}
		if (notification && !compareSync(oldElement, newElement))
			result |= IResourceDelta.SYNC;
		if (notification && !compareMarkers(oldElement, newElement))
			result |= IResourceDelta.MARKERS;
		if (!compareCharsets(oldElement, newElement))
			result |= IResourceDelta.ENCODING;
		return result == 0 ? 0 : result | IResourceDelta.CHANGED;
	}

	/**
	 * Compares the contents of the ResourceInfo.
	 */
	private boolean compareContents(ResourceInfo oldElement, ResourceInfo newElement) {
		return oldElement.getContentId() == newElement.getContentId();
	}

	private boolean compareCharsets(ResourceInfo oldElement, ResourceInfo newElement) {
		return oldElement.getCharsetGenerationCount() == newElement.getCharsetGenerationCount();
	}

	private boolean compareMarkers(ResourceInfo oldElement, ResourceInfo newElement) {
		// If both sets of markers are null then prehaps we added some markers
		// but then deleted them right away before notification. In that case
		// don't signify a marker change in the delta.
		boolean bothNull = oldElement.getMarkers(false) == null && newElement.getMarkers(false) == null;
		return bothNull || oldElement.getMarkerGenerationCount() == newElement.getMarkerGenerationCount();
	}

	/**
	 * Compares the node IDs of the ElementInfos for two resources.
	 */
	private boolean compareNodeIDs(ResourceInfo oldElement, ResourceInfo newElement) {
		return oldElement.getNodeId() == newElement.getNodeId();
	}

	/**
	 * Compares the open state of the ElementInfos for two resources.
	 */
	private boolean compareOpen(ResourceInfo oldElement, ResourceInfo newElement) {
		return oldElement.isSet(M_OPEN) == newElement.isSet(M_OPEN);
	}

	private boolean compareSync(ResourceInfo oldElement, ResourceInfo newElement) {
		return oldElement.getSyncInfoGenerationCount() == newElement.getSyncInfoGenerationCount();
	}

	/**
	 * Compares the type of the ResourceInfo.
	 */
	private boolean compareType(ResourceInfo oldElement, ResourceInfo newElement) {
		return oldElement.getType() == newElement.getType();
	}

	/**
	 * Returns a comparator which compares resource infos.
	 * Does not check for marker deltas.
	 */
	public static ResourceComparator getComparator() {
		return getComparator(false);
	}

	/**
	 * Returns a comparator which compares resource infos.
	 * If includeMarkerDeltas is true, check for marker deltas.
	 */
	public static ResourceComparator getComparator(boolean includeMarkerDeltas) {
		if (includeMarkerDeltas) {
			if (markerSingleton == null) {
				markerSingleton = new ResourceComparator(includeMarkerDeltas);
			}
			return markerSingleton;
		}
		if (noMarkerSingleton == null) {
			noMarkerSingleton = new ResourceComparator(includeMarkerDeltas);
		}
		return noMarkerSingleton;
	}
}
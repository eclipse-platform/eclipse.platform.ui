/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.*;
import org.eclipse.core.internal.utils.Assert;
import java.util.Map;
/**
 * @see IMarkerDelta
 */
public class MarkerDelta implements IMarkerDelta, IMarkerSetElement {
	protected int kind;
	protected IResource resource;
	protected MarkerInfo info;
/**
 * Creates a new marker delta.
 */
public MarkerDelta(int kind, IResource resource, MarkerInfo info) {
	this.kind = kind;
	this.resource = resource;
	this.info = info;
}
/**
 * @see IMarkerDelta#getAttribute
 */
public Object getAttribute(String attributeName) {
	return info.getAttribute(attributeName);
}
/**
 * @see IMarkerDelta#getAttribute
 */
public int getAttribute(String attributeName, int defaultValue) {
	Object value = info.getAttribute(attributeName);
	if (value == null)
		return defaultValue;
	if (value instanceof Integer)
		return ((Integer) value).intValue();
	Assert.isTrue(false);
	// avoid compiler error
	return -1;
}
/**
 * @see IMarkerDelta#getAttribute
 */
public String getAttribute(String attributeName, String defaultValue) {
	Object value = info.getAttribute(attributeName);
	if (value == null)
		return defaultValue;
	if (value instanceof String)
		return (String) value;
	Assert.isTrue(false);
	// avoid compiler error
	return null;
}
/**
 * @see IMarkerDelta#getAttribute
 */
public boolean getAttribute(String attributeName, boolean defaultValue) {
	Object value = info.getAttribute(attributeName);
	if (value == null)
		return defaultValue;
	if (value instanceof Boolean)
		return ((Boolean) value).booleanValue();
	Assert.isTrue(false);
	// avoid compiler error
	return false;
}
/**
 * @see IMarkerDelta#getAttributes
 */
public Map getAttributes() {
	return info.getAttributes();
}
/**
 * @see IMarkerDelta#getAttributes
 */
public Object[] getAttributes(String[] attributeNames) {
	return info.getAttributes(attributeNames);
}
/**
 * @see IMarkerDelta#getId
 */
public long getId() {
	return info.getId();
}
/**
 * @see IMarkerDelta#getKind
 */
public int getKind() {
	return kind;
}
/**
 * @see IMarkerDelta#getMarker
 */
public IMarker getMarker() {
	return new Marker(resource, getId());
}
/**
 * @see IMarkerDelta#getResource
 */
public IResource getResource() {
	return resource;
}
/**
 * @see IMarkerDelta#getType
 */
public String getType() {
	return info.getType();
}
/**
 * @see IMarkerDelta#isSubtypeOf
 */
public boolean isSubtypeOf(String superType) {
	return ((Workspace) getResource().getWorkspace()).getMarkerManager().getCache().isSubtype(getType(), superType);
}

/**
 * Merge two sets of marker changes.  Both sets must be on the same resource. Use the original set
 * of changes to store the result so we don't have to build a completely different set to return.
 * 
 * add + add = N/A
 * add + remove = nothing (no delta)
 * add + change = add
 * remove + add = N/A
 * remove + remove = N/A
 * remove + change = N/A
 * change + add = N/A
 * change + change = change  (note: info held onto by the marker delta should be that of the oldest change, and not replaced when composed)
 * change + remove = remove (note: info held onto by the marker delta should be that of the oldest change, and not replaced when changed to a remove)
 */
protected static MarkerSet merge(MarkerSet oldChanges, IMarkerDelta[] newChanges) {
	if (oldChanges == null) {
		MarkerSet result = new MarkerSet(newChanges.length);
		for (int i = 0; i < newChanges.length; i++)
			result.add((MarkerDelta) newChanges[i]);
		return result;
	}
	if (newChanges == null)
		return oldChanges;

	for (int i = 0; i < newChanges.length; i++) {
		MarkerDelta newDelta = (MarkerDelta) newChanges[i];
		MarkerDelta oldDelta = (MarkerDelta) oldChanges.get(newDelta.getId());
		if (oldDelta == null) {
			oldChanges.add(newDelta);
			continue;
		}
		switch (oldDelta.getKind()) {
			case IResourceDelta.ADDED :
				switch (newDelta.getKind()) {
					case IResourceDelta.ADDED :
						// add + add = N/A
						break;
					case IResourceDelta.REMOVED :
						// add + remove = nothing
						// Remove the original ADD delta.
						oldChanges.remove(oldDelta);
						break;
					case IResourceDelta.CHANGED :
						// add + change = add
						break;
				}
				break;
			case IResourceDelta.REMOVED :
				switch (newDelta.getKind()) {
					case IResourceDelta.ADDED :
						// remove + add = N/A
						break;
					case IResourceDelta.REMOVED :
						// remove + remove = N/A
						break;
					case IResourceDelta.CHANGED :
						// remove + change = N/A
						break;
				}
				break;
			case IResourceDelta.CHANGED :
				switch (newDelta.getKind()) {
					case IResourceDelta.ADDED :
						// change + add = N/A
						break;
					case IResourceDelta.REMOVED :
						// change + remove = remove
						// Change the delta kind.
						 ((MarkerDelta) oldDelta).setKind(IResourceDelta.REMOVED);
						break;
					case IResourceDelta.CHANGED :
						// change + change = change
						break;
				}
				break;
		}
	}
	return oldChanges;
}
private void setKind(int kind) {
	this.kind = kind;
}
}

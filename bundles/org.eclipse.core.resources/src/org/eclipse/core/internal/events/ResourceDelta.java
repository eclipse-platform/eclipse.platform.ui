/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.events;

import java.util.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
/**
 * Concrete implementation of the IResourceDelta interface.  Each ResourceDelta
 * object represents changes that have occurred between two states of the
 * resource tree.
 */
public class ResourceDelta extends PlatformObject implements IResourceDelta {
	protected IPath path;
	protected ResourceDeltaInfo deltaInfo;
	protected int status;
	protected ResourceInfo oldInfo;
	protected ResourceInfo newInfo;
	protected IResourceDelta[] children;
	// don't agressively set this, but cache it if called once
	protected IResource cachedResource;

	//
	protected static int KIND_MASK = 0xFF;
	private static IMarkerDelta[] EMPTY_MARKER_DELTAS = new IMarkerDelta[0];
protected ResourceDelta(IPath path, ResourceDeltaInfo deltaInfo) {
	super();
	this.path = path;
	this.deltaInfo = deltaInfo;
}

/*
 * @see IResourceDelta#accept(IResourceDeltaVisitor)
 */
public void accept(IResourceDeltaVisitor visitor) throws CoreException {
	// forward to central method
	accept(visitor, 0);
}

/*
 * @see IResourceDelta#accept(IResourceDeltaVisitor, boolean)
 */
public void accept(IResourceDeltaVisitor visitor, boolean includePhantoms) throws CoreException {
	// forward to central method
	accept(visitor, includePhantoms ? IContainer.INCLUDE_PHANTOMS : 0);
}

/*
 * @see IResourceDelta#accept(IResourceDeltaVisitor, int)
 */
public void accept(IResourceDeltaVisitor visitor, int memberFlags) throws CoreException {
	final boolean includePhantoms = (memberFlags & IContainer.INCLUDE_PHANTOMS) != 0;
	int mask = includePhantoms ? ALL_WITH_PHANTOMS : REMOVED | ADDED | CHANGED;
	if ((getKind() | mask) == 0)
		return;
	if (!visitor.visit(this))
		return;
	//recurse over children
	for (int i = 0; i < children.length; i++) {
		IResourceDelta childDelta = children[i];
		IResource child = childDelta.getResource();
		// quietly exclude team-private members unless explicitly included
		if ((memberFlags & IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS) != 0
		     || !child.isTeamPrivateMember()) {
			childDelta.accept(visitor, memberFlags);
		}
	}
}

/**
 * Check for marker deltas, and set the appropriate change flag if there are any.
 */
protected void checkForMarkerDeltas() {
	if (deltaInfo.getMarkerDeltas() == null)
		return;
	int kind = getKind();
	// Only need to check for added and removed, or for changes on the workspace.
	// For changed, the bit is set in the comparator.
	if (path.isRoot() || kind == ADDED || kind == REMOVED) {
		MarkerSet changes = (MarkerSet) deltaInfo.getMarkerDeltas().get(path);
		if (changes != null && changes.size() > 0) {
			status |= MARKERS;
			// If there have been marker changes, then ensure kind is CHANGED (if not ADDED or REMOVED).
			// See 1FV9K20: ITPUI:WINNT - severe - task list - add or delete not working
			if (kind == 0) {
				status |= CHANGED;
			}
		}
	}
}
/**
 * @see IResourceDelta#findMember(IPath)
 */
public IResourceDelta findMember(IPath path) {
	int segmentCount = path.segmentCount();
	if (segmentCount == 0)
		return this;
		
	//iterate over the path and find matching child delta
	ResourceDelta current = this;
	segments: for (int i = 0; i < segmentCount; i++) {
		IResourceDelta[] currentChildren = current.children;
		for (int j = 0, jmax = currentChildren.length; j < jmax; j++) {
			if (currentChildren[j].getFullPath().lastSegment().equals(path.segment(i))) {
				current = (ResourceDelta)currentChildren[j];
				continue segments;
			}
		}
		//matching child not found, return
		return null;
	}
	return current;
}
/**
 * Delta information on moves and on marker deltas can only be computed after
 * the delta has been built.  This method fixes up the delta to accurately
 * reflect moves (setting MOVED_FROM and MOVED_TO), and marker changes on
 * added and removed resources.
 */
protected void fixMovesAndMarkers(ElementTree oldTree) {
	NodeIDMap nodeIDMap = deltaInfo.getNodeIDMap();
	if (!path.isRoot() && !nodeIDMap.isEmpty()) {
		int kind = getKind();
		switch (kind) {
			case ADDED :
			case CHANGED :
				long nodeID = newInfo.getNodeId();
				IPath oldPath = (IPath) nodeIDMap.getOldPath(nodeID);
				if (oldPath != null && !oldPath.equals(path)) {
					//get the old info from the old tree
					oldInfo = (ResourceInfo)oldTree.getElementData(oldPath);
					// Replace change flags by comparing old info with new info,
					// Note that we want to retain the kind flag, but replace all other flags
					// This is done only for MOVED_FROM, not MOVED_TO, since a resource may be both.
					status = (status & KIND_MASK) | (deltaInfo.getComparator().compare(oldInfo, newInfo) & ~KIND_MASK);
					status |= MOVED_FROM;
				}
		}
		switch (kind) {
			case CHANGED :
			case REMOVED :
				long nodeID = oldInfo.getNodeId();
				IPath newPath = (IPath) nodeIDMap.getNewPath(nodeID);
				if (newPath != null && !newPath.equals(path)) {
					status |= MOVED_TO;
				}
		}
	}
	
	//check for marker deltas -- this is affected by move computation
	//so must happen afterwards
	checkForMarkerDeltas();
	
	//recurse on children
	for (int i = 0; i < children.length; i++) {
		((ResourceDelta)children[i]).fixMovesAndMarkers(oldTree);
	}
}
/**
 * @see IResourceDelta#getAffectedChildren
 */
public IResourceDelta[] getAffectedChildren() {
	return getAffectedChildren(ADDED | REMOVED | CHANGED, IResource.NONE);
}
/**
 * @see IResourceDelta#getAffectedChildren(int)
 */
public IResourceDelta[] getAffectedChildren(int kindMask) {
	return getAffectedChildren(kindMask, IResource.NONE);
}

/*
 * @see IResourceDelta#getAffectedChildren(int, int)
 */
public IResourceDelta[] getAffectedChildren(int kindMask, int memberFlags) {
	int numChildren = children.length;
	//if there are no children, they all match
	if (numChildren == 0) {
		return children;
	}
	if ((memberFlags & IContainer.INCLUDE_PHANTOMS) != 0) {
		// reduce INCLUDE_PHANTOMS member flag to kind mask
		kindMask |= ADDED_PHANTOM | REMOVED_PHANTOM;
	}

	//first count the number of matches so we can allocate the exact array size
	int matching = 0;
	for (int i = 0; i < numChildren; i++) {
		if ((children[i].getKind() & kindMask) == 0) {
			// child has wrong kind
			continue;
		}
		if ((memberFlags & IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS) == 0
		    && children[i].getResource().isTeamPrivateMember()) {
			// child has is a team-private member which are not included
			continue;
		}
		matching++;
	}
			
	//use arraycopy if all match
	if (matching == numChildren) {
		IResourceDelta[] result = new IResourceDelta[children.length];
		System.arraycopy(children, 0, result, 0, children.length);
		return result;
	}
		
	//create the appropriate sized array and fill it
	IResourceDelta[] result = new IResourceDelta[matching];
	int nextPosition = 0;
	for (int i = 0; i < numChildren; i++) {
		if ((children[i].getKind() & kindMask) == 0) {
			// child has wrong kind
			continue;
		}
		if ((memberFlags & IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS) == 0
		    && children[i].getResource().isTeamPrivateMember()) {
			// child has is a team-private member which are not included
			continue;
		}
		result[nextPosition++] = children[i];
	}
	return result;
}


protected ResourceDeltaInfo getDeltaInfo() {
	return deltaInfo;
}
/**
 * @see IResourceDelta#getFlags
 */
public int getFlags() {
	return status & ~KIND_MASK;
}
/**
 * @see IResourceDelta#getFullPath
 */
public IPath getFullPath() {
	return path;
}
/**
 * @see IResourceDelta#getKind
 */
public int getKind() {
	return status & KIND_MASK;
}
/**
 * @see IResourceDelta#getMarkerDeltas
 */
public IMarkerDelta[] getMarkerDeltas() {
	Map markerDeltas = deltaInfo.getMarkerDeltas();
	if (markerDeltas == null)
		return EMPTY_MARKER_DELTAS;
	if (path == null)
		path = Path.ROOT;
	MarkerSet changes = (MarkerSet) markerDeltas.get(path);
	if (changes == null)
		return EMPTY_MARKER_DELTAS;
	IMarkerSetElement[] elements = changes.elements();
	IMarkerDelta[] result = new IMarkerDelta[elements.length];
	for (int i = 0; i < elements.length; i++)
		result[i] = (IMarkerDelta) elements[i];
	return result;
}
/**
 * @see IResourceDelta#getMovedFromPath
 */
public IPath getMovedFromPath() {
	if ((status & MOVED_FROM) != 0) {
		return deltaInfo.getNodeIDMap().getOldPath(newInfo.getNodeId());
	}
	return null;
}
/**
 * @see IResourceDelta#getMovedToPath
 */
public IPath getMovedToPath() {
	if ((status & MOVED_TO) != 0) {
		return deltaInfo.getNodeIDMap().getNewPath(oldInfo.getNodeId());
	}
	return null;
}
/**
 * @see IResourceDelta#getProjectRelativePath
 */
public IPath getProjectRelativePath() {
	IPath full = getFullPath();
	int count = full.segmentCount();
	if (count < 0)
		return null;
	if (count <= 1) // 0 or 1
		return Path.EMPTY;
	return full.removeFirstSegments(1);
}
/**
 * @see IResourceDelta#getResource
 */
public IResource getResource() {
	// return a cached copy if we have one
	if (cachedResource != null)
		return cachedResource;

	// if this is a delta for the root then return the root resource
	if (path.segmentCount() == 0)
		return deltaInfo.getWorkspace().getRoot();
	// if the delta is a remove then we have to look for the old info to find the type
	// of resource to create. 
	ResourceInfo info = null;
	if ((getKind() & (REMOVED | REMOVED_PHANTOM)) != 0)
		info = oldInfo;
	else
		info = newInfo;
	Assert.isNotNull(info, "Do not have resource info for resource in delta"); //$NON-NLS-1$
	cachedResource = deltaInfo.getWorkspace().newResource(path, info.getType());
	return cachedResource;
}
/**
 * @see IResourceDelta#hasAffectedChildren
 */
public boolean hasAffectedChildren() {
	return children.length > 0;
}
protected void setChildren(IResourceDelta[] children) {
	this.children = children;
}
protected void setNewInfo(ResourceInfo newInfo) {
	this.newInfo = newInfo;
}
protected void setOldInfo(ResourceInfo oldInfo) {
	this.oldInfo = oldInfo;
}
protected void setStatus(int status) {
	this.status = status;
}
/** 
 * Returns a string representation of this delta's
 * immediate structure suitable for debug purposes.
 */
public String toDebugString() {
	final StringBuffer buffer = new StringBuffer();
	writeDebugString(buffer);
	return buffer.toString();
}
/** 
 * Returns a string representation of this delta's
 * deep structure suitable for debug purposes.
 */
public String toDeepDebugString() {
	final StringBuffer buffer = new StringBuffer("\n"); //$NON-NLS-1$
	writeDebugString(buffer);
	for (int i = 0; i < children.length; ++i) {
		ResourceDelta delta = (ResourceDelta) children[i];
		buffer.append(delta.toDeepDebugString());
	}
	return buffer.toString();
}
/**
 * For debugging only
 */
public String toString() {
	return "ResourceDelta(" + path + ")"; //$NON-NLS-1$ //$NON-NLS-2$
}
/**
 * Provides a new set of markers for the delta.  This is used
 * when the delta is reused in cases where the only changes 
 * are marker changes.
 */
public void updateMarkers(Map markers) {
	deltaInfo.setMarkerDeltas(markers);
}
/** 
 * Writes a string representation of this delta's
 * immediate structure on the given string buffer.
 */
public void writeDebugString(StringBuffer buffer) {
	buffer.append(getFullPath());
	buffer.append("["); //$NON-NLS-1$
	switch (getKind()) {
		case ADDED :
			buffer.append('+');
			break;
		case ADDED_PHANTOM :
			buffer.append('>');
			break;
		case REMOVED :
			buffer.append('-');
			break;
		case REMOVED_PHANTOM :
			buffer.append('<');
			break;
		case CHANGED :
			buffer.append('*');
			break;
		case NO_CHANGE :
			buffer.append('~');
			break;
		default :
			buffer.append('?');
			break;
	}
	buffer.append("]: {"); //$NON-NLS-1$
	int changeFlags = getFlags();
	boolean prev = false;
	if ((changeFlags & CONTENT) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("CONTENT"); //$NON-NLS-1$
		prev = true;
	}
	if ((changeFlags & MOVED_FROM) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("MOVED_FROM(" + getMovedFromPath() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		prev = true;
	}
	if ((changeFlags & MOVED_TO) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("MOVED_TO(" + getMovedToPath() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		prev = true;
	}
	if ((changeFlags & OPEN) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("OPEN"); //$NON-NLS-1$
		prev = true;
	}
	if ((changeFlags & TYPE) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("TYPE"); //$NON-NLS-1$
		prev = true;
	}
	if ((changeFlags & SYNC) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("SYNC"); //$NON-NLS-1$
		prev = true;
	}
	if ((changeFlags & MARKERS) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("MARKERS"); //$NON-NLS-1$
		writeMarkerDebugString(buffer);
		prev = true;
	}
	if ((changeFlags & REPLACED) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("REPLACED"); //$NON-NLS-1$
		prev = true;
	}
	if ((changeFlags & DESCRIPTION) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("DESCRIPTION"); //$NON-NLS-1$
		prev = true;
	}
	buffer.append("}"); //$NON-NLS-1$
}
public void writeMarkerDebugString(StringBuffer buffer) {
	buffer.append("["); //$NON-NLS-1$
	for (Iterator e = deltaInfo.getMarkerDeltas().keySet().iterator(); e.hasNext();) {
		IPath key = (IPath) e.next();
		if (getResource().getFullPath().equals(key)) {
			IMarkerSetElement[] deltas = ((MarkerSet) deltaInfo.getMarkerDeltas().get(key)).elements();
			boolean addComma = false;
			for (int i = 0; i < deltas.length; i++) {
				IMarkerDelta delta = (IMarkerDelta) deltas[i];
				if (addComma)
					buffer.append(","); //$NON-NLS-1$
				switch (delta.getKind()) {
					case IResourceDelta.ADDED :
						buffer.append("+"); //$NON-NLS-1$
						break;
					case IResourceDelta.REMOVED :
						buffer.append("-"); //$NON-NLS-1$
						break;
					case IResourceDelta.CHANGED :
						buffer.append("*"); //$NON-NLS-1$
						break;
				}
				buffer.append(delta.getId());
				addComma = true;
			}
		}
	}
	buffer.append("]"); //$NON-NLS-1$
}

}
package org.eclipse.core.internal.events;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.dtree.*;
import org.eclipse.core.internal.events.ResourceDeltaInfo;
import org.eclipse.core.internal.resources.IMarkerSetElement;
import org.eclipse.core.internal.resources.MarkerSet;
import org.eclipse.core.internal.resources.ResourceInfo;
import java.util.*;

public class ResourceDelta extends PlatformObject implements IResourceDelta {
	protected IPath path;
	protected IPath movedToPath;
	protected IPath movedFromPath;
	protected ResourceDeltaInfo deltaInfo;
	protected int status;
	protected ResourceInfo oldInfo;
	protected ResourceInfo newInfo;
	protected IResourceDelta[] children;

	//
	protected static int KIND_MASK = 0xFF;
	private static IMarkerDelta[] EMPTY_MARKER_DELTAS = new IMarkerDelta[0];
protected ResourceDelta(IPath path, ResourceDeltaInfo deltaInfo) {
	super();
	this.path = path;
	this.deltaInfo = deltaInfo;
}
/**
 * @see IResourceDelta#accept(IResourceDeltaVisitor)
 */
public void accept(IResourceDeltaVisitor visitor) throws CoreException {
	accept(visitor, false);
}
/**
 * @see IResourceDelta#accept(IResourceDeltaVisitor, boolean)
 */
public void accept(IResourceDeltaVisitor visitor, boolean includePhantoms) throws CoreException {
	int mask = REMOVED | ADDED | CHANGED;
	if (includePhantoms)
		mask |= REMOVED_PHANTOM | ADDED_PHANTOM;
	if ((getKind() | mask) == 0)
		return;
	if (!visitor.visit(this))
		return;
	IResourceDelta[] children = getAffectedChildren(mask);
	for (int i = 0; i < children.length; i++)
		children[i].accept(visitor, includePhantoms);
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
 * Do the analysis to recover MOVED operations from ADDED/REMOVED/CHANGED operations.
 */
protected void checkForMove() {
	if (path.isRoot())
		return;
	int kind = getKind();
	switch (kind) {
		case ADDED :
		case CHANGED :
			long nodeID = newInfo.getNodeId();
			IPath oldPath = (IPath) deltaInfo.getOldNodeIDMap().get(new Long(nodeID));
			if (oldPath != null && !oldPath.equals(path)) {
				// Replace change flags by comparing old info with new info,
				// Note that we want to retain the kind flag, but replace all other flags
				// This is done only for MOVED_FROM, not MOVED_TO, since a resource may be both.
				status = (status & KIND_MASK) | (deltaInfo.getComparator().compare(oldInfo, newInfo) & ~KIND_MASK);
				status |= MOVED_FROM;
				movedFromPath = oldPath;
			}
	}
	switch (kind) {
		case CHANGED :
		case REMOVED :
			long nodeID = oldInfo.getNodeId();
			IPath newPath = (IPath) deltaInfo.getNewNodeIDMap().get(new Long(nodeID));
			if (newPath != null && !newPath.equals(path)) {
				status |= MOVED_TO;
				movedToPath = newPath;
			}
	}
}
/**
 * Destroy this delta and all of the internal data.  Since the bulk of the internal data
 * is shared with all other delta handle objects on the delta tree, this will invalidate
 * all of the other related resource delta objects
 */
public void destroy() {
	// just in case we've already been destroyed
	if (deltaInfo != null)
		deltaInfo.destroy();
	deltaInfo = null;
	oldInfo = null;
	newInfo = null;
}
/**
 * @see IResourceDelta#getAffectedChildren
 */
public IResourceDelta[] getAffectedChildren() {
	return getAffectedChildren(ADDED | REMOVED | CHANGED);
}
/**
 * @see IResourceDelta#getAffectedChildren(int)
 */
public IResourceDelta[] getAffectedChildren(int mask) {
	ArrayList result = new ArrayList(children.length);
	for (int i = 0; i < children.length; i++)
		if ((children[i].getKind() & mask) != 0)
			result.add(children[i]);
	return (IResourceDelta[]) result.toArray(new IResourceDelta[result.size()]);
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
	return movedFromPath;
}
/**
 * @see IResourceDelta#getMovedToPath
 */
public IPath getMovedToPath() {
	return movedToPath;
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
	// if this is a delta for the root then return null
	if (path.segmentCount() == 0)
		return deltaInfo.getWorkspace().getRoot();
	// if the delta is a remove then we have to look for the old info to find the type
	// of resource to create. 
	ResourceInfo info = null;
	if ((getKind() & (REMOVED | REMOVED_PHANTOM)) != 0)
		info = oldInfo;
	else
		info = newInfo;
	if (info == null)
		return null;
	return deltaInfo.getWorkspace().newResource(path, info.getType());
}
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
	final StringBuffer buffer = new StringBuffer("");
	writeDebugString(buffer);
	return buffer.toString();
}
/** 
 * Returns a string representation of this delta's
 * deep structure suitable for debug purposes.
 */
public String toDeepDebugString() {
	final StringBuffer buffer = new StringBuffer("\n");
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
	return "ResourceDelta(" + path + ")";
}
/** 
 * Writes a string representation of this delta's
 * immediate structure on the given string buffer.
 */
public void writeDebugString(StringBuffer buffer) {
	buffer.append(getFullPath());
	buffer.append("[");
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
	buffer.append("]: {");
	int changeFlags = getFlags();
	boolean prev = false;
	if ((changeFlags & CONTENT) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("CONTENT");
		prev = true;
	}
	if ((changeFlags & MOVED_FROM) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("MOVED_FROM(" + getMovedFromPath() + ")");
		prev = true;
	}
	if ((changeFlags & MOVED_TO) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("MOVED_TO(" + getMovedToPath() + ")");
		prev = true;
	}
	if ((changeFlags & OPEN) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("OPEN");
		prev = true;
	}
	if ((changeFlags & TYPE) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("TYPE");
		prev = true;
	}
	if ((changeFlags & SYNC) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("SYNC");
		prev = true;
	}
	if ((changeFlags & MARKERS) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("MARKERS");
		writeMarkerDebugString(buffer);
		prev = true;
	}
	if ((changeFlags & REPLACED) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("REPLACED");
		prev = true;
	}
	if ((changeFlags & DESCRIPTION) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("DESCRIPTION");
		prev = true;
	}
	buffer.append("}");
}
public void writeMarkerDebugString(StringBuffer buffer) {
	buffer.append("[");
	for (Iterator e = deltaInfo.getMarkerDeltas().keySet().iterator(); e.hasNext();) {
		IPath key = (IPath) e.next();
		if (getResource().getFullPath().equals(key)) {
			IMarkerSetElement[] deltas = ((MarkerSet) deltaInfo.getMarkerDeltas().get(key)).elements();
			boolean addComma = false;
			for (int i = 0; i < deltas.length; i++) {
				IMarkerDelta delta = (IMarkerDelta) deltas[i];
				if (addComma)
					buffer.append(",");
				switch (delta.getKind()) {
					case IResourceDelta.ADDED :
						buffer.append("+");
						break;
					case IResourceDelta.REMOVED :
						buffer.append("-");
						break;
					case IResourceDelta.CHANGED :
						buffer.append("*");
						break;
				}
				buffer.append(delta.getId());
				addComma = true;
			}
		}
	}
	buffer.append("]");
}
}

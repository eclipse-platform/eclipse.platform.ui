package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.internal.localstore.*;
import java.io.*;
import java.util.*;

/**
 * A marker manager stores and retrieves markers on resources in the workspace.
 */
public class MarkerManager implements IManager {
	protected Workspace workspace;
	protected MarkerTypeDefinitionCache cache = new MarkerTypeDefinitionCache();
	protected Hashtable markerDeltas = null;
	protected MarkerWriter writer = new MarkerWriter(this);
	private long changeId = 0;
	
/**
 * Creates a new marker manager
 */
public MarkerManager(Workspace workspace) {
	this.workspace = workspace;
}
/**
 * Adds the given markers to the given resource.
 * @see IResource#addMarkers 
 */
public void add(IResource resource, MarkerInfo[] newMarkers) throws CoreException {
	if (newMarkers.length == 0)
		return;
	Resource target = (Resource) resource;
	ResourceInfo info = workspace.getResourceInfo(target.getFullPath(), false, false);
	int flags = target.getFlags(info);
	target.checkExists(flags, false);
	info = workspace.getResourceInfo(resource.getFullPath(), false, true);
	// set the M_MARKERS_SNAP_DIRTY flag to indicate that this
	// resource's markers have changed since the last snapshot
	info.set(ICoreConstants.M_MARKERS_SNAP_DIRTY);
	MarkerSet markers = info.getMarkers();
	if (markers == null)
		markers = new MarkerSet(newMarkers.length);
	basicAdd(resource, markers, newMarkers);
	if (!markers.isEmpty())
		info.setMarkers(markers);
}
/**
 * Adds the new markers to the given set of markers.  If added, the markers
 * are associated with the specified resource.IMarkerDeltas for Added markers 
 * are generated.
 */
private void basicAdd(IResource resource, MarkerSet markers, MarkerInfo[] newMarkers) throws CoreException {
	IMarkerDelta[] changes = new IMarkerDelta[newMarkers.length];
	for (int i = 0; i < newMarkers.length; i++) {
		MarkerInfo newMarker = newMarkers[i];
		MarkerInfo original = null;
		// should always be a new marker.
		if (newMarker.getId() != MarkerInfo.UNDEFINED_ID) {
			String message = Policy.bind("resources.changeInAdd");
			throw new ResourceException(new ResourceStatus(IResourceStatus.INTERNAL_ERROR, resource.getFullPath(), message));
		}
		newMarker.setId(workspace.nextMarkerId());
		changes[i] = new MarkerDelta(IResourceDelta.ADDED, resource, newMarker);
		markers.add(newMarker);
	}
	changedMarkers(resource, changes);
}
/**
 * Returns the markers in the given set of markers which match the given type.
 */
private MarkerInfo[] basicFindMatching(MarkerSet markers, String type, boolean includeSubtypes) {
	List result = new ArrayList(markers.size());
	IMarkerSetElement[] elements = markers.elements();
	for (int i = 0; i < elements.length; i++) {
		MarkerInfo marker = (MarkerInfo) elements[i];
		// if the type is null then we are looking for all types of markers
		if (type == null)
			result.add(marker);
		else {
			if (includeSubtypes) {
				if (cache.isSubtype(marker.getType(), type))
					result.add(marker);
			} else {
				if (marker.getType().equals(type))
					result.add(marker);
			}
		}
	}
	return (MarkerInfo[]) result.toArray(new MarkerInfo[result.size()]);
}
/**
 * Removes markers of the specified type from the given resource.
 */
private void basicRemoveMarkers(IResource resource, String type, boolean includeSubtypes) {
	//don't get a modifiable info until we know we need to modify it.
	ResourceInfo info = workspace.getResourceInfo(resource.getFullPath(), false, false);
	MarkerSet markers = info.getMarkers();
	if (markers == null)
		return;
	IMarkerSetElement[] matching;
	if (type == null) {
		// if the type is null, all markers are to be removed.
		//now we need to crack open the tree
		info = workspace.getResourceInfo(resource.getFullPath(), false, true);
		info.setMarkers(null);
		matching = markers.elements();
	} else {
		matching = basicFindMatching(markers, type, includeSubtypes);
		// if none match, there is nothing to remove
		if (matching.length == 0)
			return;
		//now we need to crack open the tree
		info = workspace.getResourceInfo(resource.getFullPath(), false, true);
		
		// remove all the matching markers and also the whole 
		// set if there are no remaining markers
		markers.removeAll(matching);
		if (markers.size() == 0)
			info.setMarkers(null);
	}
	info.set(ICoreConstants.M_MARKERS_SNAP_DIRTY);
	IMarkerDelta[] changes = new IMarkerDelta[matching.length];
	for (int i = 0; i < matching.length; i++)
		changes[i] = new MarkerDelta(IResourceDelta.REMOVED, resource, (MarkerInfo) matching[i]);
	if (changes != null)
		changedMarkers(resource, changes);
}
/**
 * Returns the markers on the given target which match the specified type
 */
private IMarker[] buildMarkers(IMarkerSetElement[] markers, IResource target) {
	IMarker[] result = new IMarker[markers.length];
	for (int i = 0; i < markers.length; i++) {
		result[i] = new Marker(target, ((MarkerInfo) markers[i]).getId());
	}
	return result;
}
/**
 * Markers have changed on the given resource.  Remember the changes for subsequent notification.
 */
protected void changedMarkers(IResource resource, IMarkerDelta[] changes) {
	if (changes == null || changes.length == 0)
		return;
	if (markerDeltas == null)
		markerDeltas = new Hashtable(11);
	IPath path = resource.getFullPath();
	MarkerSet previousChanges = (MarkerSet) markerDeltas.get(path);
	MarkerSet result = MarkerDelta.merge(previousChanges, changes);
	if (result.size() == 0)
		markerDeltas.remove(path);
	else
		markerDeltas.put(path, result);
	changeId++;
	ResourceInfo info = workspace.getResourceInfo(path, false, true);
	if (info != null)
		info.incrementMarkerGenerationCount();
}
/**
 * Internal workspace lifecycle event
 */
public void changing(IProject project) {
}
/**
 * Internal workspace lifecycle event
 */
public void closing(IProject project) {
}
/**
 * Internal workspace lifecycle event
 */
public void deleting(IProject project) {
}

/**
 * Returns the marker with the given id or <code>null</code> if none is found.
 */
public IMarker findMarker(IResource resource, long id) {
	MarkerInfo info = findMarkerInfo(resource, id);
	return info == null ? null : new Marker(resource, info.getId());
}
/**
 * Returns the marker with the given id or <code>null</code> if none is found.
 */
public MarkerInfo findMarkerInfo(IResource resource, long id) {
	ResourceInfo info = workspace.getResourceInfo(resource.getFullPath(), false, false);
	if (info == null)
		return null;
	MarkerSet markers = info.getMarkers();
	if (markers == null)
		return null;
	return (MarkerInfo) markers.get(id);
}
/**
 * Returns all markers of the specified type on the given target, with option
 * to search the target's children.
 * Passing <code>null</code> for the type specifies a match
 * for all types (i.e., <code>null</code> is a wildcard.
  */
public IMarker[] findMarkers(IResource target, final String type, final boolean includeSubtypes, int depth) throws CoreException {
	final List result = new ArrayList(10);
	IResourceVisitor visitor = new IResourceVisitor() {
		public boolean visit(IResource resource) throws CoreException {
			IMarker[] matching = findMatchingMarkers(resource, type, includeSubtypes);
			if (matching != null)
				result.addAll(Arrays.asList(matching));
			return true;
		}
	};
	target.accept(visitor, depth, false);
	return (IMarker[]) result.toArray(new IMarker[result.size()]);
}
/**
 * Returns the markers on the given target which match the specified type
 */
public IMarker[] findMatchingMarkers(IResource target, String type, boolean includeSubtypes) {
	ResourceInfo info = ((Workspace) target.getWorkspace()).getResourceInfo(target.getFullPath(), false, false);
	MarkerSet markers = info.getMarkers();
	IMarkerSetElement[] matching;
	if (markers == null)
		return null;
	if (type == null)
		matching = markers.elements();
	else
		matching = basicFindMatching(markers, type, includeSubtypes);
	return buildMarkers(matching, target);
}

public boolean hasDeltas() {
	return markerDeltas != null && !markerDeltas.isEmpty();
}
public MarkerTypeDefinitionCache getCache() {
	return cache;
}

public long getChangeId() {
	return changeId;
}

/**
 * Returns the table of marker deltas, keyed by path.
 * Returns null if there are no deltas.
 *
 * @return Hashtable of IPath to IMarkerDelta[]
 */
public Hashtable getMarkerDeltas() {
	return markerDeltas;
}
public void moved(final IResource source, final IResource destination, int depth) throws CoreException {
	final int count = destination.getFullPath().segmentCount();

	// we removed from the source and added to the destination
	IResourceVisitor visitor = new IResourceVisitor() {
		public boolean visit(IResource resource) throws CoreException {
			Resource r = (Resource) resource;
			ResourceInfo info = r.getResourceInfo(false, true);
			MarkerSet markers = info.getMarkers();
			if (markers == null)
				return true;
			info.set(ICoreConstants.M_MARKERS_SNAP_DIRTY);
			IMarkerDelta[] removed = new IMarkerDelta[markers.size()];
			IMarkerDelta[] added = new IMarkerDelta[markers.size()];
			IPath path = resource.getFullPath().removeFirstSegments(count);
			path = source.getFullPath().append(path);
			IResource sourceChild = workspace.newResource(path, resource.getType());
			IMarkerSetElement[] elements = markers.elements();
			for (int i = 0; i < elements.length; i++) {
				// calculate the ADDED delta
				MarkerInfo markerInfo = (MarkerInfo) elements[i];
				IMarkerDelta delta = new MarkerDelta(IResourceDelta.ADDED, resource, markerInfo);
				added[i] = delta;
				// calculate the REMOVED delta
				delta = new MarkerDelta(IResourceDelta.REMOVED, sourceChild, markerInfo);
				removed[i] = delta;
			}
			changedMarkers(resource, added);
			changedMarkers(sourceChild, removed);
			return true;
		}
	};
	destination.accept(visitor, depth, false);
}
/**
 * Internal workspace lifecycle event
 */
public void opening(IProject project) {
}
/**
 * Removes the specified marker 
 */
public void removeMarker(IResource resource, long id) {
	MarkerInfo markerInfo = findMarkerInfo(resource, id);
	if (markerInfo == null)
		return;
	ResourceInfo info = ((Workspace) resource.getWorkspace()).getResourceInfo(resource.getFullPath(), false, true);
	MarkerSet markers = info.getMarkers();
	int size = markers.size();
	markers.remove(markerInfo);
	// if that was the last marker remove the set to save space.
	if (markers.size() == 0)
		info.setMarkers(null);
	// if we actually did remove a marker, post a delta for the change.
	if (markers.size() != size) {
		info.set(ICoreConstants.M_MARKERS_SNAP_DIRTY);
		IMarkerDelta[] change = new IMarkerDelta[] { new MarkerDelta(IResourceDelta.REMOVED, resource, markerInfo)};
		changedMarkers(resource, change);
	}
}
/**
 * Remove all markers for the given resource to the specified depth.
 */
public void removeMarkers(IResource resource) throws CoreException {
	removeMarkers(resource, null, false, IResource.DEPTH_INFINITE);
}
/**
 * Remove all markers with the given type from the node at the given path.
 * Passing <code>null</code> for the type specifies a match
 * for all types (i.e., <code>null</code> is a wildcard.
 */
public void removeMarkers(IResource target, final String type, final boolean includeSubtypes, int depth) throws CoreException {
	IResourceVisitor visitor = new IResourceVisitor() {
		public boolean visit(IResource resource) throws CoreException {
			basicRemoveMarkers(resource, type, includeSubtypes);
			return true;
		}
	};
	target.accept(visitor, depth, false);
}
/**
 * Reset the marker deltas.
 */
public void resetMarkerDeltas() {
	markerDeltas  = null;
} 
public void restore(IResource resource, boolean generateDeltas, IProgressMonitor monitor) throws CoreException {
	// first try and load the last saved file, then apply the snapshots
	restoreFromSave(resource, generateDeltas);
	restoreFromSnap(resource);
}
protected void restoreFromSave(IResource resource, boolean generateDeltas) throws CoreException {
	IPath sourceLocation = workspace.getMetaArea().getMarkersLocationFor(resource);
	IPath tempLocation = workspace.getMetaArea().getBackupLocationFor(sourceLocation);
	try {
		DataInputStream input = new DataInputStream(new SafeFileInputStream(sourceLocation.toOSString(), tempLocation.toOSString()));
		try {
			MarkerReader reader = new MarkerReader(workspace);
			reader.read(input, generateDeltas);
		} finally {
			input.close();
		}
	} catch (FileNotFoundException e) {
		// Ignore if no markers saved.
	} catch (IOException e) {
		String msg = Policy.bind("resources.readMeta", sourceLocation.toString());
		throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, sourceLocation, msg, e);
	}
}
protected void restoreFromSnap(IResource resource) {
	IPath sourceLocation = workspace.getMetaArea().getMarkersSnapshotLocationFor(resource);
	try {
		DataInputStream input = new DataInputStream(new SafeChunkyInputStream(sourceLocation.toOSString()));
		try {
			MarkerSnapshotReader reader = new MarkerSnapshotReader(workspace);
			while (true)
				reader.read(input);
		} catch (EOFException eof) {
			// ignore end of file
		} finally {
			input.close();
		}
	} catch (FileNotFoundException e) {
		// ignore if no markers saved
	} catch (Exception e) {
		// only log the exception, we should not fail restoring the snapshot
		String msg = Policy.bind("resources.readMeta", sourceLocation.toString());
		ResourcesPlugin.getPlugin().getLog().log(new ResourceStatus(IResourceStatus.FAILED_READ_METADATA, sourceLocation, msg, e));
	}
}
public void save(IResource resource, DataOutputStream output, List list) throws IOException {
	writer.save(resource, output, list);
}
public void snap(IResource resource, DataOutputStream output) throws IOException {
	writer.snap(resource, output);
}
/**
 * @see IManager
 */
public void shutdown(IProgressMonitor monitor) {
}
/**
 * @see IManager
 */
public void startup(IProgressMonitor monitor) throws CoreException {
}
}

package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Sorter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * A marker manager stores and retrieves markers on resources in the workspace.
 */
public class MarkerManager implements IManager {
	protected Workspace workspace;
	protected MarkerTypeDefinitionCache cache = new MarkerTypeDefinitionCache();
	protected Hashtable markerDeltas = null;
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
	MarkerSet markers = info.getMarkers();
	if (markers == null)
		markers = new MarkerSet(newMarkers.length);
	basicAdd(resource, markers, newMarkers);
	if (!markers.isEmpty())
		info.setMarkers(markers);
}
/**
 * Adds the new markers to the given set of markers.  If added, the markers
 * are associated with the specified resource and marked as immutable.
 * IMarkerDeltas for Added markers are generated.
 */
private void basicAdd(IResource resource, MarkerSet markers, MarkerInfo[] newMarkers) throws CoreException {
	IMarkerDelta[] changes = new IMarkerDelta[newMarkers.length];
	for (int i = 0; i < newMarkers.length; i++) {
		MarkerInfo newMarker = newMarkers[i];
		MarkerInfo original = null;
		// should always be a new marker.
		if (newMarker.getId() != MarkerInfo.UNDEFINED_ID)
			throw new ResourceException(new ResourceStatus(IResourceStatus.INTERNAL_ERROR, resource.getFullPath(), "Trying to CHANGE marker in ADD method."));
		newMarker.setId(workspace.nextMarkerId());
		changes[i] = new MarkerDelta(IResourceDelta.ADDED, resource, newMarker);
		markers.add(newMarker);
	}
	changedMarkers(resource, changes);
}
/**
 * Returns the count of the number of markers on the given 
 * target which match the specified type
 */
private int basicCountMatching(IResource resource, String type, boolean includeSubtypes) {
	ResourceInfo info = workspace.getResourceInfo(resource.getFullPath(), false, false);
	MarkerSet markers = info.getMarkers();
	if (markers == null)
		return 0;
	// if the type is null, all markers are to be counted.
	if (type == null)
		return markers.size();
	int result = 0;
	IMarkerSetElement[] elements = markers.elements();
	for (int i = 0; i < elements.length; i++) {
		String markerType = ((MarkerInfo) elements[i]).getType();
		if (includeSubtypes) {
			if (cache.isSubtype(markerType, type))
				result++;
		} else {
			if (markerType.equals(type))
				result++;
		}
	}
	return result;
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
	ResourceInfo info = workspace.getResourceInfo(resource.getFullPath(), false, true);
	MarkerSet markers = info.getMarkers();
	if (markers == null)
		return;
	IMarkerSetElement[] matching;
	if (type == null) {
		// if the type is null, all markers are to be removed.
		info.setMarkers(null);
		matching = markers.elements();
	} else {
		matching = basicFindMatching(markers, type, includeSubtypes);
		// if none match, there is nothing to remove
		if (matching.length == 0)
			return;
		// remove all the matching markers and also the whole 
		// set if there are no remaining markers
		markers.removeAll(matching);
		if (markers.size() == 0)
			info.setMarkers(null);
	}
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
 * Returns an Object array of length 2. The first element is an Integer which is the number 
 * of persistent markers found. The second element is an array of boolean values, with a 
 * value of true meaning that the marker at that index is to be persisted.
 */
private Object[] filterMarkers(IMarkerSetElement[] markers) {
	Object[] result = new Object[2];
	boolean[] isPersistent = new boolean[markers.length];
	int count = 0;
	for (int i = 0; i < markers.length; i++) {
		MarkerInfo info = (MarkerInfo) markers[i];
		if (cache.isPersistent(info.getType())) {
			isPersistent[i] = true;
			count++;
		}
	}
	result[0] = new Integer(count);
	result[1] = isPersistent;
	return result;
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
public MarkerTypeDefinitionCache getCache() {
	return cache;
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
			ResourceInfo info = r.getResourceInfo(false, false);
			MarkerSet markers = info.getMarkers();
			if (markers == null)
				return true;
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
public void read(DataInputStream input, boolean generateDeltas) throws CoreException {
	// the MarkerReader creates the appropriate reader depending on
	// the version of the file.
	MarkerReader reader = new MarkerReader(workspace);
	reader.read(input, generateDeltas);
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
private void write(Map attributes, DataOutputStream output) throws IOException {
	output.writeInt(attributes.size());
	for (Iterator i = attributes.keySet().iterator(); i.hasNext();) {
		String key = (String) i.next();
		output.writeUTF(key);
		Object value = attributes.get(key);
		if (value instanceof Integer) {
			output.writeInt(ICoreConstants.ATTRIBUTE_INTEGER);
			output.writeInt(((Integer) value).intValue());
			continue;
		}
		if (value instanceof Boolean) {
			output.writeInt(ICoreConstants.ATTRIBUTE_BOOLEAN);
			output.writeBoolean(((Boolean) value).booleanValue());
			continue;
		}
		if (value instanceof String) {
			output.writeInt(ICoreConstants.ATTRIBUTE_STRING);
			output.writeUTF((String) value);
			continue;
		}
		// otherwise we came across an attribute of an unknown type
		// so just write out null since we don't know how to marshal it.
		output.writeInt(ICoreConstants.ATTRIBUTE_NULL);
	}
}
private void write(MarkerInfo info, DataOutputStream output, List writtenTypes) throws IOException {
	output.writeLong(info.getId());
	// if we have already written the type once, then write an integer
	// constant to represent it instead to remove duplication
	String type = info.getType();
	int index = writtenTypes.indexOf(type);
	if (index == -1) {
		output.writeInt(ICoreConstants.TYPE_CONSTANT);
		output.writeUTF(type);
		writtenTypes.add(type);
	} else {
		output.writeInt(ICoreConstants.INT_CONSTANT);
		output.writeInt(index);
	}
	if (info.getAttributes(false) == null)
		output.writeInt(0);
	else
		write(info.getAttributes(false), output);
}
/**
VERSION_ID
RESOURCE[]

VERSION_ID:
	int (used for backwards compatibiliy)

RESOURCE:
	String - resource path
	int - markers array size
	MARKER[]

MARKER:
	int - marker id
	String - marker type
	int - attributes size
	ATTRIBUTE[]

ATTRIBUTE:
	String - key
	ATTRIBUTE_VALUE

ATTRIBUTE_VALUE:
	int - type indicator
	Integer/Boolean/String - value (no value if type == NULL)
	
 */
public void write(IResource resource, DataOutputStream output, List writtenTypes) throws IOException {
	ResourceInfo info = ((Resource) resource).getResourceInfo(false, false);
	if (info == null)
		return;
	MarkerSet markers = info.getMarkers();
	if (markers == null)
		return;
	IMarkerSetElement[] elements = markers.elements();
	// filter out the markers...determine if there are any persistent ones
	Object[] result = filterMarkers(elements);
	int count = ((Integer) result[0]).intValue();
	if (count == 0)
		return;
	// if this is the first set of markers that we have written, then
	// write the version id for the file.
	if (output.size() == 0)
		output.writeInt(ICoreConstants.MARKERS_VERSION);
	boolean[] isPersistent = (boolean[]) result[1];
	output.writeUTF(resource.getFullPath().toString());
	output.writeInt(count);
	for (int i = 0; i < elements.length; i++)
		if (isPersistent[i])
			write((MarkerInfo) elements[i], output, writtenTypes);
}
}

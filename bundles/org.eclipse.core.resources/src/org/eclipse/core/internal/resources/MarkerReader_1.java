package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

/**
 * This class is used to read markers from disk. This is for version 1. Here
 * is the file format:
 * 
 * VERSION_ID
 * RESOURCE[]
 * 
 * VERSION_ID:
 * int (used for backwards compatibiliy)
 * 
 * RESOURCE:
 * String - resource path
 * int - markers array size
 * MARKER[]
 * 
 * MARKER:
 * int - marker id
 * String - marker type
 * int - attributes size
 * ATTRIBUTE[]
 * 
 * ATTRIBUTE:
 * String - key
 * ATTRIBUTE_VALUE
 * 
 * ATTRIBUTE_VALUE:
 * int - type indicator
 * Integer/Boolean/String - value (no value if type == NULL)
 */
public class MarkerReader_1 extends MarkerReader {
public MarkerReader_1(Workspace workspace) {
	super(workspace);
}
public void read(DataInputStream input, boolean generateDeltas) throws CoreException {
	List readTypes = new ArrayList(5);
	try {
		while (input.available() != 0) {
			IPath path = new Path(input.readUTF());
			Resource resource = (Resource) workspace.getRoot().findMember(path);
			if (resource == null)
				return;
			ResourceInfo resourceInfo = resource.getResourceInfo(false, false);
			int markersSize = input.readInt();
			resourceInfo.markers = new MarkerSet(markersSize);
			for (int i = 0; i < markersSize; i++)
				resourceInfo.markers.add(readMarkerInfo(input, readTypes));
			if (generateDeltas) {
				// Iterate over all elements and add not null ones. This saves us from copying
				// and shrinking the array.
				IMarkerSetElement[] infos = resourceInfo.markers.elements;
				ArrayList deltas = new ArrayList(infos.length);
				for (int i = 0; i < infos.length; i++)
					if (infos[i] != null)
						deltas.add(new MarkerDelta(IResourceDelta.ADDED, resource, (MarkerInfo) infos[i]));
				workspace.getMarkerManager().changedMarkers(resource, (IMarkerDelta[]) deltas.toArray(new IMarkerDelta[deltas.size()]));
			}
		}
	} catch (IOException e) {
		throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, null, "Failed reading markers.", e);
	}
}
private Map readAttributes(DataInputStream input) throws IOException {
	int attributesSize = input.readInt();
	if (attributesSize == 0)
		return null;
	Map result = new HashMap(attributesSize);
	for (int j = 0; j < attributesSize; j++) {
		String key = input.readUTF();
		int type = input.readInt();
		Object value = null;
		switch (type) {
			case ICoreConstants.ATTRIBUTE_INTEGER :
				value = new Integer(input.readInt());
				break;
			case ICoreConstants.ATTRIBUTE_BOOLEAN :
				value = new Boolean(input.readBoolean());
				break;
			case ICoreConstants.ATTRIBUTE_STRING :
				value = input.readUTF();
				break;
			case ICoreConstants.ATTRIBUTE_NULL :
				// do nothing
				break;
		}
		if (value != null)
			result.put(key, value);
	}
	return result;
}
private MarkerInfo readMarkerInfo(DataInputStream input, List readTypes) throws IOException, CoreException {
	MarkerInfo info = new MarkerInfo();
	info.setId(input.readLong());
	int constant = input.readInt();
	switch (constant) {
		case ICoreConstants.TYPE_CONSTANT :
			String type = input.readUTF();
			info.setType(type);
			readTypes.add(type);
			break;
		case ICoreConstants.INT_CONSTANT :
			info.setType((String) readTypes.get(input.readInt()));
			break;
		default :
			// XXX: assert(false) here or throw a real exception?
			throw new ResourceException(new ResourceStatus(IResourceStatus.INTERNAL_ERROR, "Errors restoring markers."));
	}
	info.internalSetAttributes(readAttributes(input));
	return info;
}
}

package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.internal.utils.Assert;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.*;

/**
 * This class is used to read markers from disk. This is for version 2. Here
 * is the file format:
 */
public class MarkerReader_2 extends MarkerReader {

	// type constants
	public static final byte INDEX = 1;
	public static final byte QNAME = 2;

	// marker attribute types
	public static final byte ATTRIBUTE_NULL = 0;
	public static final byte ATTRIBUTE_BOOLEAN = 1;
	public static final byte ATTRIBUTE_INTEGER = 2;
	public static final byte ATTRIBUTE_STRING = 3;

public MarkerReader_2(Workspace workspace) {
	super(workspace);
}
/**
 * SAVE_FILE -> VERSION_ID RESOURCE+
 * VERSION_ID -> int
 * RESOURCE -> RESOURCE_PATH MARKERS_SIZE MARKER+
 * RESOURCE_PATH -> String
 * MARKERS_SIZE -> int
 * MARKER -> MARKER_ID TYPE ATTRIBUTES_SIZE ATTRIBUTE*
 * MARKER_ID -> long
 * TYPE -> INDEX | QNAME
 * INDEX -> byte int
 * QNAME -> byte String
 * ATTRIBUTES_SIZE -> short
 * ATTRIBUTE -> ATTRIBUTE_KEY ATTRIBUTE_VALUE
 * ATTRIBUTE_KEY -> String
 * ATTRIBUTE_VALUE -> INTEGER_VALUE | BOOLEAN_VALUE | STRING_VALUE | NULL_VALUE
 * INTEGER_VALUE -> byte int
 * BOOLEAN_VALUE -> byte boolean
 * STRING_VALUE -> byte String
 * NULL_VALUE -> byte
 */
public void read(DataInputStream input, boolean generateDeltas) throws IOException {
	try {
		List readTypes = new ArrayList(5);
		while (true) {
			IPath path = new Path(input.readUTF());
			int markersSize = input.readInt();
			MarkerSet markers = new MarkerSet(markersSize);
			for (int i = 0; i < markersSize; i++)
				markers.add(readMarkerInfo(input, readTypes));
			// if the resource doesn't exist then return. ensure we do this after
			// reading the markers from the file so we don't get into an
			// inconsistent state.
			ResourceInfo info = workspace.getResourceInfo(path, false, false);
			if (info == null)
				continue;
			info.setMarkers(markers);
			if (generateDeltas) {
				// Iterate over all elements and add not null ones. This saves us from copying
				// and shrinking the array.
				Resource resource = workspace.newResource(path, info.getType());
				IMarkerSetElement[] infos = markers.elements;
				ArrayList deltas = new ArrayList(infos.length);
				for (int i = 0; i < infos.length; i++)
					if (infos[i] != null)
						deltas.add(new MarkerDelta(IResourceDelta.ADDED, resource, (MarkerInfo) infos[i]));
				workspace.getMarkerManager().changedMarkers(resource, (IMarkerDelta[]) deltas.toArray(new IMarkerDelta[deltas.size()]));
			}
		}
	} catch (EOFException e) {
		// ignore end of file
	}
}
private Map readAttributes(DataInputStream input) throws IOException {
	int attributesSize = input.readShort();
	if (attributesSize == 0)
		return null;
	Map result = new MarkerAttributeMap(attributesSize);
	for (int j = 0; j < attributesSize; j++) {
		String key = input.readUTF();
		byte type = input.readByte();
		Object value = null;
		switch (type) {
			case ATTRIBUTE_INTEGER :
				value = new Integer(input.readInt());
				break;
			case ATTRIBUTE_BOOLEAN :
				value = new Boolean(input.readBoolean());
				break;
			case ATTRIBUTE_STRING :
				value = input.readUTF();
				break;
			case ATTRIBUTE_NULL :
				// do nothing
				break;
		}
		if (value != null)
			result.put(key, value);
	}
	return result.isEmpty() ? null : result;
}
private MarkerInfo readMarkerInfo(DataInputStream input, List readTypes) throws IOException {
	MarkerInfo info = new MarkerInfo();
	info.setId(input.readLong());
	byte constant = input.readByte();
	switch (constant) {
		case QNAME :
			String type = input.readUTF();
			info.setType(type);
			readTypes.add(type);
			break;
		case INDEX :
			info.setType((String) readTypes.get(input.readInt()));
			break;
		default :
			Assert.isTrue(false, "Errors restoring markers.");
	}
	info.internalSetAttributes(readAttributes(input));
	return info;
}
}

package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.runtime.*;
//
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
//
/**
 * Snapshot the markers for the specified resource to the given output stream.
 */
public class MarkerSnapshotReader_1 extends MarkerSnapshotReader {

	// type constants
	public static final byte INDEX = 1;
	public static final byte QNAME = 2;

	// marker attribute types
	public static final byte ATTRIBUTE_NULL = 0;
	public static final byte ATTRIBUTE_BOOLEAN = 1;
	public static final byte ATTRIBUTE_INTEGER = 2;
	public static final byte ATTRIBUTE_STRING = 3;

public MarkerSnapshotReader_1(Workspace workspace) {
	super(workspace);
}
/**
 * SNAP_FILE -> [VERSION_ID RESOURCE]*
 * VERSION_ID -> int (used for backwards compatibiliy)
 * RESOURCE -> RESOURCE_PATH MARKER_SIZE MARKER+
 * RESOURCE_PATH -> String
 * MARKER_SIZE -> int
 * MARKER -> MARKER_ID TYPE ATTRIBUTES_SIZE ATTRIBUTE*
 * MARKER_ID -> long
 * TYPE -> INDEX | QNAME
 * INDEX -> byte int
 * QNAME -> byte String
 * ATTRIBUTES_SIZE -> short
 * ATTRIBUTE -> ATTRIBUTE_KEY ATTRIBUTE_VALUE
 * ATTRIBUTE_KEY -> String
 * ATTRIBUTE_VALUE -> BOOLEAN_VALUE | INTEGER_VALUE | STRING_VALUE | NULL_VALUE
 * BOOLEAN_VALUE -> byte boolean
 * INTEGER_VALUE -> byte int
 * STRING_VALUE -> byte String
 * NULL_VALUE -> byte
 */
public void read(DataInputStream input) throws IOException {
	IPath path = new Path(input.readUTF());
	int markersSize = input.readInt();
	MarkerSet markers = new MarkerSet(markersSize);
	ArrayList readTypes = new ArrayList();
	for (int i = 0; i < markersSize; i++)
		markers.add(readMarkerInfo(input, readTypes));
	// we've read all the markers from the file for this snap. if the resource
	// doesn't exist in the workspace then consider this a delete and return
	ResourceInfo info = workspace.getResourceInfo(path, false, false);
	if (info == null)
		return;
	info.setMarkers(markers);
	info.clear(ICoreConstants.M_MARKERS_SNAP_DIRTY);
}
private Map readAttributes(DataInputStream input) throws IOException {
	short attributesSize = input.readShort();
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

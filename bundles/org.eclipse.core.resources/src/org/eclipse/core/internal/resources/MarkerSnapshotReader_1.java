/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *     Mickael Istria (Red Hat Inc.) - Bug 488937
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.*;

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
	@Override
	public void read(DataInputStream input) throws IOException, CoreException {
		IPath path = new Path(input.readUTF());
		int markersSize = input.readInt();
		MarkerSet markers = new MarkerSet(markersSize);
		ArrayList<String> readTypes = new ArrayList<>();
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

	private Map<String, Object> readAttributes(DataInputStream input) throws IOException {
		short attributesSize = input.readShort();
		if (attributesSize == 0)
			return null;
		Map<String, Object> result = new MarkerAttributeMap<>(attributesSize);
		for (int j = 0; j < attributesSize; j++) {
			String key = input.readUTF();
			byte type = input.readByte();
			Object value = null;
			switch (type) {
				case ATTRIBUTE_INTEGER :
					value = input.readInt();
					break;
				case ATTRIBUTE_BOOLEAN :
					value = input.readBoolean();
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

	private MarkerInfo readMarkerInfo(DataInputStream input, List<String> readTypes) throws IOException, CoreException {
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
				info.setType(readTypes.get(input.readInt()));
				break;
			default :
				//if we get here the marker file is corrupt
				String msg = Messages.resources_readMarkers;
				throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, null, msg, null);
		}
		info.internalSetAttributes(readAttributes(input));
		return info;
	}
}

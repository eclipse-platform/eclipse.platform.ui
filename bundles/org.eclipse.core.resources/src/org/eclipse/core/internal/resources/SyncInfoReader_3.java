package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;

import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.*;

/**
 * This class is used to read sync info from disk. This is the implementation
 * for reading files with version number 3.
 */
public class SyncInfoReader_3 extends SyncInfoReader {
	
	// for sync info
	public static final byte INDEX = 1;
	public static final byte QNAME = 2;

public SyncInfoReader_3(Workspace workspace, Synchronizer synchronizer) {
	super(workspace, synchronizer);
}
/**
 * SAVE_FILE -> VERSION_ID RESOURCE+
 * VERSION_ID -> int
 * RESOURCE -> RESOURCE_PATH SIZE SYNCINFO*
 * RESOURCE_PATH -> String
 * SIZE -> int
 * SYNCINFO -> TYPE BYTES
 * TYPE -> INDEX | QNAME
 * INDEX -> byte int
 * QNAME -> byte String
 * BYTES -> byte[]
 * 
 */ 
public void readSyncInfo(DataInputStream input) throws IOException, CoreException {
	try {
		List readPartners = new ArrayList(5);
		while (true) {
			IPath path = new Path(input.readUTF());
			readSyncInfo(path, input, readPartners);
		}
	} catch (EOFException e) {
		// ignore end of file
	}
}
private void readSyncInfo(IPath path, DataInputStream input, List readPartners) throws IOException, CoreException {
	int size = input.readInt();
	HashMap table = new HashMap(size);
	for (int i = 0; i < size; i++) {
		QualifiedName name = null;
		byte type = input.readByte();
		switch (type) {
			case QNAME :
				String qualifier = input.readUTF();
				String local = input.readUTF();
				name = new QualifiedName(qualifier, local);
				readPartners.add(name);
				break;
			case INDEX :
				name = (QualifiedName) readPartners.get(input.readInt());
				break;
			default :
				//if we get here then the sync info file is corrupt
				String msg = Policy.bind("resources.readSync", path == null ? "" : path.toString());
				throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, path, msg, null);
		}
		// read the bytes
		int length = input.readInt();
		byte[] bytes = new byte[length];
		input.readFully(bytes);
		// put them in the table
		table.put(name, bytes);
	}
	// set the table on the resource info
	ResourceInfo info = workspace.getResourceInfo(path, true, false);
	if (info == null)
		return;
	info.setSyncInfo(table);
	info.clear(ICoreConstants.M_SYNCINFO_SNAP_DIRTY);
}
}

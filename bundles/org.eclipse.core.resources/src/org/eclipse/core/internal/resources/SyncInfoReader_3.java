package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.internal.utils.Assert;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.*;

/**
 * This class is used to read sync info from disk. This is the implementation
 * for reading files with version number 3.
 */
public class SyncInfoReader_3 extends SyncInfoReader {
	
	// version number
	public static final int SYNCINFO_VERSION = 3;

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
public void readSyncInfo(DataInputStream input) throws IOException {
	try {
		List readPartners = new ArrayList(5);
		while (true) {
			IPath path = new Path(input.readUTF());
			Resource resource = (Resource) workspace.getRoot().findMember(path, true);
			readSyncInfo(resource, input, readPartners);
			if (resource == null)
				return;
		}
	} catch (EOFException e) {
		// ignore end of file
	}
}
private void readSyncInfo(Resource resource, DataInputStream input, List readPartners) throws IOException {
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
				Assert.isTrue(false, "Errors restoring sync info for resource.");
		}
		// read the bytes
		int length = input.readInt();
		byte[] bytes = new byte[length];
		input.readFully(bytes);
		// put them in the table
		table.put(name, bytes);
	}
	// set the table on the resource info
	ResourceInfo info = resource.getResourceInfo(true, true);
	info.setSyncInfo(table);
}
}

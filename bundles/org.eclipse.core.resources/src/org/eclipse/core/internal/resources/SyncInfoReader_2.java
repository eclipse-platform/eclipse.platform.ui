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
 * This class is used to read sync info from disk. This is the implementation
 * for reading files with version number 2.
 */
public class SyncInfoReader_2 extends SyncInfoReader {
public SyncInfoReader_2(Workspace workspace, Synchronizer synchronizer) {
	super(workspace, synchronizer);
}
public void readSyncInfo(DataInputStream input) throws CoreException {
	final List readPartners = new ArrayList(5);
	try {
		while (input.available() != 0) {
			IPath path = new Path(input.readUTF());
			Resource resource = (Resource) workspace.getRoot().findMember(path, true);
			if (resource == null)
				return;
			readSyncInfo(resource, input, readPartners);
		}
	} catch (IOException e) {
		throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, null, "Failed reading sync info.", e);
	}
}
private void readSyncInfo(Resource resource, DataInputStream input, List readPartners) throws IOException, CoreException {
	int size = input.readInt();
	HashMap table = new HashMap(size);
	for (int i = 0; i < size; i++) {
		QualifiedName name = null;
		int type = input.readInt();
		switch (type) {
			case ICoreConstants.QNAME_CONSTANT :
				String qualifier = input.readUTF();
				String local = input.readUTF();
				name = new QualifiedName(qualifier, local);
				readPartners.add(name);
				break;
			case ICoreConstants.INT_CONSTANT :
				name = (QualifiedName) readPartners.get(input.readInt());
				break;
			default :
				// XXX: assert(false) here or throw a real exception?
				throw new ResourceException(new ResourceStatus(IResourceStatus.INTERNAL_ERROR, resource.getFullPath(), "Errors restoring sync info for resource."));
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

package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * This class is used to read sync info from disk. Subclasses implement
 * version specific reading code.
 */
public class SyncInfoReader {
	protected Workspace workspace;
	protected Synchronizer synchronizer;
public SyncInfoReader(Workspace workspace, Synchronizer synchronizer) {
	super();
	this.workspace = workspace;
	this.synchronizer = synchronizer;
}
/**
 * Returns the appropriate reader for the given version.
 */
protected SyncInfoReader getReader(int formatVersion) throws IOException {
	switch (formatVersion) {
		case 2 :
			return new SyncInfoReader_2(workspace, synchronizer);
		default :
			throw new IOException("Unknown format");
	}
}
public void readPartners(DataInputStream input) throws CoreException {
	try {
		int size = input.readInt();
		Set registry = new HashSet(size);
		for (int i = 0; i < size; i++) {
			String qualifier = input.readUTF();
			String local = input.readUTF();
			registry.add(new QualifiedName(qualifier, local));
		}
		synchronizer.setRegistry(registry);
	} catch (IOException e) {
		throw new ResourceException(new ResourceStatus(IResourceStatus.INTERNAL_ERROR, "Errors reading sync info file: " + e.toString()));
	}
}
public void readSyncInfo(DataInputStream input) throws CoreException {
	try {
		// dispatch to the appropriate reader depending
		// on the version of the file
		int formatVersion = readVersionNumber(input);
		SyncInfoReader reader = getReader(formatVersion);
		reader.readSyncInfo(input);
	} catch (IOException e) {
		throw new ResourceException(new ResourceStatus(IResourceStatus.ERROR, null, "Error reading sync info file.", e));
	}
}
protected static int readVersionNumber(DataInputStream input) throws IOException {
	return input.readInt();
}
}

package org.eclipse.core.internal.resources;
/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.resources.IResourceStatus;
import java.io.IOException;
import java.io.DataInputStream;

public class MarkerSnapshotReader {
	protected Workspace workspace;
public MarkerSnapshotReader(Workspace workspace) {
	super();
	this.workspace = workspace;
}
/**
 * Returns the appropriate reader for the given version.
 */
protected MarkerSnapshotReader getReader(int formatVersion) throws IOException {
	switch (formatVersion) {
		case 1 :
			return new MarkerSnapshotReader_1(workspace);
		default :
			throw new IOException("Unknown format");
	}
}
public void read(DataInputStream input) throws IOException {
	int formatVersion = readVersionNumber(input);
	MarkerSnapshotReader reader = getReader(formatVersion);
	reader.read(input);
}
protected static int readVersionNumber(DataInputStream input) throws IOException {
	return input.readInt();
}
}

package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * This class is used to read markers from disk. Subclasses implement
 * version specific reading code.
 */
public class MarkerReader {
	protected Workspace workspace;
public MarkerReader(Workspace workspace) {
	super();
	this.workspace = workspace;
}
/**
 * Returns the appropriate reader for the given version.
 */
protected MarkerReader getReader(int formatVersion) throws IOException {
	switch (formatVersion) {
		case 1 :
			return new MarkerReader_1(workspace);
		default :
			throw new IOException("Unknown format");
	}
}
public void read(DataInputStream input, boolean generateDeltas) throws CoreException {
	try {
		int formatVersion = readVersionNumber(input);
		MarkerReader reader = getReader(formatVersion);
		reader.read(input, generateDeltas);
	} catch (IOException e) {
		throw new ResourceException(new ResourceStatus(IResourceStatus.ERROR, null, "Error while reading marker file.", e));
	}
}
protected static int readVersionNumber(DataInputStream input) throws IOException {
	return input.readInt();
}
}

package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.IOException;

import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.runtime.CoreException;

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
		case 2 :
			return new MarkerReader_2(workspace);
		default :
			throw new IOException(Policy.bind("resources.format"));
	}
}
public void read(DataInputStream input, boolean generateDeltas) throws IOException, CoreException {
	int formatVersion = readVersionNumber(input);
	MarkerReader reader = getReader(formatVersion);
	reader.read(input, generateDeltas);
}
protected static int readVersionNumber(DataInputStream input) throws IOException {
	return input.readInt();
}
}

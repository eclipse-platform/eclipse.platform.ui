/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.internal.utils.Policy;
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
			throw new IOException(Policy.bind("resources.format")); //$NON-NLS-1$
	}
}
public void read(DataInputStream input) throws IOException, CoreException {
	int formatVersion = readVersionNumber(input);
	MarkerSnapshotReader reader = getReader(formatVersion);
	reader.read(input);
}
protected static int readVersionNumber(DataInputStream input) throws IOException {
	return input.readInt();
}
}

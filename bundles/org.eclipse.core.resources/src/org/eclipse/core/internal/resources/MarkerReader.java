/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

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
		case 3 :
			return new MarkerReader_3(workspace);
		default :
			throw new IOException(Policy.bind("resources.format")); //$NON-NLS-1$
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

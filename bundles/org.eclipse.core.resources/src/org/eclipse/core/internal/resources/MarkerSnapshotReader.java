/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.DataInputStream;
import java.io.IOException;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;

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
			case 2 :
				return new MarkerSnapshotReader_2(workspace);
			default :
				throw new IOException(NLS.bind(Messages.resources_format, new Integer(formatVersion)));
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

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

public class SyncInfoSnapReader {
	protected Workspace workspace;
	protected Synchronizer synchronizer;

	public SyncInfoSnapReader(Workspace workspace, Synchronizer synchronizer) {
		super();
		this.workspace = workspace;
		this.synchronizer = synchronizer;
	}

	/**
	 * Returns the appropriate reader for the given version.
	 */
	protected SyncInfoSnapReader getReader(int formatVersion) throws IOException {
		switch (formatVersion) {
			case 3 :
				return new SyncInfoSnapReader_3(workspace, synchronizer);
			default :
				throw new IOException(Messages.resources_format);
		}
	}

	public void readSyncInfo(DataInputStream input) throws IOException {
		// dispatch to the appropriate reader depending
		// on the version of the file
		int formatVersion = readVersionNumber(input);
		SyncInfoSnapReader reader = getReader(formatVersion);
		reader.readSyncInfo(input);
	}

	protected static int readVersionNumber(DataInputStream input) throws IOException {
		return input.readInt();
	}
}

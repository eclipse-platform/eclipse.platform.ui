/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.metadata;

import java.io.*;
import org.eclipse.core.internal.localstore.SafeChunkyInputStream;

/**
 * A dumper for .syncinfo.snap files.
 * 
 * @see org.eclipse.core.tools.metadata.SyncInfoSnapshotDumpingStrategy_3
 */
public class SyncInfoSnapshotDumper extends AbstractDumper {

	static final byte INDEX = 1;
	static final byte QNAME = 2;

	/**
	 * @see org.eclipse.core.tools.metadata.AbstractDumper#getStringDumpingStrategy(DataInputStream)
	 */
	protected IStringDumpingStrategy getStringDumpingStrategy(DataInputStream dataInput) throws Exception {
		int versionId = dataInput.readInt();
		if (versionId != 3)
			throw new DumpException("Unknown sync info's snapshot file version: " + versionId); //$NON-NLS-1$
		return new SyncInfoSnapshotDumpingStrategy_3();
	}

	/**
	 * @see org.eclipse.core.tools.metadata.AbstractDumper#openInputStream(File)
	 */
	protected InputStream openInputStream(File file) throws IOException {
		return new SafeChunkyInputStream(file);
	}
}
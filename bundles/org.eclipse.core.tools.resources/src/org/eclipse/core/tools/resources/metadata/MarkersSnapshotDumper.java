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
package org.eclipse.core.tools.resources.metadata;

import java.io.*;
import org.eclipse.core.internal.localstore.SafeChunkyInputStream;
import org.eclipse.core.tools.metadata.*;

/**
 * A dumper for .markers.snap files.
 * 
 * @see org.eclipse.core.tools.resources.metadata.AbstractDumper
 * @see org.eclipse.core.tools.resources.metadata.MarkersSnapshotDumpingStrategy_1
 * @see org.eclipse.core.tools.resources.metadata.MarkersSnapshotDumpingStrategy_2 
 */
public class MarkersSnapshotDumper extends MultiStrategyDumper {

	// type constants
	static final byte INDEX = 1;
	static final byte QNAME = 2;

	// marker attribute types
	static final byte ATTRIBUTE_NULL = 0;
	static final byte ATTRIBUTE_BOOLEAN = 1;
	static final byte ATTRIBUTE_INTEGER = 2;
	static final byte ATTRIBUTE_STRING = 3;

	/**
	 * @see org.eclipse.core.tools.resources.metadata.AbstractDumper#getStringDumpingStrategy(java.io.DataInputStream)
	 */
	protected IStringDumpingStrategy getStringDumpingStrategy(DataInputStream dataInput) throws Exception {
		int versionId;
		IStringDumpingStrategy strategy;
		versionId = dataInput.readInt();
		switch (versionId) {
			case 1 :
				strategy = new MarkersSnapshotDumpingStrategy_1();
				break;
			case 2 :
				strategy = new MarkersSnapshotDumpingStrategy_2();
				break;
			default :
				throw new DumpException("Unknown markers snapshot file version: " + versionId); //$NON-NLS-1$
		}
		return strategy;
	}

	/**
	 * @see org.eclipse.core.tools.resources.metadata.AbstractDumper#openInputStream(java.io.File)
	 */
	protected InputStream openInputStream(File file) throws IOException {
		return new SafeChunkyInputStream(file);
	}

}
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

import java.io.DataInputStream;
import org.eclipse.core.tools.metadata.*;

/**
 * A dumper for .syncinfo files.
 *
 * @see org.eclipse.core.tools.resources.metadata.AbstractDumper 
 * @see org.eclipse.core.tools.resources.metadata.SyncInfoDumpingStrategy_2
 * @see org.eclipse.core.tools.resources.metadata.SyncInfoDumpingStrategy_3
 */
public class SyncInfoDumper extends MultiStrategyDumper {

	// type constants
	static final byte INDEX = 1;
	static final byte QNAME = 2;

	/**
	 * @see org.eclipse.core.tools.resources.metadata.AbstractDumper#getStringDumpingStrategy(java.io.DataInputStream)
	 */
	protected IStringDumpingStrategy getStringDumpingStrategy(DataInputStream dataInput) throws Exception {
		int versionId = dataInput.readInt();
		IStringDumpingStrategy strategy;
		switch (versionId) {
			case 2 :
				strategy = new SyncInfoDumpingStrategy_2();
				break;
			case 3 :
				strategy = new SyncInfoDumpingStrategy_3();
				break;
			default :
				throw new DumpException("Unknown sync info file version: " + versionId); //$NON-NLS-1$
		}
		return strategy;
	}
}
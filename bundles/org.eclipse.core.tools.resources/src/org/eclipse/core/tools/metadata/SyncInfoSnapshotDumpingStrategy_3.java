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

import java.io.DataInputStream;
import java.io.IOException;
import org.eclipse.core.tools.ByteUtil;

/**
 * A strategy for reading .syncinfo.snap files version 3. Layout:
 * <pre>
 * SNAP_FILE -> [VERSION_ID RESOURCE]*
 * VERSION_ID -> int
 * RESOURCE -> RESOURCE_PATH SIZE SYNCINFO*
 * RESOURCE_PATH -> String
 * SIZE -> int
 * SYNCINFO -> QNAME BYTES
 * QNAME -> String String
 * BYTES -> byte[]
 * </pre>
 */
class SyncInfoSnapshotDumpingStrategy_3 implements IStringDumpingStrategy {
	/**
	 * @see org.eclipse.core.tools.metadata.IStringDumpingStrategy#dumpStringContents(DataInputStream)
	 */
	public String dumpStringContents(DataInputStream dataInput) throws Exception {
		StringBuffer contents = new StringBuffer();
		String resourceName = dataInput.readUTF();

		contents.append("Resource: "); //$NON-NLS-1$
		contents.append(resourceName);
		contents.append('\n');
		dumpReadPartners(dataInput, contents);
		contents.append('\n');
		return contents.toString();
	}

	private void dumpReadPartners(DataInputStream input, StringBuffer contents) throws DumpException, IOException {
		int size = input.readInt();
		for (int i = 0; i < size; i++) {
			String qualifier = input.readUTF();
			String localName = input.readUTF();

			String qualifiedName = (qualifier.length() > 0) ? (qualifier + ":" + localName) : localName; //$NON-NLS-1$

			contents.append("Read Partner: "); //$NON-NLS-1$
			contents.append(qualifiedName);
			contents.append('\n');

			// read the bytes - what to do with them???
			int length = input.readInt();
			byte[] bytes = new byte[length];
			input.readFully(bytes);
			// just show the first ones
			contents.append("Bytes ("); //$NON-NLS-1$
			contents.append(length);
			contents.append("): "); //$NON-NLS-1$
			contents.append(ByteUtil.byteArrayToString(bytes, 64));
			contents.append('\n');
		}
	}

	/**
	 * @see org.eclipse.core.tools.metadata.IStringDumpingStrategy#getFormatDescription()
	 */
	public String getFormatDescription() {
		return "Sync info snapshot file version 3"; //$NON-NLS-1$
	}

}
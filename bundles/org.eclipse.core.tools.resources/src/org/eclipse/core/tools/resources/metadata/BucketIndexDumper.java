/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools.resources.metadata;

import java.io.*;
import org.eclipse.core.internal.localstore.HistoryBucket;
import org.eclipse.core.internal.localstore.HistoryBucket.HistoryEntry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tools.metadata.AbstractDumper;

public class BucketIndexDumper extends AbstractDumper {

	protected void dumpContents(PushbackInputStream input, StringBuffer contents) throws IOException {
		DataInputStream source = new DataInputStream(input);
		try {
			int version = source.readByte();
			contents.append("Version: "); //$NON-NLS-1$
			contents.append(Integer.toHexString(version));
			contents.append('\n');
			if (version != HistoryBucket.VERSION) {
				contents.append("Version does not match. Expected: "); //$NON-NLS-1$
				contents.append(Integer.toHexString(HistoryBucket.VERSION));
				return;
			}
			int entryCount = source.readInt();
			contents.append("#entries: "); //$NON-NLS-1$
			contents.append(entryCount);
			for (int i = 0; i < entryCount; i++) {
				String key = source.readUTF();
				contents.append("\nPath: ");
				contents.append(key);
				int length = source.readUnsignedShort();
				byte[][] uuids = new byte[length][HistoryEntry.DATA_LENGTH];
				for (int j = 0; j < uuids.length; j++)
					source.read(uuids[j]);
				HistoryBucket.HistoryEntry entry = new HistoryEntry(new Path(key), uuids);
				for (int j = 0; j < entry.getOccurrences(); j++) {
					contents.append("\n\t");
					contents.append("uuid: ");
					contents.append(entry.getUUID(j));
					contents.append(" - timestamp: ");
					contents.append(entry.getTimestamp(j));
				}
				contents.append('\n');				
			}
		} finally {
			source.close();
		}
	}

}

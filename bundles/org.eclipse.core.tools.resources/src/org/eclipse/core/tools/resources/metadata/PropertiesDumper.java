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

import java.io.File;
import org.eclipse.core.internal.indexing.*;
import org.eclipse.core.internal.properties.StoreKey;
import org.eclipse.core.tools.ByteUtil;
import org.eclipse.core.tools.metadata.*;

/**
 * A dumper for .properties files.
 */
public class PropertiesDumper implements IDumper {

	/**
	 * Dumps a properties file. This implementation uses core internal classes for
	 * reading properties files.
	 * 
	 * @param file the file to be dumped
	 * @return a dump object representing the contents of the file dumped
	 * @see org.eclipse.core.tools.resources.metadata.IDumper#dump(java.io.File)
	 */
	public IDump dump(File file) {
		Dump dump = new Dump();
		dump.setFile(file);
		StringBuffer contents = new StringBuffer();
		IndexedStore store = null;
		try {
			store = new IndexedStore();
			store.open(file.getAbsolutePath());
			IndexCursor cursor = store.getIndex("index").open(); //$NON-NLS-1$
			cursor.findFirstEntry();
			StoreKey key;
			String value;
			contents.append("Keys are ( <qualifier> , <local-name> , <resource-name> )\n"); //$NON-NLS-1$
			for (int i = 0; !cursor.isAtEnd(); i++) {
				key = new StoreKey(cursor.getKey());
				contents.append("\nKey #"); //$NON-NLS-1$
				contents.append(i + 1);
				contents.append(" ( "); //$NON-NLS-1$
				contents.append(key.getQualifier());
				contents.append(" , "); //$NON-NLS-1$
				contents.append(key.getLocalName());
				contents.append(" , "); //$NON-NLS-1$
				contents.append(key.getResourceName());
				contents.append(" )"); //$NON-NLS-1$
				contents.append('\n');
				contents.append("Value: "); //$NON-NLS-1$
				value = ByteUtil.byteArrayToString(cursor.getValue());
				contents.append(value);
				contents.append("\n"); //$NON-NLS-1$
				cursor.next();
			}
		} catch (Exception e) {
			dump.setFailureReason(e);
		} finally {
			// closing the store will close the cursor too.
			try {
				if (store != null)
					store.close();
			} catch (IndexedStoreException ise) {
				if (!dump.isFailed())
					dump.setFailureReason(ise);
			}
		}
		dump.setContents(contents.toString());
		return dump;
	}

}
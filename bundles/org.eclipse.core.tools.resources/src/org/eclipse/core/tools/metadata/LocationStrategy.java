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

/**
 * A strategy for reading .location files. 
 */
class LocationStrategy implements IStringDumpingStrategy {

	/**
	 * @see org.eclipse.core.tools.metadata.IStringDumpingStrategy#dumpStringContents(DataInputStream)
	 */
	public String dumpStringContents(DataInputStream dataInput) throws DumpException, IOException {
		StringBuffer contents = new StringBuffer(100);
		String location = dataInput.readUTF();
		contents.append("Location: '"); //$NON-NLS-1$
		contents.append(location);
		contents.append('\''); //$NON-NLS-1$		
		//try to read the dynamic references
		int numRefs = dataInput.readInt();
		if (numRefs < 0)
			return contents.toString();
		contents.append('\n');
		contents.append("Dynamic references ("); //$NON-NLS-1$
		contents.append(numRefs);
		contents.append("): "); //$NON-NLS-1$
		for (int i = 0; i < numRefs; i++) {
			String projectName = dataInput.readUTF();
			contents.append("\n\t"); //$NON-NLS-1$
			contents.append(projectName);
		}
		return contents.toString();
	}

	/**
	 * @see org.eclipse.core.tools.metadata.IStringDumpingStrategy#getFormatDescription()
	 */
	public String getFormatDescription() {
		return "Project location file"; //$NON-NLS-1$
	}
}
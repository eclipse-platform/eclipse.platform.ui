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

/**
 * A strategy for reading .location files. 
 */
class LocationStrategy implements IStringDumpingStrategy {

	/**
	 * @see org.eclipse.core.tools.metadata.IStringDumpingStrategy#dumpStringContents(DataInputStream)
	 */
	public String dumpStringContents(DataInputStream dataInput) throws Exception {
		return "Location: " + dataInput.readUTF(); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.core.tools.metadata.IStringDumpingStrategy#getFormatDescription()
	 */
	public String getFormatDescription() {
		return "Project location file"; //$NON-NLS-1$
	}
}
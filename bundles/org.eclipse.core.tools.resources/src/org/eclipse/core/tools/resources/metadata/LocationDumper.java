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
import org.eclipse.core.tools.metadata.AbstractDumper;
import org.eclipse.core.tools.metadata.IStringDumpingStrategy;

/**
 * A dumper for .location files.
 *  
 * @see org.eclipse.core.tools.resources.metadata.AbstractDumper
 * @see org.eclipse.core.tools.resources.metadata.LocationStrategy  
 */
public class LocationDumper extends AbstractDumper {

	/**
	 * @see org.eclipse.core.tools.resources.metadata.AbstractDumper#getStringDumpingStrategy(java.io.DataInputStream)
	 */
	protected IStringDumpingStrategy getStringDumpingStrategy(DataInputStream input) throws Exception {
		return new LocationStrategy();
	}

	/**
	 * @see org.eclipse.core.tools.resources.metadata.AbstractDumper#openInputStream(java.io.File)
	 */
	protected InputStream openInputStream(File file) throws IOException {
		return new SafeChunkyInputStream(file);
	}

}
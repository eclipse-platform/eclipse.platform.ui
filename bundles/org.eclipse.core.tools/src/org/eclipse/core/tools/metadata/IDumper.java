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

import java.io.File;

/**
 * A dumper is an object that knows how to process a given kind of file,
 * translating it to a human-readable form.
 */
public interface IDumper {

	/** 
	 * Reads a given file and produces a dump object. Any exception that 
	 * may occur during file processing must be caught and stored as 
	 * failure reason in the IDump object returned.
	 * 
	 * @param file the file to be dumped
	 * @return a dump object representing the contents of the file dumped
	 */
	IDump dump(File file);
}
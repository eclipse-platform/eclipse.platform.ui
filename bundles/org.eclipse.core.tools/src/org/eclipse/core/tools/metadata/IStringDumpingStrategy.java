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
 * A dumping strategy defines how a input stream will be dumped as a string.
 * A given dumper may use different strategies for reading different versions of 
 * a file it understands, or may even use different strategies for reading 
 * different segments of a same file.
 * 
 * @see org.eclipse.core.tools.metadata.AbstractDumper
 */
public interface IStringDumpingStrategy {
	/**
	 * Dumps contents read from the provided stream.
	 * Concrete implementations should not catch any exceptions. 
	 * Concrete implementations should not close the input stream.
	 * This method may read the entire input stream contents, or just part of it.
	 * 
	 * @param input the input stream where to dump contents from 
	 * @return the contents read in string format
	 * @throws Exception any exception occurred while dumping the input stream 
	 */
	public String dumpStringContents(DataInputStream input) throws IOException, DumpException;

	/**
	 * Returns a high-level description for the file format understood by this 
	 * strategy.
	 * 
	 * @return a string describing the file format this strategy understands.
	 */
	public String getFormatDescription();

}
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
 * Represents an object produced by dumping a given file. 
 */
public interface IDump {

	/**
	 * Returns the file processed in order to produce this dump.
	 * 
	 * @return the file processed
	 */
	File getFile();

	/**
	 * Returns the dump contents. Dump contents objects must have a 
	 * <code>toString()</code>
	 * implementation that returns meaningful, human-readable, representation for 
	 * the contents.
	 * 
	 * @return an object representing the contents of the dumped file.
	 */
	Object getContents();

	/**
	 * Returns true if the file reading process ended due to a failure. If 
	 * <code>getFailureReason()</code> does not return <code>null</code>, this 
	 * method must return <code>true</code>.
	 * 
	 * @return true if the dumping process terminated due to a failure
	 */
	boolean isFailed();

	/**
	 * Returns the exception that caused the failure, or null if a failure did not 
	 * happen.
	 * 
	 * @return the exception that caused the failure, or null if a 
	 * failure did not happen.
	 */
	Exception getFailureReason();

	/**
	 * Returns the offset where dumper stopped reading the dumped file. Optionally 
	 * implemented.
	 * 
	 * @return the number of bytes read
	 */
	long getOffset();

}
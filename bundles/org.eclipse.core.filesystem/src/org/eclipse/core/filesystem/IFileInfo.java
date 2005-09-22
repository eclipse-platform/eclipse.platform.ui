/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.filesystem;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A file info is a simple structure holding information about a file or directory.
 * The information contained here is static; changes to this object will
 * not cause corresponding changes to any file on disk, and changes to files
 * on disk are not reflected in this object. At best, an IFileInfo represents a snapshot
 * of the state of a file at a particular moment in time.
 * 
 * @see IFileStore#fetchInfo(int, IProgressMonitor)
 * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
 * @since 3.2
 */
public interface IFileInfo extends Comparable {

	/**
	 * Returns whether this file or directory exists.
	 * 
	 * @return <code>true</code> if this file exists, and <code>false</code>
	 * otherwise.
	 */
	public abstract boolean exists();

	/**
	 * Returns the value of the specified attribute for this file.  The attribute
	 * must be one of the <code>IFileStoreConstants#ATTRIBUTE_*</code>
	 * constants.
	 * 
	 * @param attribute The attribute to retrieve the value for
	 * @return the value of the specified attribute for this file.
	 */
	public boolean getAttribute(int attribute);

	/**
	 * Returns the last modified time for this file, or {@link IFileStoreConstants#INVALID}
	 * if the file does not exist or the last modified time could not be computed.
	 * 
	 * @return the last modified time for this file, or {@link IFileStoreConstants#INVALID}
	 */
	public abstract long getLastModified();

	/**
	 * Returns the length of this file, or {@link IFileStoreConstants#INVALID}
	 * if the file does not exist, is a directory, or the length could not be computed.
	 * 
	 * @return the length of this file, or {@link IFileStoreConstants#INVALID}
	 */
	public abstract long getLength();

	/**
	 * Returns the name of this file.
	 * 
	 * @return the name of this file.
	 */
	public abstract String getName();

	/**
	 * Returns whether this file is a directory.  This is a convenience method,
	 * fully equivalent to <code>getAttribute(ATTRIBUTE_DIRECTORY)</code>
	 * 
	 * @return <code>true</code> if this file is a directory, and <code>false</code>
	 * otherwise.
	 */
	public abstract boolean isDirectory();

	/**
	 * Returns whether this file is read only.  This is a convenience method,
	 * fully equivalent to <code>getAttribute(ATTRIBUTE_READ_ONLY)</code>
	 * 
	 * @return <code>true</code> if this file is read only, and <code>false</code>
	 * otherwise.
	 */
	public abstract boolean isReadOnly();

	/**
	 * Sets the value of the specified attribute for this file.  The attribute
	 * must be one of the <code>IFileStoreConstants#ATTRIBUTE_*</code>
	 * constants.
	 * 
	 * @param attribute The attribute to set the value for
	 * @param value the value of the specified attribute for this file.
	 */
	public void setAttribute(int attribute, boolean value);
	
	/**
	 * Sets whether this file or directory exists.
	 * 
	 * @param value <code>true</code> if this file exists, and <code>false</code>
	 * otherwise.
	 */
	public void setExists(boolean value);

	/**
	 * Sets the last modified time for this file.  A value of {@link IFileStoreConstants#INVALID}
	 * indicates the file does not exist or the last modified time could not be computed.
	 * 
	 * @param time the last modified time for this file, or {@link IFileStoreConstants#INVALID}
	 */
	public abstract void setLastModified(long time);

	/**
	 * Sets the length of this file. A value of {@link IFileStoreConstants#INVALID}
	 * indicates the file does not exist, is a directory, or the length could not be computed.
	 * 
	 * @param length the length of this file, or {@link IFileStoreConstants#INVALID}
	 */
	public abstract void setLength(long length);
	
	/**
	 * Sets the name of this file.
	 * 
	 * @param name The file name
	 */
	public void setName(String name);
}
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
 * <p>
 * This interface is not intended to be implemented by clients.  File store
 * implementations should use the concrete class {@link org.eclipse.core.filesystem.provider.FileStore}
 * </p>
 * 
 * @see IFileStore#fetchInfo(int, IProgressMonitor)
 * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
 * @since org.eclipse.core.filesystem 1.0
 */
public interface IFileInfo extends Comparable, Cloneable {

	/**
	 * Returns whether this file or directory exists.
	 * 
	 * @return <code>true</code> if this file exists, and <code>false</code>
	 * otherwise.
	 */
	public abstract boolean exists();

	/**
	 * Returns the value of the specified attribute for this file.  The attribute
	 * must be one of the <code>EFS#ATTRIBUTE_*</code>
	 * constants. Returns <code>false</code> if this file does not exist,
	 * could not be accessed, or the provided attribute does not apply to this
	 * file system.
	 * 
	 * @param attribute The attribute to retrieve the value for
	 * @return the value of the specified attribute for this file.
	 * @see IFileSystem#attributes()
	 */
	public abstract boolean getAttribute(int attribute);

	/**
	 * Returns the last modified time for this file, or {@link EFS#NONE}
	 * if the file does not exist or the last modified time could not be computed.
	 * <p>
	 * The time format is the POSIX encoding of UTC, except the returned value
	 * is in milliseconds rather than seconds.  Thus the returned value is:
	 * 86,400,000 * days since (00:00:00 GMT, January 1, 1970) + number of UTC 
	 * milliseconds since the beginning of that day (often incorrectly specified as the 
	 * number of milliseconds since the epoch, which does not account for the 
	 * insertion and removal of leap seconds).
	 * </p>
	 * 
	 * @return the last modified time for this file, or {@link EFS#NONE}
	 */
	public abstract long getLastModified();

	/**
	 * Returns the length of this file, or {@link EFS#NONE}
	 * if the file does not exist, is a directory, or the length could not be computed.
	 * 
	 * @return the length of this file, or {@link EFS#NONE}
	 */
	public abstract long getLength();

	/**
	 * Returns the name of this file.
	 * 
	 * @return the name of this file.
	 */
	public abstract String getName();

	/**
	 * Returns whether this file is a directory, or <code>false</code> if this
	 * file does not exist.
	 * 
	 * @return <code>true</code> if this file is a directory, and <code>false</code>
	 * otherwise.
	 */
	public abstract boolean isDirectory();

	/**
	 * Sets the value of the specified attribute for this file info.  The attribute
	 * must be one of the <code>EFS#ATTRIBUTE_*</code> constants.  
	 * Note that not all attributes are applicable in a given file system.
	 * </p>
	 * <p>
	 * Users must call {@link IFileStore#putInfo(IFileInfo, int, IProgressMonitor)}
	 * before changes made to this info take effect in an underlying file.
	 * </p>
	 * 
	 * @param attribute The attribute to set the value for
	 * @param value the value of the specified attribute for this file.
	 * @see IFileSystem#attributes()
	 */
	public abstract void setAttribute(int attribute, boolean value);

	/**
	 * Sets the last modified time for this file.  A value of {@link EFS#NONE}
	 * indicates the file does not exist or the last modified time could not be computed.
	 * <p>
	 * Users must call {@link IFileStore#putInfo(IFileInfo, int, IProgressMonitor)}
	 * before changes made to this info take effect in an underlying file.
	 * </p>
	 * 
	 * @param time the last modified time for this file, or {@link EFS#NONE}
	 */
	public abstract void setLastModified(long time);
}
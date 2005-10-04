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

import java.net.URI;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

/**
 * This is the main interface to a single file system.  Each file system instance
 * manages interaction with all files in the backing store represented by a 
 * particular URI scheme.
 * <p>
 * File systems are registered using the "filesystems" extension point.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.  File system
 * implementations must subclass {@link FileSystem} rather than implementing
 * this interface directly.
 * </p>
 * 
 * @see FileSystemCore#getFileSystem(String)
 * @since 1.0
 */
public interface IFileSystem extends IAdaptable {

	/**
	 * Returns the file attributes supported by this file system.  This value
	 * is a bit mask of the <code>IFileStoreConstants.ATTRIBUTE_</code>
	 * constants.  All file systems are assumed to support the attribute
	 * <code>IFileStoreConstants.ATTRIBUTE_DIRECTORY</code>, so
	 * this attribute does not need to be specified here.
	 * 
	 * @return the file attributes supported by this file system.
	 */
	public int attributes();

	/**
	 * Returns whether this file system supports deletion
	 * 
	 * @return <code>true</code> if this file system allows deletion
	 * of files and directories, and <code>false</code> otherwise.
	 */
	public boolean canDelete();

	/**
	 * Returns whether this file system supports modification.
	 * 
	 * @return <code>true</code> if this file system allows modification
	 * of files and directories, and <code>false</code> otherwise.
	 */
	public boolean canWrite();

	/**
	 * Returns the URI scheme of this file system.
	 * 
	 * @return the URI scheme of this file system.
	 */
	public String getScheme();
	
	/**
	 * Returns a handle to a file store in this file system.  This method succeeds
	 * regardless of whether a file exists at that path in this file system.
	 * <p>
	 * This is a convenience method for file systems that do not make use
	 * of the authority {@link java.net.URI} component, such as a host or user 
	 * information. The provided path argument is interpreted as the path component 
	 * of the file system's {@link java.net.URI}.
	 * </p>
	 * 
	 * @param path A path to a file store within the scheme of this file system.
	 * @return A handle to a file store in this file system
	 */
	public IFileStore getStore(IPath path);

	/**
	 * Returns a handle to a file store in this file system.  This method succeeds
	 * regardless of whether a file exists at that path in this file system.
	 * 
	 * @param uri The URI of the file store to return.
	 * @return A handle to a file store in this file system
	 */
	public IFileStore getStore(URI uri);

	/**
	 * Returns whether this file system is case sensitive.  A case sensitive
	 * file system treats files with names that differ only in case as different
	 * files. For example, "HELLO", "Hello", and "hello" would be three different
	 * files or directories in a case sensitive file system.
	 * 
	 * @return <code>true</code> if this file system is case sensitive, and
	 * <code>false</code> otherwise.
	 */
	public boolean isCaseSensitive();

}
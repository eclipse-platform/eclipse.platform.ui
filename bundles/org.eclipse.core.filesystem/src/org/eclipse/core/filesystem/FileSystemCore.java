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
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.internal.filesystem.InternalFileSystemCore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class is the main entry point for clients of the file system API.  This
 * class has factory methods for obtaining instances of file systems and file
 * stores.
 * <p>
 * This class is not intended to be instantiated or subclassed.
 * </p>
 * @since 1.0
 */
public class FileSystemCore {

	/**
	 * Creates an empty file information object.  The resulting information
	 * will represent a non-existent file with no name and no attributes set.
	 * 
	 * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
	 * @return an empty file information object.
	 */
	public static IFileInfo createFileInfo() {
		return new FileInfo();
	}

	/**
	 * Returns a file system corresponding to the given scheme.
	 * 
	 * @param scheme The file system URI scheme
	 * @return The corresponding file system for the given scheme
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>There is no registered file system for the given URI scheme.</li>
	 * <li>There was a failure initializing the file system.</li>
	 * </ul>
	 */
	public static IFileSystem getFileSystem(String scheme) throws CoreException {
		return InternalFileSystemCore.getInstance().getFileSystem(scheme);
	}
	
	/**
	 * Returns the local file system.
	 * 
	 * @return The local file system
	 */
	public static IFileSystem getLocalFileSystem() {
		return InternalFileSystemCore.getInstance().getLocalFileSystem();
	}
	
	/**
	 * Returns the null file system.  The null file system can be used
	 * to represent a non-existent or unresolved file system. An example
	 * of a null file system is a file system whose location is relative to an undefined 
	 * variable, or a system whose scheme is unknown.
	 * <p>
	 * Basic handle-based queries can be performed on the null file system, but all 
	 * operations that actually require file system access will fail.
	 * 
	 * @return The null file system
	 */
	public static IFileSystem getNullFileSystem() {
		return InternalFileSystemCore.getInstance().getNullFileSystem();
	}
	
	/**
	 * Returns the file store corresponding to the provided URI.
	 * 
	 * @param uri The URI of the file store to return
	 * @return The file store
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>There is no registered file system for the given URI scheme.</li>
	 * <li>The URI syntax was not in the appropriate form for that scheme.</li>
	 * <li>There was a failure initializing the file system.</li>
	 * </ul>
	 */
	public static IFileStore getStore(URI uri) throws CoreException {
		return InternalFileSystemCore.getInstance().getStore(uri);
	}

	/**
	 * This class is not intended to be instantiated.
	 */
	private FileSystemCore() {
		super();
	}
}
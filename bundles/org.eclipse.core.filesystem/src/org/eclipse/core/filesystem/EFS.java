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
 * This class is the main entry point for clients of the Eclipse file system API.  This
 * class has factory methods for obtaining instances of file systems and file
 * stores, and provides constants for option values and error codes.
 * <p>
 * This class is not intended to be instantiated or subclassed.
 * </p>
 * @since 1.0
 */
public class EFS {
	/**
	 * The unique identifier constant (value "<code>org.eclipse.core.filesystem</code>")
	 * of the Core file system plug-in.
	 */
	public static final String PI_FILE_SYSTEM = "org.eclipse.core.filesystem"; //$NON-NLS-1$

	/** 
	 * The simple identifier constant (value "<code>filesystems</code>") of
	 * the extension point of the Core file system plug-in where plug-ins declare
	 * file system implementations.
	 */
	public static final String PT_FILE_SYSTEMS = "filesystems"; //$NON-NLS-1$

	/**
	 * A constant known to be zero (0), used in operations which
	 * take bit flags to indicate that "no bits are set".  This value is
	 * also used as a default value in cases where a file system attribute
	 * cannot be computed.
	 * 
	 * @see IFileInfo#getLength()
	 * @see IFileInfo#getLastModified()
	 */
	public static final int NONE = 0;

	/**
	 * Option flag constant (value 1 &lt;&lt;0) indicating a file opened
	 * for appending data to the end.
	 * 
	 * @see IFileStore#openOutputStream(int, IProgressMonitor)
	 */
	public static final int APPEND = 1 << 0;
	
	/**
	 * Option flag constant (value 1 &lt;&lt;1) indicating that existing
	 * files may be overwritten.
	 * 
	 * @see IFileStore#copy(IFileStore, int, IProgressMonitor)
	 * @see IFileStore#move(IFileStore, int, IProgressMonitor)
	 */
	public static final int OVERWRITE = 1 << 1;
	
	/**
	 * Option flag constant (value 1 &lt;&lt;2) indicating that an
	 * operation acts on a single file or directory, and not its parents
	 * or children.
	 * 
	 * @see IFileStore#copy(IFileStore, int, IProgressMonitor)
	 * @see IFileStore#mkdir(int, IProgressMonitor)
	 */
	public static final int SHALLOW = 1 << 2;

	/**
	 * Option flag constant (value 1 &lt;&lt;10) indicating that a
	 * file's attributes should be updated.
	 * 
	 * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
	 */
	public static final int SET_ATTRIBUTES = 1 << 10;

	/**
	 * Option flag constant (value 1 &lt;&lt;11) indicating that a
	 * file's last modified time should be updated.
	 * 
	 * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
	 */
	public static final int SET_LAST_MODIFIED = 1 << 11;

	/**
	 * @deprecated To be removed before I20051018
	 */
	public static final int ATTRIBUTE_DIRECTORY= 1 << 0;

	//note that "1 << 0" is reserved as the directory attribute that is not API
	
	/**
	 * Attribute constant (value 1 &lt;&lt;1) indicating that a
	 * file is read only.
	 * 
	 * @see IFileStore#fetchInfo()
	 * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
	 * @see IFileInfo#getAttribute(int)
	 * @see IFileInfo#setAttribute(int, boolean)
	 */
	public static final int ATTRIBUTE_READ_ONLY = 1 << 1;

	/**
	 * Attribute constant (value 1 &lt;&lt;2) indicating that a
	 * file is a executable.
	 * 
	 * @see IFileStore#fetchInfo()
	 * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
	 * @see IFileInfo#getAttribute(int)
	 * @see IFileInfo#setAttribute(int, boolean)
	 */
	public static final int ATTRIBUTE_EXECUTABLE = 1 << 2;
	
	/**
	 * Attribute constant (value 1 &lt;&lt;3) indicating that a
	 * file is an archive.
	 * 
	 * @see IFileStore#fetchInfo()
	 * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
	 * @see IFileInfo#getAttribute(int)
	 * @see IFileInfo#setAttribute(int, boolean)
	 */
	public static final int ATTRIBUTE_ARCHIVE = 1 << 3;

	/**
	 * Attribute constant (value 1 &lt;&lt;4) indicating that a
	 * file is hidden.
	 * 
	 * @see IFileStore#fetchInfo()
	 * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
	 * @see IFileInfo#getAttribute(int)
	 * @see IFileInfo#setAttribute(int, boolean)
	 */
	public static final int ATTRIBUTE_HIDDEN = 1 << 4;

	/**
	 * Attribute constant (value 1 &lt;&lt;5) indicating that a
	 * file is a symbolic link.
	 * 
	 * @see IFileStore#fetchInfo()
	 * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
	 * @see IFileInfo#getAttribute(int)
	 * @see IFileInfo#setAttribute(int, boolean)
	 */
	public static final int ATTRIBUTE_LINK = 1 << 5;

	/**
	 * Scheme constant (value "file") indicating the local file system scheme.
	 * @see EFS#getLocalFileSystem()
	 */
	public static final String SCHEME_FILE = "file"; //$NON-NLS-1$

	
	/**
	 * Scheme constant (value "null") indicating the null file system scheme.
	 * @see EFS#getNullFileSystem()
	 */
	public static final String SCHEME_NULL = "null"; //$NON-NLS-1$

	/*
	 * Status code definitions
	 */
	// Errors [266-298]

	/** Status code constant (value 268) indicating a store unexpectedly 
	 * exists on the file system.
	 * Severity: error. Category: file system.
	 */
	public static final int ERROR_EXISTS = 268;

	/** Status code constant (value 269) indicating a store unexpectedly 
	 * does not exist on the file system.
	 * Severity: error. Category: file system.
	 */
	public static final int ERROR_NOT_EXISTS = 269;

	/** Status code constant (value 270) indicating the file system location for
	 * a store could not be computed. 
	 * Severity: error. Category: file system.
	 */
	public static final int ERROR_NO_LOCATION = 270;

	/** Status code constant (value 271) indicating an error occurred while
	 * reading from the file system.
	 * Severity: error. Category: file system.
	 */
	public static final int ERROR_READ = 271;

	/** Status code constant (value 272) indicating an error occurred while
	 * writing to the file system.
	 * Severity: error. Category: file system.
	 */
	public static final int ERROR_WRITE = 272;

	/** Status code constant (value 273) indicating an error occurred while
	 * deleting from the file system.
	 * Severity: error. Category: file system.
	 */
	public static final int ERROR_DELETE = 273;

	/** Status code constant (value 275) indicating this file system is not case
	 * sensitive and a file that differs only in case unexpectedly exists on 
	 * the file system.
	 * Severity: error. Category: file system.
	 */
	public static final int ERROR_CASE_VARIANT_EXISTS = 275;

	/** Status code constant (value 276) indicating a file exists in the
	 * file system but is not of the expected type (file instead of directory,
	 * or vice-versa).
	 * Severity: error. Category: file system.
	 */
	public static final int ERROR_WRONG_TYPE = 276;

	/** Status code constant (value 277) indicating that the parent
	 * file in the file system is marked as read-only.
	 * Severity: error. Category: file system.
	 */
	public static final int ERROR_PARENT_READ_ONLY = 277;

	/** Status code constant (value 279) indicating that the 
	 * file in the file system is marked as read-only.
	 * Severity: error. Category: file system.
	 */
	public static final int ERROR_READ_ONLY = 279;

	/** Status code constant (value 566) indicating an error internal has occurred.
	 * Severity: error. Category: internal.
	 */
	public static final int ERROR_INTERNAL = 566;
	
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
	private EFS() {
		super();
	}
}
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
 * This interface supplies constants used by API classes in the file system plugin.
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @since 3.2
 */
public interface IFileStoreConstants {

	/**
	 * The unique identifier constant (value "<code>org.eclipse.core.filesystem</code>")
	 * of the Core file system plug-in.
	 */
	public static final String PI_FILE_SYSTEM = "org.eclipse.core.filesystem"; //$NON-NLS-1$

	/** 
	 * The simple identifier constant (value "<code>filesystems</code>") of
	 * the extension point of the Core file system plug-in where plug-ins declare
	 * file system implementations.
	 * @todo Call this extension point "providers" rather than "filesystems"??
	 */
	public static final String PT_FILE_SYSTEMS = "filesystems"; //$NON-NLS-1$

	/**
	 * A constant (value -1) representing an invalid value.
	 * 
	 * @see IFileInfo#getLastModified()
	 * @see IFileInfo#getLength()
	 */
	public static final int INVALID = -1;
	
	/**
	 * A constant known to be zero (0), used in operations which
	 * take bit flags to indicate that "no bits are set".
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
	 * Attribute constant (value 1 &lt;&lt;0) indicating that a
	 * file is a directory.
	 * 
	 * @see IFileStore#fetchInfo()
	 * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
	 * @see IFileInfo#getAttribute(int)
	 * @see IFileInfo#setAttribute(int, boolean)
	 */
	public static final int ATTRIBUTE_DIRECTORY= 1 << 0;

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
	 * Scheme constant (value "file") indicating the local file system scheme.
	 * @see FileSystemCore#getLocalFileSystem()
	 */
	public static final String SCHEME_FILE = "file"; //$NON-NLS-1$

	
	/**
	 * Scheme constant (value "null") indicating the null file system scheme.
	 * @see FileSystemCore#getNullFileSystem()
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
	public static final int EXISTS_LOCAL = 268;

	/** Status code constant (value 269) indicating a store unexpectedly 
	 * does not exist on the file system.
	 * Severity: error. Category: file system.
	 */
	public static final int NOT_FOUND_LOCAL = 269;

	/** Status code constant (value 270) indicating the file system location for
	 * a store could not be computed. 
	 * Severity: error. Category: file system.
	 */
	public static final int NO_LOCATION_LOCAL = 270;

	/** Status code constant (value 271) indicating an error occurred while
	 * reading from the file system.
	 * Severity: error. Category: file system.
	 */
	public static final int FAILED_READ_LOCAL = 271;

	/** Status code constant (value 272) indicating an error occurred while
	 * writing to the file system.
	 * Severity: error. Category: file system.
	 */
	public static final int FAILED_WRITE_LOCAL = 272;

	/** Status code constant (value 273) indicating an error occurred while
	 * deleting from the file system.
	 * Severity: error. Category: file system.
	 */
	public static final int FAILED_DELETE_LOCAL = 273;

	/** Status code constant (value 275) indicating this file system is not case
	 * sensitive and a file that differs only in case unexpectedly exists on 
	 * the file system.
	 * Severity: error. Category: file system.
	 */
	public static final int CASE_VARIANT_EXISTS = 275;

	/** Status code constant (value 276) indicating a file exists in the
	 * file system but is not of the expected type (file instead of directory,
	 * or vice-versa).
	 * Severity: error. Category: file system.
	 */
	public static final int WRONG_TYPE_LOCAL = 276;

	/** Status code constant (value 277) indicating that the parent
	 * file in the file system is marked as read-only.
	 * Severity: error. Category: file system.
	 */
	public static final int PARENT_READ_ONLY = 277;

	/** Status code constant (value 279) indicating that the 
	 * file in the file system is marked as read-only.
	 * Severity: error. Category: file system.
	 */
	public static final int READ_ONLY_LOCAL = 279;

	/** Status code constant (value 566) indicating an error internal has occurred.
	 * Severity: error. Category: internal.
	 */
	public static final int INTERNAL_ERROR = 566;
}

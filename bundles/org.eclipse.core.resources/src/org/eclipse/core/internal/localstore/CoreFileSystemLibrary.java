/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Incorporated - get/setResourceAttribute code
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import java.io.File;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.internal.resources.ResourceStatus;
import org.eclipse.core.internal.utils.Convert;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public abstract class CoreFileSystemLibrary {

	/** Indicates whether or not this FS is case sensitive */
	private static final boolean caseSensitive = new File("a").compareTo(new File("A")) != 0; //$NON-NLS-1$ //$NON-NLS-2$
	private static boolean hasNatives = false;
	private static boolean isUnicode = false;

	/** instance of this library */
	// The name convention is to get the plugin version at the time
	// the library is changed.  
	private static final String LIBRARY_NAME = "core_3_1_0"; //$NON-NLS-1$

	private static boolean loggedFailedGetAttributes = false;

	/**
	 * The following masks are used to represent the bits
	 * returned by the getStat() and internalGetStat() methods.
	 * The idea is to save JNI calls. So internalGetStat() is a native
	 * that grabs as much important information as it can and puts it in
	 * a long variable.
	 * The lower bits represent the last modified timestamp of the
	 * given file and the higher bits represent some relevant flags.
	 */

	/** indicates if the resource is a folder or a file */
	private static final long STAT_FOLDER = 0x2000000000000000l;
	/** indicates if the resource is marked as read-only */
	private static final long STAT_READ_ONLY = 0x1000000000000000l;
	/** reserved, should not be used */
	private static final long STAT_RESERVED = 0x8000000000000000l;
	/** indicates if this is a valid stat or some problem happened when 
	 retrieving the information */
	private static final long STAT_VALID = 0x4000000000000000l;
	/** used to extract the last modified timestamp */
	private static final long STAT_LASTMODIFIED = ~(STAT_RESERVED | STAT_VALID | STAT_FOLDER | STAT_READ_ONLY);

	static {
		try {
			System.loadLibrary(LIBRARY_NAME);
			hasNatives = true;
			isUnicode = internalIsUnicode();
		} catch (UnsatisfiedLinkError e) {
			logMissingNativeLibrary(e);
		}
	}

	/**
	 * Copies file attributes from source to destination. The copyLastModified attribute
	 * indicates whether the lastModified attribute should be copied.
	 */
	public static boolean copyAttributes(String source, String destination, boolean copyLastModified) {
		if (hasNatives)
			// Note that support for copying last modified info is not implemented on Windows
			return isUnicode ? internalCopyAttributesW(source.toCharArray(), destination.toCharArray(), copyLastModified) : internalCopyAttributes(Convert.toPlatformBytes(source), Convert.toPlatformBytes(destination), copyLastModified);
		return false; // not supported
	}

	public static long getLastModified(long stat) {
		return (stat & STAT_LASTMODIFIED);
	}

	public static long getLastModified(String fileName) {
		if (hasNatives)
			return getLastModified(getStat(fileName));

		// inlined (no native) implementation
		return new File(fileName).lastModified();
	}

	public static ResourceAttributes getResourceAttributes(String fileName) {
		try {
			ResourceAttributes attributes = new ResourceAttributes();
			if (!hasNatives) {
				//non-native implementation
				attributes.setReadOnly(isReadOnly(fileName));
				return attributes;
			}
			//ensure we return null on failure
			if (isUnicode ? internalGetResourceAttributesW(fileName.toCharArray(), attributes) : internalGetResourceAttributes(Convert.toPlatformBytes(fileName), attributes))
				return attributes;
		} catch (UnsatisfiedLinkError e) {
			if (!loggedFailedGetAttributes) {
				loggedFailedGetAttributes = true;
				String message = Policy.bind("resources.getResourceAttributesFailed", fileName); //$NON-NLS-1$
				ResourceStatus status = new ResourceStatus(IStatus.INFO, new Path(fileName), message);
				ResourcesPlugin.getPlugin().getLog().log(status);
			}
		}
		return null;
	}

	public static long getStat(String fileName) {
		if (hasNatives)
			return isUnicode ? internalGetStatW(fileName.toCharArray()) : internalGetStat(Convert.toPlatformBytes(fileName));

		// inlined (no native) implementation
		File target = new File(fileName);
		long result = target.lastModified();
		if (result == 0) // non-existing
			return result;
		result |= STAT_VALID;
		if (target.isDirectory())
			result |= STAT_FOLDER;
		if (!(new File(fileName).canWrite()))
			result |= STAT_READ_ONLY;
		return result;
	}

	/**
	 * Copies file attributes from source to destination. The copyLastModified attribute
	 * indicates whether the lastModified attribute should be copied.
	 */
	private static final native boolean internalCopyAttributes(byte[] source, byte[] destination, boolean copyLastModified);

	/**
	 * Copies file attributes from source to destination. The copyLastModified attribute
	 * indicates whether the lastModified attribute should be copied (Unicode
	 * version - should not be called if <code>isUnicode</code> is
	 * <code>false</code>).
	 */
	private static final native boolean internalCopyAttributesW(char[] source, char[] destination, boolean copyLastModified);

	/** Put the extended attributes that the platform supports in the IResource attributes object. Attributes
	 * that are not supported by the platform will not be set and will remain the default value (<code>false</code>). */
	private static final native boolean internalGetResourceAttributes(byte[] fileName, ResourceAttributes attribute);

	/** Put the extended attributes that the platform supports in the IResource attributes object. Attributes
	 * that are not supported by the platform will not be set and will remain null (the default). 
	 * (Unicode version - should not be called if <code>isUnicode</code> is <code>false</code>). */
	private static final native boolean internalGetResourceAttributesW(char[] fileName, ResourceAttributes attribute);

	/**
	 * Returns the stat information for the specified filename in a long (64 bits). We just
	 * retrieve the stat information we consider necessary and store everything in one long
	 * to save some JNI calls (standard version)
	 */
	private static final native long internalGetStat(byte[] fileName);

	/**
	 * Returns the stat information for the specified filename in a long (64 bits). We just
	 * retrieve the stat information we consider necessary and store everything in one long
	 * to save some JNI calls (Unicode version - should not be called if <code>isUnicode</code>
	 * is <code>false</code>).
	 */
	private static final native long internalGetStatW(char[] fileName);

	/**
	 * Returns <code>true</code> if the underlying file system API supports Unicode,
	 * <code>false</code> otherwise.
	 */
	private static final native boolean internalIsUnicode();

	/** Set the extended attributes specified in the IResource attribute. Only attributes 
	 * that the platform supports will be set. */
	private static final native boolean internalSetResourceAttributes(byte[] fileName, ResourceAttributes attribute);

	/** Set the extended attributes specified in the IResource attribute object. Only 
	 * attributes that the platform supports will be set. (Unicode version - should not 
	 * be called if <code>isUnicode</code> is <code>false</code>). */
	private static final native boolean internalSetResourceAttributesW(char[] fileName, ResourceAttributes attribute);

	public static boolean isCaseSensitive() {
		return caseSensitive;
	}

	public static boolean isFile(long stat) {
		return isSet(stat, STAT_VALID) && !isSet(stat, STAT_FOLDER);
	}

	public static boolean isFolder(long stat) {
		return isSet(stat, STAT_VALID) && isSet(stat, STAT_FOLDER);
	}

	public static boolean isReadOnly(String fileName) {
		// Use the same implementation whether or not we are using
		// the natives. If the file doesn't exist then getStat() will return 0
		// and this method will return false.
		return isSet(getStat(fileName), STAT_READ_ONLY);
	}

	public static boolean isReadOnly(long stat) {
		return isSet(stat, STAT_READ_ONLY);
	}

	private static boolean isSet(long stat, long mask) {
		return (stat & mask) != 0;
	}

	private static void logMissingNativeLibrary(UnsatisfiedLinkError e) {
		String libName = System.mapLibraryName(LIBRARY_NAME);
		String message = Policy.bind("localstore.couldNotLoadLibrary", libName); //$NON-NLS-1$
		ResourceStatus status = new ResourceStatus(IStatus.INFO, null, message, null);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

	public static boolean setReadOnly(String fileName, boolean readonly) {
		ResourceAttributes attributes = getResourceAttributes(fileName);
		if (attributes == null)
			return false;
		attributes.setReadOnly(readonly);
		try {
			setResourceAttributes(fileName, attributes);
		} catch (CoreException e) {
			//spec of setReadOnly doesn't throw exceptions on failure
			return false;
		}
		return true;
	}

	public static void setResourceAttributes(String fileName, ResourceAttributes attributes) throws CoreException {
		if (!hasNatives) {
			//do nothing if there are no natives
			return;
		}
		if (isUnicode ? internalSetResourceAttributesW(fileName.toCharArray(), attributes) : internalSetResourceAttributes(Convert.toPlatformBytes(fileName), attributes))
			return;
		String message = Policy.bind("resources.setResourceAttributesFailed", fileName); //$NON-NLS-1$
		throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, new Path(fileName), message, null);
	}

	/**
	 * Return <code>true</code> if we have found the core library and are using it for
	 * our file-system calls, and <code>false</code> otherwise.
	 */
	public static boolean usingNatives() {
		return hasNatives;
	}
}
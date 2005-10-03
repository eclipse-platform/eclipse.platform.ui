/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filesystem.local;

import java.io.File;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.internal.filesystem.Messages;
import org.eclipse.core.internal.filesystem.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

abstract class LocalFileNatives {

	/** Indicates whether or not this FS is case sensitive */
	private static final boolean caseSensitive = Platform.OS_MACOSX.equals(Platform.getOS()) ? false : new File("a").compareTo(new File("A")) != 0; //$NON-NLS-1$ //$NON-NLS-2$
	private static boolean hasNatives = false;
	private static boolean isUnicode = false;

	/** instance of this library */
	// The name convention is to use the plugin version at the time the library is changed.  
	private static final String LIBRARY_NAME = "localfile_1_0_0"; //$NON-NLS-1$

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

	public static FileInfo fetchFileInfo(String fileName) {
		FileInfo info = new FileInfo();
		if (isUnicode)
			internalGetFileInfoW(fileName.toCharArray(), info);
		else
			internalGetFileInfo(Convert.toPlatformBytes(fileName), info);
		return info;
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

	/**
	 * Stores the file information for the specified filename in the supplied file
	 * information object.  This avoids multiple JNI calls.
	 */
	private static final native boolean internalGetFileInfo(byte[] fileName, IFileInfo info);

	/**
	 * Stores the file information for the specified filename in the supplied file
	 * information object.  This avoids multiple JNI calls.
	 */
	private static final native boolean internalGetFileInfoW(char[] fileName, IFileInfo info);

	/**
	 * Returns <code>true</code> if the underlying file system API supports Unicode,
	 * <code>false</code> otherwise.
	 */
	private static final native boolean internalIsUnicode();

	/** Set the extended attributes specified in the IResource attribute. Only attributes 
	 * that the platform supports will be set. */
	private static final native boolean internalSetFileInfo(byte[] fileName, IFileInfo attribute, int options);

	/** Set the extended attributes specified in the IResource attribute object. Only 
	 * attributes that the platform supports will be set. (Unicode version - should not 
	 * be called if <code>isUnicode</code> is <code>false</code>). */
	private static final native boolean internalSetFileInfoW(char[] fileName, IFileInfo attribute, int options);

	public static boolean isCaseSensitive() {
		return caseSensitive;
	}

	private static void logMissingNativeLibrary(UnsatisfiedLinkError e) {
		String libName = System.mapLibraryName(LIBRARY_NAME);
		String message = NLS.bind(Messages.couldNotLoadLibrary, libName);
		Policy.log(IStatus.INFO, message);
	}

	public static void setFileInfo(String fileName, IFileInfo info, int options) {
		if (isUnicode)
			internalSetFileInfoW(fileName.toCharArray(), info, options);
		else
			internalSetFileInfo(Convert.toPlatformBytes(fileName), info, options);
	}

	/**
	 * Return <code>true</code> if we have found the core library and are using it for
	 * our file-system calls, and <code>false</code> otherwise.
	 */
	public static boolean usingNatives() {
		return hasNatives;
	}
}

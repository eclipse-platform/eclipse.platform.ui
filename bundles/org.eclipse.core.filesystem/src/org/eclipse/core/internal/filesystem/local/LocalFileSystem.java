/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import java.net.URI;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

/**
 * File system provider for the "file" scheme.  This file system provides access to
 * the local file system that is available via java.io.File.
 * 
 * @since 3.2
 */
public class LocalFileSystem extends FileSystem {

	private static final boolean caseSensitive = Platform.OS_MACOSX.equals(Platform.getOS()) ? false : new java.io.File("a").compareTo(new java.io.File("A")) != 0; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The singleton instance of this file system.
	 */
	private static IFileSystem instance;

	/**
	 * Returns the instance of this file system
	 * 
	 * @return The instance of this file system.
	 */
	public static IFileSystem getInstance() {
		return instance;
	}

	/**
	 * Creates a new local file system.
	 */
	public LocalFileSystem() {
		super();
		instance = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileSystem#attributes()
	 */
	public int attributes() {
		int attributes = EFS.ATTRIBUTE_READ_ONLY;
		//intern so we can compare with constants using identity
		String os = Platform.getOS().intern();
		if (os == Platform.OS_WIN32)
			attributes |= EFS.ATTRIBUTE_ARCHIVE | EFS.ATTRIBUTE_HIDDEN;
		else if (os == Platform.OS_LINUX || os == Platform.OS_MACOSX)
			attributes |= EFS.ATTRIBUTE_EXECUTABLE;
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileSystem#canDelete()
	 */
	public boolean canDelete() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileSystem#canWrite()
	 */
	public boolean canWrite() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileSystem#getStore(org.eclipse.core.runtime.IPath)
	 */
	public IFileStore getStore(IPath path) {
		return new LocalFile(path.toFile());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileSystem#getStore(java.net.URI)
	 */
	public IFileStore getStore(URI uri) {
		return new LocalFile(new File(uri.getSchemeSpecificPart()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileSystem#isCaseSensitive()
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}
}
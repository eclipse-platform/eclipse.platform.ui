/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.launching;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.Path;

/**
 * A simple in memory file system to test launch configurations in EFS
 */
public class DebugFileSystem extends FileSystem {

	/**
	 * represents a directory
	 */
	public static final byte[] DIRECTORY_BYTES = new byte[] {1, 2, 3, 4};

	private static DebugFileSystem system;

	/**
	 * Keys URIs to file stores for existing files
	 */
	private final Map<URI, byte[]> files = new HashMap<URI, byte[]>();

	/**
	 * Constructs the singleton
	 */
	public DebugFileSystem() {
		system = this;
		// create root of the file system
		try {
			setContents(new URI("debug", Path.ROOT.toString(), null), DIRECTORY_BYTES); //$NON-NLS-1$
		} catch (URISyntaxException e) {}
	}

	/**
	 * Returns the Debug files system.
	 *
	 * @return file system
	 */
	static DebugFileSystem getDefault() {
		return system;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileSystem#getStore(java.net.URI)
	 */
	@Override
	public IFileStore getStore(URI uri) {
		return new DebugFileStore(uri);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileSystem#canDelete()
	 */
	@Override
	public boolean canDelete() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileSystem#canWrite()
	 */
	@Override
	public boolean canWrite() {
		return true;
	}

	/**
	 * Returns whether contents of the file or <code>null</code> if none.
	 *
	 * @param uri
	 * @return bytes or <code>null</code>
	 */
	public byte[] getContents(URI uri) {
		return files.get(uri);
	}

	/**
	 * Deletes the file.
	 *
	 * @param uri
	 */
	public void delete(URI uri) {
		files.remove(uri);
	}

	/**
	 * Sets the content of the given file.
	 *
	 * @param uri
	 * @param bytes
	 */
	public void setContents(URI uri, byte[] bytes) {
		files.put(uri, bytes);
	}

	/**
	 * Returns URIs of all existing files.
	 *
	 * @return
	 */
	public URI[] getFileURIs() {
		return files.keySet().toArray(new URI[files.size()]);
	}

}

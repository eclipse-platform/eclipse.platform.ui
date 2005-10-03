/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.filesystem;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.*;

/**
 * A null file store represents a file whose location is unknown,
 * such as a location based on an undefined variable.  Basic
 * handle queries can be performed on this class, but all operations that actually
 * require file system access will fail.
 */
public class NullFileStore extends FileStore {
	private IPath path;

	/**
	 * Creates a null file store
	 * @param path The path of the file in this store
	 */
	public NullFileStore(IPath path) {
		this.path = path;
	}

	public String[] childNames(int options, IProgressMonitor monitor) {
		return EMPTY_STRING_ARRAY;
	}

	public void delete(int options, IProgressMonitor monitor) throws CoreException {
		fail();
	}

	private void fail() throws CoreException {
		//TODO real message
		Policy.error(IStatus.ERROR, toString());
	}

	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) {
		FileInfo result = new FileInfo(getName());
		result.setExists(false);
		return result;
	}

	public IFileStore getChild(String name) {
		return new NullFileStore(path.append(name));
	}

	public IFileSystem getFileSystem() {
		return NullFileSystem.getInstance();
	}

	public String getName() {
		return String.valueOf(path.lastSegment());
	}

	public IFileStore getParent() {
		return path.segmentCount() == 0 ? null : new NullFileStore(path.removeLastSegments(1));
	}

	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		fail();
		return null;
	}

	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		fail();
		return null;
	}

	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		fail();
		return null;
	}

	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		fail();
	}

	public String toString() {
		return path.toString();
	}

	public URI toURI() {
		try {
			return new URI("null", null, path.toString(), null); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			//should not happen
			throw new Error(e);
		}
	}
}

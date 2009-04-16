/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filesystem;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.*;

/**
 * A null file store represents a file whose location is unknown,
 * such as a location based on an undefined variable.  This store
 * acts much like /dev/null on *nix: writes are ignored, reads return
 * empty streams, and modifications such as delete, mkdir, will fail.
 */
public class NullFileStore extends FileStore {
	private IPath path;

	/**
	 * Creates a null file store
	 * @param path The path of the file in this store
	 */
	public NullFileStore(IPath path) {
		Assert.isNotNull(path);
		this.path = path;
	}

	public IFileInfo[] childInfos(int options, IProgressMonitor monitor) {
		return EMPTY_FILE_INFO_ARRAY;
	}

	public String[] childNames(int options, IProgressMonitor monitor) {
		return EMPTY_STRING_ARRAY;
	}

	public void delete(int options, IProgressMonitor monitor) throws CoreException {
		//super implementation will always fail
		super.delete(options, monitor);
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
		//super implementation will always fail
		return super.mkdir(options, monitor);
	}

	public InputStream openInputStream(int options, IProgressMonitor monitor) {
		return new ByteArrayInputStream(new byte[0]);
	}

	public OutputStream openOutputStream(int options, IProgressMonitor monitor) {
		return new OutputStream() {
			public void write(int b) {
				//do nothing
			}
		};
	}

	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		//super implementation will always fail
		super.putInfo(info, options, monitor);
	}

	public String toString() {
		return path.toString();
	}

	public URI toURI() {
		try {
			return new URI(EFS.SCHEME_NULL, null, path.isEmpty() ? "/" : path.toString(), null); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			//should never happen
			Policy.log(IStatus.ERROR, "Invalid URI", e); //$NON-NLS-1$
			return null;
		}
	}
}

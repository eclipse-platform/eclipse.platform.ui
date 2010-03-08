/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     James Blackburn - Fix for bug 266712
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.InputStream;
import java.net.URI;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.*;

/**
 * A file store representing a virtual resource. 
 * A virtual resource always exists and has no children.
 */
public class VirtualFileStore extends FileStore {
	private final URI location;

	public VirtualFileStore(URI location) {
		this.location = location;
	}

	public String[] childNames(int options, IProgressMonitor monitor) {
		return FileStore.EMPTY_STRING_ARRAY;
	}

	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) {
		FileInfo result = new FileInfo();
		result.setDirectory(true);
		result.setExists(true);
		result.setLastModified(1);//last modified of zero indicates non-existence
		return result;
	}

	public void delete(int options, IProgressMonitor monitor) {
		//nothing to do - virtual resources don't exist in any physical file system
	}

	public IFileStore getChild(String name) {
		return EFS.getNullFileSystem().getStore(new Path(name).makeAbsolute());
	}

	public String getName() {
		return "virtual"; //$NON-NLS-1$
	}

	public IFileStore getParent() {
		return null;
	}

	public void move(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
		destination.mkdir(EFS.NONE, monitor);
	}

	public InputStream openInputStream(int options, IProgressMonitor monitor) {
		return null;
	}

	public URI toURI() {
		return location;
	}
}

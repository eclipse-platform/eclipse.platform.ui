/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.filesystem.bogus;

import java.io.File;
import java.net.URI;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.internal.filesystem.ram.MemoryFileStore;

/**
 * 
 */
public class BogusFileStore extends MemoryFileStore {

	public BogusFileStore(IPath path) {
		super(path);
	}

	public URI toURI() {
		return BogusFileSystem.toURI(path);
	}

	public java.io.File toLocalFile(int options, IProgressMonitor monitor) throws CoreException {
		IPath parentPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append("bogus_fs");
		File parent = new java.io.File(parentPath.toOSString());
		if (!parent.exists())
			parent.mkdirs();
		return new java.io.File(parentPath.append(getName()).toOSString());
	}
}

/*******************************************************************************
 *  Copyright (c) 2006, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.filesystem.bogus;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.internal.filesystem.ram.MemoryFileSystem;

/**
 * A simple file system implementation that maps to a local file system location.
 */
public class BogusFileSystem extends MemoryFileSystem {

	public static final String SCHEME_BOGUS = "bogus";

	private static BogusFileSystem instance;

	public static BogusFileSystem getInstance() {
		BogusFileSystem tmpInstance = instance;
		if (tmpInstance != null)
			return tmpInstance;
		return instance = new BogusFileSystem();
	}

	public static URI toURI(IPath path) {
		try {
			return new URI(BogusFileSystem.SCHEME_BOGUS, path.setDevice(null).toPortableString(), null);
		} catch (URISyntaxException e) {
			//should not happen
			throw new RuntimeException(e);
		}
	}

	public BogusFileSystem() {
		instance = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileSystem#getStore(java.net.URI)
	 */
	public IFileStore getStore(URI uri) {
		return new BogusFileStore(Path.fromPortableString(uri.getSchemeSpecificPart()));
	}
}

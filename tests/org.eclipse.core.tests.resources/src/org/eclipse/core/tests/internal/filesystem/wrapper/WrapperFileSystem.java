/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.filesystem.wrapper;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.CoreTest;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * A simple file system implementation that acts as a wrapper around the
 * local file system.
 */
public class WrapperFileSystem extends FileSystem {

	private static final IFileStore NULL_ROOT = EFS.getNullFileSystem().getStore(Path.ROOT);

	private static final String SCHEME_WRAPPED = "wrapped";

	private static WrapperFileSystem instance;

	public static URI getBasicURI(URI wrappedURI) {
		Assert.isLegal(SCHEME_WRAPPED.equals(wrappedURI.getScheme()));
		return URI.create(wrappedURI.getQuery());
	}

	public static WrapperFileSystem getInstance() {
		WrapperFileSystem tmpInstance = instance;
		if (tmpInstance != null)
			return tmpInstance;
		return instance = new WrapperFileSystem();
	}

	public static URI getWrappedURI(URI baseURI) {
		try {
			return new URI(SCHEME_WRAPPED, null, baseURI.getPath(), baseURI.toString(), null);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.toString());
		}
	}

	public WrapperFileSystem() {
		instance = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileSystem#getStore(java.net.URI)
	 */
	public IFileStore getStore(URI uri) {
		Assert.isLegal(SCHEME_WRAPPED.equals(uri.getScheme()));
		IFileStore baseStore;
		try {
			baseStore = EFS.getStore(getBasicURI(uri));
		} catch (CoreException e) {
			CoreTest.log(ResourceTest.PI_RESOURCES_TESTS, e);
			return NULL_ROOT;
		}
		return new WrapperFileStore(baseStore);
	}
}

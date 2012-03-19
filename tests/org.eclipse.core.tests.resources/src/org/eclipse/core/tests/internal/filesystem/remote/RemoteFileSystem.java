/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.filesystem.remote;

import java.net.URI;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.internal.filesystem.ram.MemoryFileSystem;

/**
 * A test file system that mocks remote file system and keeps everything in memory.
 */
public class RemoteFileSystem extends MemoryFileSystem {
	public static final String SCHEME_REMOTE = "remote";

	public RemoteFileSystem() {
		super();
	}

	public IFileStore getStore(URI uri) {
		return new RemoteFileStore(uri.getUserInfo(), uri.getHost(), uri.getPort(), new Path(uri.getPath()));
	}
}

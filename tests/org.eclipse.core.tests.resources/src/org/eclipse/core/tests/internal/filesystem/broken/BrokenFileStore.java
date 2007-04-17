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
package org.eclipse.core.tests.internal.filesystem.broken;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import junit.framework.Assert;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.*;

/**
 * 
 */
public class BrokenFileStore extends FileStore {

	private URI uri;

	public BrokenFileStore(URI uri) {
		this.uri = uri;
	}

	public BrokenFileStore(IPath path) {
		try {
			uri = new URI(uri.getScheme(), path.toString(), null);
		} catch (URISyntaxException e) {
			Assert.fail(e.getMessage());
		}
	}

	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		fail();
		return null;
	}

	private void fail() throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.core.tests.resources", 1, "This exception is thrown on purpose as part of a test", null));
	}

	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		fail();
		return null;
	}

	private IPath getPath() {
		return new Path(uri.getSchemeSpecificPart());
	}

	public IFileStore getChild(String name) {
		return new BrokenFileStore(getPath().append(name));
	}

	public String getName() {
		return getPath().lastSegment();
	}

	public IFileStore getParent() {
		IPath path = getPath();
		if (path.segmentCount() == 0)
			return null;
		return new BrokenFileStore(path.removeLastSegments(1));
	}

	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		fail();
		return null;
	}

	public URI toURI() {
		return uri;
	}

}

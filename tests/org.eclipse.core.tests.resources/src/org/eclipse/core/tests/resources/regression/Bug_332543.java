/*******************************************************************************
 *  Copyright (c) 2011, 2012 Broadcom Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     James Blackburn (Broadcom Corp.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import java.io.*;
import java.net.URI;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.internal.filesystem.wrapper.WrapperFileStore;
import org.eclipse.core.tests.internal.filesystem.wrapper.WrapperFileSystem;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * This tests that I/O Exception on OuptuStream#close() after IFile#setContents is correctly reported.
 */
public class Bug_332543 extends ResourceTest {

	public static Test suite() {
		return new TestSuite(Bug_332543.class);
	}

	/**
	 * Wrapper FS which throws an IOException when someone
	 * closes an output stream...
	 */
	public static class IOErrOnCloseFileStore extends WrapperFileStore {
		public IOErrOnCloseFileStore(IFileStore store) {
			super(store);
		}

		public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
			OutputStream os = super.openOutputStream(options, monitor);
			os = new BufferedOutputStream(os) {
				public void close() throws java.io.IOException {
					// We close the output stream (so there aren't issues deleting the project during tear-down)
					super.close();
					// But we also throw IOException as if the operation had failed.
					throw new IOException("Whoops I dunno how to close!");
				}
			};
			return os;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		WrapperFileSystem.setCustomFileStore(null);
		super.tearDown();
	}

	public void testBug() throws Exception {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		String proj_name = getUniqueString();
		IPath proj_loc = root.getLocation().append(proj_name);
		URI proj_uri = WrapperFileSystem.getWrappedURI(URIUtil.toURI(proj_loc));

		IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription(proj_name);
		desc.setLocationURI(proj_uri);
		// Create the project on the wrapped file system
		IProject project = root.getProject(desc.getName());
		project.create(desc, getMonitor());

		// Create a file in the project
		IFile f = project.getFile("foo.txt");
		ensureExistsInFileSystem(f);

		// Set our evil IOException on close() fs.
		WrapperFileSystem.setCustomFileStore(IOErrOnCloseFileStore.class);

		// Now open the project
		project.open(getMonitor());

		// Try #setContents on an existing file
		try {
			f.setContents(new ByteArrayInputStream("Random".getBytes()), false, true, getMonitor());
			fail("1.0");
		} catch (CoreException e) {
			// This is expected.
		}

		// Try create on a non-existent file
		f = project.getFile("foo1.txt");
		try {
			f.create(new ByteArrayInputStream("Random".getBytes()), false, getMonitor());
			fail("2.0");
		} catch (CoreException e) {
			// This is expected.
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.tests.resources.ResourceTest;

public class Bug_233939 extends ResourceTest {
	/**
	 * Constructor for Bug_233939.
	 */
	public Bug_233939() {
		super();
	}

	/**
	 * Constructor for Bug_233939.
	 * @param name
	 */
	public Bug_233939(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(Bug_233939.class);
	}

	public void testBug() {
		// only activate this test on platforms that support it
		if (!isAttributeSupported(EFS.ATTRIBUTE_SYMLINK))
			return;
		String fileName = "file.txt";

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(getUniqueString());
		IFile file = project.getFile(fileName);

		IPath fileInTempDirPath = getTempDir().addTrailingSeparator().append(fileName);

		// create a project
		try {
			project.create(getMonitor());
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}

		// create a file in the temp dir
		try {
			fileInTempDirPath.toFile().createNewFile();
		} catch (IOException e) {
			fail("2.0", e);
		}

		// create a link to the file in the temp dir and refresh,
		// the resource in the workspace should have symbolic link attribute set
		createSymLink(project.getLocation().toFile(), fileName, fileInTempDirPath.toString(), false);
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
		assertExistsInWorkspace("4.0", file);
		assertTrue("5.0", file.getResourceAttributes().isSymbolicLink());

		IFile[] files = root.findFilesForLocation(file.getLocation());
		assertEquals("7.0", 1, files.length);
		assertEquals("7.1", file, files[0]);

		//		IFile[] files = root.findFilesForLocation(fileInTempDirPath);
		//		assertEquals("6.0", 1, files.length);
		//		assertEquals("6.1", file, files[0]);
	}

}

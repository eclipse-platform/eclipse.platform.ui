/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.eclipse.osgi.service.environment.Constants;

/**
 * Tests regression of bug 44106. In this case deleting a file which was a
 * symbolic link on Linux actually deleted the files that it pointed too rather
 * than just the link itself.
 */
public class Bug_44106 extends EclipseWorkspaceTest {

	public static Test suite() {
		return new TestSuite(Bug_44106.class);
	}

	public Bug_44106() {
		super();
	}

	public Bug_44106(String name) {
		super(name);
	}

	public void testDeleteLinkedFolder() {
		if (!Platform.getOS().equals(Constants.OS_LINUX))
			return;
		// create the file/folder that we are going to link to
		IPath linkDestFolder = getTempDir().append(getUniqueString());
		IPath linkDestFile = linkDestFolder.append(getUniqueString());
		try {
			createFileInFileSystem(linkDestFile);
		} catch (CoreException e) {
			fail("0.0", e);
		}
		assertTrue("0.1", linkDestFolder.toFile().exists());
		assertTrue("0.2", linkDestFile.toFile().exists());

		// create some resources in the workspace
		IProject project = getWorkspace().getRoot().getProject(getUniqueString());
		ensureExistsInWorkspace(project, true);

		// link in the folder
		String target = linkDestFolder.toOSString();
		IFolder linkedFolder = project.getFolder("linkedFolder");
		IFile linkedFile = linkedFolder.getFile(linkDestFile.lastSegment());
		String local = linkedFolder.getLocation().toOSString();
		try {
			Process p = Runtime.getRuntime().exec("/bin/ln -s " + target + " " + local);
			p.waitFor();
		} catch (IOException e) {
			fail("1.0", e);
		} catch (InterruptedException e) {
			fail("1.1", e);
		}
		assertExistsInFileSystem("1.2", linkedFolder);
		assertExistsInFileSystem("1.3", linkedFile);

		// do a refresh and ensure that the resources are in the workspace
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}
		assertExistsInWorkspace("2.1", linkedFolder);
		assertExistsInWorkspace("2.2", linkedFile);

		// delete the folder
		try {
			linkedFolder.delete(IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}

		// ensure that the folder and file weren't deleted in the filesystem
		assertDoesNotExistInWorkspace("4.0", linkedFolder);
		assertDoesNotExistInWorkspace("4.1", linkedFile);
		assertTrue("4.2", linkDestFolder.toFile().exists());
		assertTrue("4.3", linkDestFile.toFile().exists());

		// cleanup
		linkDestFile.toFile().delete();
		linkDestFolder.toFile().delete();
	}
}
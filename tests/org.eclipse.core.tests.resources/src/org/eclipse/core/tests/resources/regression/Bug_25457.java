/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.io.InputStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tests.resources.ResourceTest;
import org.eclipse.osgi.service.environment.Constants;

/**
 * Tests regression of bug 25457.  In this case, attempting to move a project
 * that is only a case change, where the move fails due to another handle being
 * open on a file in the hierarchy, would cause deletion of the source.
 */
public class Bug_25457 extends ResourceTest {
	public static Test suite() {
		return new TestSuite(Bug_25457.class);
	}

	public Bug_25457() {
		super();
	}

	public Bug_25457(String name) {
		super(name);
	}

	public void testFile() {
		//this test only works on windows
		if (!Platform.getOS().equals(Constants.OS_WIN32))
			return;
		IProject source = getWorkspace().getRoot().getProject("project");
		IFile sourceFile = source.getFile("file.txt");
		IFile destFile = source.getFile("File.txt");
		ensureExistsInWorkspace(source, true);
		ensureExistsInWorkspace(sourceFile, true);

		//open a stream in the source to cause the rename to fail
		InputStream stream = null;
		try {
			try {
				stream = sourceFile.getContents();
			} catch (CoreException e) {
				fail("0.99", e);
			}
			//try to rename the file (should fail)
			try {
				sourceFile.move(destFile.getFullPath(), IResource.NONE, getMonitor());
				fail("1.99");
			} catch (CoreException e1) {
				//should fail
			}
			//ensure source still exists
			assertTrue("2.0", source.exists());
			assertTrue("2.1", sourceFile.exists());

			//ensure destination file does not exist
			assertTrue("2.2", !destFile.exists());

		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					fail("9.99", e);
				}
			}
		}
	}

	public void testFolder() {
		//this test only works on windows
		if (!Platform.getOS().equals(Constants.OS_WIN32))
			return;
		IProject source = getWorkspace().getRoot().getProject("SourceProject");
		IFolder sourceFolder = source.getFolder("folder");
		IFile sourceFile = sourceFolder.getFile("Important.txt");
		IFolder destFolder = source.getFolder("Folder");
		IFile destFile = destFolder.getFile("Important.txt");
		ensureExistsInWorkspace(source, true);
		ensureExistsInWorkspace(sourceFolder, true);
		ensureExistsInWorkspace(sourceFile, true);

		//open a stream in the source to cause the rename to fail
		InputStream stream = null;
		try {
			try {
				stream = sourceFile.getContents();
			} catch (CoreException e) {
				fail("0.99", e);
			}
			//try to rename the project (should fail)
			try {
				sourceFolder.move(destFolder.getFullPath(), IResource.NONE, getMonitor());
				fail("1.99");
			} catch (CoreException e1) {
				//should fail
			}
			//ensure source still exists
			assertTrue("2.0", source.exists());
			assertTrue("2.1", sourceFolder.exists());
			assertTrue("2.2", sourceFile.exists());

			//ensure destination does not exist
			assertTrue("2.3", !destFolder.exists());
			assertTrue("2.4", !destFile.exists());

		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					fail("9.99", e);
				}
			}
		}
	}

	public void testProject() {
		//this test only works on windows
		if (!Platform.getOS().equals(Constants.OS_WIN32))
			return;
		IProject source = getWorkspace().getRoot().getProject("project");
		IProject destination = getWorkspace().getRoot().getProject("Project");
		IFile sourceFile = source.getFile("Important.txt");
		IFile destFile = destination.getFile("Important.txt");
		ensureExistsInWorkspace(source, true);
		ensureExistsInWorkspace(sourceFile, true);

		//open a stream in the source to cause the rename to fail
		InputStream stream = null;
		try {
			try {
				stream = sourceFile.getContents();
			} catch (CoreException e) {
				fail("1.99", e);
			}
			//try to rename the project (should fail)
			try {
				source.move(destination.getFullPath(), IResource.NONE, getMonitor());
				fail("1.99");
			} catch (CoreException e1) {
				//should fail
			}
			//ensure source still exists
			assertTrue("2.0", source.exists());
			assertTrue("2.1", sourceFile.exists());

			//ensure destination does not exist
			assertTrue("2.2", !destination.exists());
			assertTrue("2.3", !destFile.exists());

		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					fail("9.99", e);
				}
			}
		}

	}
}
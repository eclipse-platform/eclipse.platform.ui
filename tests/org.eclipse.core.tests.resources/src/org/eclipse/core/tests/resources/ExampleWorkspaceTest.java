/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.io.InputStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * Tests which use the Eclipse Platform workspace.
 */
public class ExampleWorkspaceTest extends ResourceTest {
	/**
	 * Need a zero argument constructor to satisfy the test harness.
	 * This constructor should not do any real work nor should it be
	 * called by user code.
	 */
	public ExampleWorkspaceTest() {
		super();
	}

	public ExampleWorkspaceTest(String name) {
		super(name);
	}

	protected IProject getTestProject() {
		return getWorkspace().getRoot().getProject("testProject");
	}

	/**
	 * Returns the test suite for this test class.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(ExampleWorkspaceTest.class.getName());
		suite.addTest(new ExampleWorkspaceTest("testProjectCreation"));
		suite.addTest(new ExampleWorkspaceTest("testFolderCreation"));
		suite.addTest(new ExampleWorkspaceTest("testFileCreation"));
		suite.addTest(new ExampleWorkspaceTest("testFileInFolderCreation"));
		suite.addTest(new ExampleWorkspaceTest("testSetContents"));
		suite.addTest(new ExampleWorkspaceTest("testFileOverFolder"));
		suite.addTest(new ExampleWorkspaceTest("testFolderOverFile"));
		suite.addTest(new ExampleWorkspaceTest("testFolderDeletion"));
		suite.addTest(new ExampleWorkspaceTest("testFileDeletion"));
		suite.addTest(new ExampleWorkspaceTest("testProjectDeletion"));
		return suite;
	}

	public void testFileCreation() throws Throwable {
		IFile target = getTestProject().getFile("testFile");
		target.create(null, true, getMonitor());
		assertTrue(target.exists());
	}

	public void testFileDeletion() throws Throwable {
		IFile target = getTestProject().getFile("testFileForDelete");
		target.create(null, true, getMonitor());
		assertTrue(target.exists());
		target.delete(true, getMonitor());
		assertTrue(!target.exists());
	}

	public void testFileInFolderCreation() throws Throwable {
		IFile target = getTestProject().getFile(new Path("testFolder/testFile2"));
		target.create(getRandomContents(), true, getMonitor());
		assertTrue(target.exists());
	}

	public void testFileOverFolder() throws Throwable {
		IFolder existing = getTestProject().getFolder("testFolder");
		assertTrue(existing.exists());
		IFile target = getWorkspace().getRoot().getFile(existing.getFullPath());
		try {
			target.create(null, true, getMonitor());
		} catch (CoreException e) {
			assertTrue(existing.exists());
			return;
		}
		fail("Should not be able to create file over folder");
	}

	public void testFolderCreation() throws Throwable {
		IFolder target = getTestProject().getFolder("testFolder");
		target.create(true, true, getMonitor());
		assertTrue(target.exists());
	}

	public void testFolderDeletion() throws Throwable {
		IProject project = getTestProject();
		IResource[] before = buildResources(project, new String[] {"c/", "c/b/", "c/x", "c/b/y", "c/b/z"});
		ensureExistsInWorkspace(before, true);
		//
		assertExistsInWorkspace(before);
		project.getFolder("c").delete(true, getMonitor());
		assertDoesNotExistInWorkspace(before);
	}

	public void testFolderOverFile() throws Throwable {
		IFile existing = getTestProject().getFile("testFile");
		assertTrue(existing.exists());
		IFolder target = getWorkspace().getRoot().getFolder(existing.getFullPath());
		try {
			target.create(true, true, getMonitor());
		} catch (CoreException e) {
			assertTrue(existing.exists());
			return;
		}
		fail("Should not be able to create folder over a file");
	}

	public void testProjectCreation() throws Throwable {
		IProject target = getTestProject();
		target.create(null, getMonitor());
		assertTrue(target.exists());
		target.open(getMonitor());
		assertTrue(target.isOpen());
	}

	public void testProjectDeletion() throws Throwable {
		IProject target = getTestProject();
		target.delete(true, getMonitor());
		assertTrue("Project Deletion failed", !target.exists());
	}

	public void testSetContents() throws Throwable {
		IFile target = getTestProject().getFile("testFile");
		String testString = getRandomString();
		target.setContents(getContents(testString), true, false, getMonitor());
		InputStream content = null;
		try {
			content = target.getContents();
			assertTrue("get not equal set", compareContent(content, getContents(testString)));
		} finally {
			content.close();
		}
	}
}

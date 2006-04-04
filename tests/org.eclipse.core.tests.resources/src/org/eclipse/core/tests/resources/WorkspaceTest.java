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
import org.eclipse.core.runtime.*;

/**
 * This class should be refactored into the black box tests for
 * solution, project, folder and file.
 */
public class WorkspaceTest extends ResourceTest {
	/**
	 * Need a zero argument constructor to satisfy the test harness.
	 * This constructor should not do any real work nor should it be
	 * called by user code.
	 */
	public WorkspaceTest() {
		super();
	}

	public WorkspaceTest(String name) {
		super(name);
	}

	/**
	 * All of the WorkspaceTests build on each other. This test must
	 * be run last of all to clean up from all previous tests in this class.
	 * @throws Exception
	 */
	public void doCleanup() throws Exception {
		cleanup();
	}

	/**
	 * Returns a collection of string paths describing the standard 
	 * resource hierarchy for this test.  In the string forms, folders are
	 * represented as having trailing separators ('/').  All other resources
	 * are files.  It is generally assumed that this hierarchy will be 
	 * inserted under some solution and project structure.
	 */
	public String[] defineHierarchy() {
		return new String[] {"/", "/1/", "/1/1", "/1/2", "/1/3", "/2/", "/2/1", "/2/2", "/2/3", "/3/", "/3/1", "/3/2", "/3/3", "/4/", "/5"};
	}

	protected IProject getTestProject() {
		return getWorkspace().getRoot().getProject("testProject");
	}

	protected IProject getTestProject2() {
		return getWorkspace().getRoot().getProject("testProject2");
	}

	public void setGetPersistentProperty(IResource target) throws Throwable {
		String value = "this is a test property value";
		QualifiedName name = new QualifiedName("itp-test", "testProperty");
		target.setPersistentProperty(name, value);
		// see if we can get the property
		assertTrue("get not equal set", target.getPersistentProperty(name).equals(value));
		// see what happens if we get a non-existant property
		name = new QualifiedName("itp-test", "testNonProperty");
		assertNull("non-existant persistent property not missing", target.getPersistentProperty(name));
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(WorkspaceTest.class.getName());
		// test the basic create operations
		suite.addTest(new WorkspaceTest("testProjectCreation"));
		suite.addTest(new WorkspaceTest("testFolderCreation"));
		suite.addTest(new WorkspaceTest("testFileCreation"));
		suite.addTest(new WorkspaceTest("testFileInFolderCreation"));
		suite.addTest(new WorkspaceTest("testSetContents"));
		suite.addTest(new WorkspaceTest("testFileOverFolder"));
		suite.addTest(new WorkspaceTest("testFolderOverFile"));
		suite.addTest(new WorkspaceTest("testProjectCreateOpenCloseDelete"));
		suite.addTest(new WorkspaceTest("testProjectReferences"));

		// test closing and reopening 
		suite.addTest(new WorkspaceTest("testProjectCloseOpen"));

		// test persistent properties on all the different resource types.
		suite.addTest(new WorkspaceTest("testSetGetProjectPersistentProperty"));
		suite.addTest(new WorkspaceTest("testSetGetFolderPersistentProperty"));
		suite.addTest(new WorkspaceTest("testSetGetFilePersistentProperty"));

		// test moves
		suite.addTest(new WorkspaceTest("testSimpleMove"));
		suite.addTest(new WorkspaceTest("testFileMove"));
		suite.addTest(new WorkspaceTest("testLeafFolderMove"));
		suite.addTest(new WorkspaceTest("testFolderMove"));

		// create a bunch of things at once
		suite.addTest(new WorkspaceTest("testMultiCreation"));

		// test deletions
		suite.addTest(new WorkspaceTest("testFolderDeletion"));
		suite.addTest(new WorkspaceTest("testFileDeletion"));
		suite.addTest(new WorkspaceTest("testFileEmptyDeletion"));
		suite.addTest(new WorkspaceTest("testMultiDeletion"));
		suite.addTest(new WorkspaceTest("testProjectDeletion"));

		suite.addTest(new WorkspaceTest("doCleanup"));
		return suite;
	}

	protected void tearDown() throws Exception {
		// overwrite the superclass and do nothing since our test methods build on each other
	}

	public void testFileCreation() throws Throwable {
		IPath path = new Path("/testProject/testFile");
		IFile target = getWorkspace().getRoot().getFile(path);
		target.create(null, true, getMonitor());
		assertTrue(target.exists());
	}

	public void testFileDeletion() throws Throwable {
		IPath path = new Path("/testProject/testFileForDelete");
		IFile target = getWorkspace().getRoot().getFile(path);
		target.create(null, true, getMonitor());
		assertTrue(target.exists());
		target.delete(true, getMonitor());
		assertTrue(!target.exists());
	}

	public void testFileEmptyDeletion() throws Throwable {
		IPath path = new Path("/testProject/testFileForDelete2");
		IFile target = getWorkspace().getRoot().getFile(path);
		target.create(getContents(""), true, getMonitor());
		assertTrue(target.exists());
		target.delete(true, getMonitor());
		assertTrue(!target.exists());
	}

	public void testFileInFolderCreation() throws Throwable {
		IPath path = new Path("/testProject/testFolder/testFile2");
		IFile target = getWorkspace().getRoot().getFile(path);
		target.create(getRandomContents(), true, getMonitor());
		assertTrue(target.exists());
	}

	public void testFileMove() throws Throwable {
		IPath path = new Path("/testProject/targetFile");
		IFile target = getWorkspace().getRoot().getFile(path);
		target.create(getRandomContents(), true, getMonitor());
		IFile destination = getWorkspace().getRoot().getFile(new Path("/testProject/movedFile"));
		target.move(destination.getFullPath(), true, getMonitor());
		assertTrue(destination.exists());
		assertTrue(!target.exists());
	}

	public void testFileOverFolder() throws Throwable {
		IPath path = new Path("/testProject/testFolder");
		IFolder existing = getWorkspace().getRoot().getFolder(path);
		assertTrue(existing.exists());
		IFile target = getWorkspace().getRoot().getFile(path);
		try {
			target.create(null, true, getMonitor());
		} catch (CoreException e) {
			assertTrue(existing.exists());
			return;
		}
		fail("Should not be able to create file over folder");
	}

	public void testFolderCreation() throws Throwable {
		IPath path = new Path("/testProject/testFolder");
		IFolder target = getWorkspace().getRoot().getFolder(path);
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

	public void testFolderMove() throws Throwable {
		IProject project = getTestProject();
		IResource[] before = buildResources(project, new String[] {"b/", "b/b/", "b/x", "b/b/y", "b/b/z"});
		IResource[] after = buildResources(project, new String[] {"a/", "a/b/", "a/x", "a/b/y", "a/b/z"});

		// create the resources and set some content in a file that will be moved.
		ensureExistsInWorkspace(before, true);
		String content = getRandomString();
		IFile file = project.getFile(new Path("b/b/z"));
		file.setContents(getContents(content), true, false, getMonitor());

		// Be sure the resources exist and then move them.
		assertExistsInWorkspace(before);
		project.getFolder("b").move(project.getFullPath().append("a"), true, getMonitor());

		//
		assertDoesNotExistInWorkspace(before);
		assertExistsInWorkspace(after);
		file = project.getFile(new Path("a/b/z"));
		assertTrue("get not equal set", compareContent(getContents(content), file.getContents(false)));
	}

	public void testFolderOverFile() throws Throwable {
		IPath path = new Path("/testProject/testFile");
		IFile existing = getWorkspace().getRoot().getFile(path);
		assertTrue(existing.exists());
		IFolder target = getWorkspace().getRoot().getFolder(path);
		try {
			target.create(true, true, getMonitor());
		} catch (CoreException e) {
			assertTrue(existing.exists());
			return;
		}
		fail("Should not be able to create folder over a file");
	}

	public void testLeafFolderMove() throws Throwable {
		IProject project = getTestProject();
		IFolder source = project.getFolder("testFolder");
		IFolder dest = project.getFolder("movedFolder");
		//
		source.move(dest.getFullPath(), true, getMonitor());
		assertExistsInWorkspace(dest);
		assertDoesNotExistInWorkspace(source);
	}

	public void testMultiCreation() throws Throwable {
		final IProject project = getWorkspace().getRoot().getProject("bar");
		final IResource[] resources = buildResources(project, new String[] {"a/", "a/b"});
		IWorkspaceRunnable body = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				if (!project.exists())
					project.create(null);
				if (!project.isOpen())
					project.open(null);
				// define an operation which will create a bunch of resources including a project.
				for (int i = 0; i < resources.length; i++) {
					IResource resource = resources[i];
					switch (resource.getType()) {
						case IResource.FILE :
							((IFile) resource).create(null, false, getMonitor());
							break;
						case IResource.FOLDER :
							((IFolder) resource).create(false, true, getMonitor());
							break;
						case IResource.PROJECT :
							((IProject) resource).create(getMonitor());
							break;
					}
				}
			}
		};
		getWorkspace().run(body, getMonitor());
		assertExistsInWorkspace(project);
		assertExistsInWorkspace(resources);
	}

	public void testMultiDeletion() throws Throwable {
		IProject project = getTestProject();
		IResource[] before = buildResources(project, new String[] {"c/", "c/b/", "c/x", "c/b/y", "c/b/z"});
		ensureExistsInWorkspace(before, true);
		//
		assertExistsInWorkspace(before);
		getWorkspace().delete(before, true, getMonitor());
		assertDoesNotExistInWorkspace(before);
	}

	public void testProjectCloseOpen() throws Throwable {
		IProject target = getTestProject();
		target.close(getMonitor());
		assertTrue(target.exists());
		assertTrue(!target.isOpen());
		assertTrue(!target.getFolder("testFolder").exists());
		target.open(getMonitor());
		assertTrue(target.isOpen());
		assertTrue(target.getFolder("testFolder").exists());
	}

	public void testProjectCreateOpenCloseDelete() throws Throwable {
		IProject target = getTestProject2();
		target.create(null, getMonitor());
		assertTrue(target.exists());
		target.open(getMonitor());
		assertTrue(target.isOpen());
		target.close(getMonitor());
		assertTrue(!target.isOpen());
		target.delete(true, getMonitor());
		assertTrue(!target.exists());
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

	public void testProjectReferences() throws Throwable {
		IProject target = getTestProject2();
		target.create(null, getMonitor());
		assertTrue(target.exists());
		IProject project = getTestProject();
		IProjectDescription description = project.getDescription();
		description.setReferencedProjects(new IProject[] {target});
		project.setDescription(description, getMonitor());
		assertTrue(target.getReferencingProjects().length == 1);

		target.delete(true, getMonitor());
		assertTrue(!target.exists());
	}

	public void testSetContents() throws Throwable {
		IPath path = new Path("/testProject/testFile");
		IFile target = getWorkspace().getRoot().getFile(path);
		String testString = getRandomString();
		target.setContents(getContents(testString), true, false, getMonitor());
		InputStream content = null;
		try {
			content = target.getContents(false);
			assertTrue("get not equal set", compareContent(content, getContents(testString)));
		} finally {
			content.close();
		}
	}

	public void testSetGetFilePersistentProperty() throws Throwable {
		IResource target = getWorkspace().getRoot().getFile(new Path("/testProject/testFile"));
		setGetPersistentProperty(target);
	}

	public void testSetGetFolderPersistentProperty() throws Throwable {
		IResource target = getWorkspace().getRoot().getFolder(new Path("/testProject/testFolder"));
		setGetPersistentProperty(target);
	}

	public void testSetGetProjectPersistentProperty() throws Throwable {
		IResource target = getWorkspace().getRoot().getProject("/testProject");
		setGetPersistentProperty(target);
	}

	public void testSetProperty() throws Throwable {
		IPath path = new Path("/testProject/testFile");
		IFile target = getWorkspace().getRoot().getFile(path);
		String value = "this is a test property value";
		QualifiedName name = new QualifiedName("itp-test", "testProperty");
		target.setPersistentProperty(name, value);
		assertTrue("get not equal set", target.getPersistentProperty(name).equals(value));
	}

	public void testSimpleMove() throws Throwable {
		IPath path = new Path("/testProject/simpleFile");
		IFile target = getWorkspace().getRoot().getFile(path);
		target.create(getRandomContents(), true, getMonitor());
		IFile destination = getWorkspace().getRoot().getFile(new Path("/testProject/newSimpleFile"));
		target.move(destination.getFullPath(), true, getMonitor());
		assertTrue(destination.exists());
		assertTrue(!target.exists());
	}
}

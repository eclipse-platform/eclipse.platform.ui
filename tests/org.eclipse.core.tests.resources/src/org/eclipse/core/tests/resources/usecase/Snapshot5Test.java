package org.eclipse.core.tests.resources.usecase;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
/**
 * Only verifies previous session.
 */
public class Snapshot5Test extends SnapshotTest {
public Snapshot5Test() {
}
public Snapshot5Test(String name) {
	super(name);
}
public static Test suite() {
	// we do not add the whole class because the order is important
	TestSuite suite = new TestSuite();
	suite.addTest(new Snapshot5Test("testVerifyPreviousSession"));
	suite.addTest(new Snapshot5Test("cleanUp"));
	return suite;
}
public void testVerifyPreviousSession() {
	// MyProject
	IProject project = getWorkspace().getRoot().getProject(PROJECT_1);
	assertTrue("0.0", project.exists());
	assertTrue("0.1", project.isOpen());

	// verify existence of children
	IResource[] resources = buildResources(project, Snapshot4Test.defineHierarchy1());
	assertExistsInFileSystem("2.1", resources);
	assertExistsInWorkspace("2.2", resources);
	IFile file = project.getFile("added file");
	assertDoesNotExistInFileSystem("2.3", file);
	assertDoesNotExistInWorkspace("2.4", file);
	file = project.getFile("yet another file");
	assertDoesNotExistInFileSystem("2.5", file);
	assertDoesNotExistInWorkspace("2.6", file);
	IFolder folder = project.getFolder("a folder");
	assertDoesNotExistInFileSystem("2.7", folder);
	assertDoesNotExistInWorkspace("2.8", folder);

	// Project2
	project = getWorkspace().getRoot().getProject(PROJECT_2);
	assertTrue("3.0", !project.exists());
}
public void cleanUp() throws CoreException {
	ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
	getWorkspace().save(true, null);
}
}
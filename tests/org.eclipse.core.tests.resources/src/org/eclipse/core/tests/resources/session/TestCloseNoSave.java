package org.eclipse.core.tests.resources.session;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

/**
 * Tests closing a workspace without save.
 */
public class TestCloseNoSave extends WorkspaceSerializationTest {
/**
 * Constructor for TestCloseNoSave.
 */
public TestCloseNoSave() {
	super();
}
/**
 * Constructor for TestCloseNoSave.
 * @param name
 */
public TestCloseNoSave(String name) {
	super(name);
}
public void test1() throws CoreException {
	/* create some resource handles */
	IProject project = workspace.getRoot().getProject(PROJECT);
	project.create(getMonitor());
	project.open(getMonitor());
	IFolder folder = project.getFolder(FOLDER);
	folder.create(true, true, getMonitor());
	IFile file = folder.getFile(FILE);
	file.create(getRandomContents(), true, getMonitor());
}
public void test2() throws CoreException {
	/* projects should exist, but files shouldn't.  Refresh local should bring in files */
	IResource[] members = workspace.getRoot().members();
	assertEquals("1.0", 1, members.length);
	assertTrue("1.1", members[0].getType() == IResource.PROJECT);
	IProject project  = (IProject)members[0];
	assertTrue("1.2", project.exists());
	members = project.members();
	assertEquals("1.3", 1, members.length);
	assertEquals("1.3a", IProjectDescription.DESCRIPTION_FILE_NAME, members[0].getName());
	IFolder folder = project.getFolder(FOLDER);
	IFile file = folder.getFile(FILE);	
	assertTrue("1.4", !folder.exists());
	assertTrue("1.5", !file.exists());

	//opening the project does an automatic local refresh	
	project.open(null);
	
	assertEquals("2.0", 2, project.members().length);
	assertTrue("2.1", folder.exists());
	assertTrue("2.2", file.exists());
}
}


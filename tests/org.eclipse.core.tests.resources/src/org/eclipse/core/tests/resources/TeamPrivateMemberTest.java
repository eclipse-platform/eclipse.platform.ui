package org.eclipse.core.tests.resources;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import junit.framework.*;
import junit.textui.TestRunner;
import java.util.*;

public class TeamPrivateMemberTest extends EclipseWorkspaceTest {
public TeamPrivateMemberTest() {
}
public TeamPrivateMemberTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(TeamPrivateMemberTest.class);

//	TestSuite suite = new TestSuite();
//	suite.addTest(new TeamPrivateMemberTest("testProjectMoveVariations"));
//	return suite;
}
protected void tearDown() throws Exception {
	super.tearDown();
	//FIXME: This refresh may fail in the future if the .project file has been deleted
	getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
	ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
}
public void testMembers() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	IFile file = project.getFile("file.txt");
	IFile subFile = folder.getFile("subfile.txt");
	IResource[] resources = new IResource[] {project, folder, file, subFile};
	IResource[] members = null;
	ensureExistsInWorkspace(resources, true);
	
	// Initial values should be false.
	assertTeamPrivateMember("1.0", project, false, IResource.DEPTH_INFINITE);

	// Check the calls to #members
	try {
		members = project.members();
	} catch(CoreException e) {
		fail("2.0", e);
	}
	// +1 for the .project file
	assertEquals("2.1", 3, members.length);
	try {
		members = folder.members();
	} catch(CoreException e) {
	}
	assertEquals("2.3", 1, members.length);
	
	// Set the values.
	setTeamPrivateMember("3.0", project, true, IResource.DEPTH_INFINITE);
	assertTeamPrivateMember("3.1", project, true, IResource.DEPTH_INFINITE);
	
	// Check the values
	assertTeamPrivateMember("4.0", project, true, IResource.DEPTH_INFINITE);

	// Check the calls to #members
	try {
		members = project.members();
	} catch(CoreException e) {
		fail("5.0", e);
	}
	assertEquals("5.1", 0, members.length);
	try {
		members = folder.members();
	} catch(CoreException e) {
		fail("5.2", e);
	}
	assertEquals("5.3", 0, members.length);
	
	// FIXME: add the tests for #members(int)
}
public void testAccept() {
}
public void testDeltaAccept() {
}
public void testExists() {
}public void testSetGet() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	IFile file = project.getFile("file.txt");
	IFile subFile = folder.getFile("subfile.txt");
	IResource[] resources = new IResource[] {project, folder, file, subFile};
	ensureExistsInWorkspace(resources, true);
	
	// Initial values should be false.
	for (int i=0; i<resources.length; i++) {
		IResource resource = resources[i];
		assertTrue("1.0: " + resource.getFullPath(), !resource.isTeamPrivateMember());
	}
	
	// Now set the values.
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		try {
			resource.setTeamPrivateMember(true);
		} catch (CoreException e) {
			fail("2.0: " + resource.getFullPath(), e);
		}
	}
	
	// The values should be true for files and folders, false otherwise.
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		switch (resource.getType()) {
			case IResource.FILE:
			case IResource.FOLDER:
				assertTrue("3.0: " + resource.getFullPath(), resource.isTeamPrivateMember());
				break;
			case IResource.PROJECT:
			case IResource.ROOT:
				assertTrue("3.1: " + resource.getFullPath(), !resource.isTeamPrivateMember());
				break;
		}
	}
	
	// Clear the values.
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		try {
			resource.setTeamPrivateMember(false);
		} catch (CoreException e) {
			fail("4.0: " + resource.getFullPath(), e);
		}
	}

	// Values should be false again.
	for (int i=0; i<resources.length; i++) {
		IResource resource = resources[i];
		assertTrue("5.0: " + resource.getFullPath(), !resource.isTeamPrivateMember());
	}
}
protected void assertTeamPrivateMember(final String message, IResource root, final boolean value, int depth) {
	IResourceVisitor visitor = new IResourceVisitor() {
		public boolean visit(IResource resource) throws CoreException {
			boolean expected = false;
			if (resource.getType() == IResource.FILE || resource.getType() == IResource.FOLDER)
				expected = value;
			assertEquals(message + resource.getFullPath(), expected, resource.isTeamPrivateMember());
			return true;
		}
	};
	try {
		root.accept(visitor, depth, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail(message + "resource.accept", e);
	}
}
protected void setTeamPrivateMember(final String message, IResource root, final boolean value, int depth) {
	IResourceVisitor visitor = new IResourceVisitor() {
		public boolean visit(IResource resource) throws CoreException {
			try {
				resource.setTeamPrivateMember(value);
			} catch(CoreException e) {
				fail(message + resource.getFullPath(), e);
			}
			return true;
		}
	};
	try {
		root.accept(visitor, depth, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail(message + "resource.accept", e);
	}
}
}

package org.eclipse.core.tests.resources;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import java.util.HashSet;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

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
public void findMember() {
}
/**
 * Resources which are marked as team private members are not included in #members
 * calls unless specifically included by calling #members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS)
 */
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
	
	// reset to false
	setTeamPrivateMember("6.0", project, false, IResource.DEPTH_INFINITE);
	assertTeamPrivateMember("6.1", project, false, IResource.DEPTH_INFINITE);
	
	// Check the calls to members(IResource.NONE);
	try {
		members = project.members(IResource.NONE);
	} catch(CoreException e) {
		fail("7.0", e);
	}
	// +1 for the .project file
	assertEquals("7.1", 3, members.length);
	try {
		members = project.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("7.2", e);
	}
	// +1 for the .project file
	assertEquals("7.3", 3, members.length);
	try {
		members = folder.members();
	} catch (CoreException e) {
		fail("7.4", e);
	}
	assertEquals("7.5", 1, members.length);
	
	// Set one of the children to be TEAM_PRIVATE and try again
	try {
		folder.setTeamPrivateMember(true);
	} catch(CoreException e) {
		fail("8.0", e);
	}
	try {
		members = project.members();
	} catch(CoreException e) {
		fail("8.1", e);
	}
	// +1 for .project, -1 for team private folder
	assertEquals("8.2", 2, members.length);
	try {
		members = folder.members();
	} catch(CoreException e) {
		fail("8.3", e);
	}
	assertEquals("8.4", 1, members.length);
	try {
		members = project.members(IResource.NONE);
	} catch(CoreException e) {
		fail("8.5", e);
	}
	// +1 for .project, -1 for team private folder
	assertEquals("8.6", 2, members.length);
	try {
		members = folder.members();
	} catch(CoreException e) {
		fail("8.7", e);
	}
	assertEquals("8.8", 1, members.length);
	try {
		members = project.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("8.9", e);
	}
	// +1 for .project
	assertEquals("8.10", 3, members.length);
	try {
		members = folder.members();
	} catch(CoreException e) {
		fail("8.11", e);
	}
	assertEquals("8.12", 1, members.length);
	
	// Set all the resources to be team private
	setTeamPrivateMember("9.0", project, true, IResource.DEPTH_INFINITE);
	assertTeamPrivateMember("9.1", project, true, IResource.DEPTH_INFINITE);
	try {
		members = project.members(IResource.NONE);
	} catch(CoreException e) {
		fail("9.2", e);
	}
	assertEquals("9.3", 0, members.length);
	try {
		members = folder.members(IResource.NONE);
	} catch(CoreException e) {
		fail("9.4", e);
	}
	assertEquals("9.5", 0, members.length);
	try {
		members = project.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("9.6", e);
	}
	// +1 for .project
	assertEquals("9.7", 3, members.length);
	try {
		members = folder.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("9.8", e);
	}
	assertEquals("9.9", 1, members.length);
}
/**
 * Resources which are marked as team private members should not be visited by
 * resource visitors.
 */
public void testAccept() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	IFile file = project.getFile("file.txt");
	IFile subFile = folder.getFile("subfile.txt");
	IResource[] resources = new IResource[] {project, folder, file, subFile};
	ensureExistsInWorkspace(resources, true);
	IResource description = project.getFile(".project");
	Set expected = new HashSet();
	final Set actual = new HashSet();
	
	// default case, no team private members
	for (int i=0; i<resources.length; i++)
		expected.add(resources[i].getFullPath());
	expected.add(description.getFullPath());
	IResourceVisitor visitor = new IResourceVisitor() {
		public boolean visit(IResource resource) {
			actual.add(resource.getFullPath());
			return true;
		}
	};
	try {
		project.accept(visitor);
	} catch(CoreException e) {
		fail("1.0", e);
	}
	assertEquals("1.1", expected, actual);
	actual.clear();
	try {
		project.accept(visitor, IResource.DEPTH_INFINITE, IResource.NONE);
	} catch(CoreException e) {
		fail("1.2", e);
	}
	assertEquals("1.3", expected, actual);
	try {
		project.accept(visitor, IResource.DEPTH_INFINITE, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("1.4", e);
	}
	assertEquals("1.5", expected, actual);
	
	// set the folder to be team private. It and its children should
	// be ignored by the visitor
	try {
		folder.setTeamPrivateMember(true);
	} catch(CoreException e) {
		fail("2.0", e);
	}
	expected.clear();
	expected.add(project.getFullPath());
	expected.add(file.getFullPath());
	expected.add(description.getFullPath());
	actual.clear();
	try {
		project.accept(visitor);
	} catch(CoreException e) {
		fail("2.1", e);
	}
	assertEquals("2.2", expected, actual);
	actual.clear();
	try {
		project.accept(visitor, IResource.DEPTH_INFINITE, IResource.NONE);
	} catch(CoreException e) {
		fail("2.3", e);
	}
	assertEquals("2.4", expected, actual);
	// should see all resources if we include the flag
	expected.clear();
	for (int i=0; i<resources.length; i++)
		expected.add(resources[i].getFullPath());
	expected.add(description.getFullPath());
	actual.clear();
	try {
		project.accept(visitor, IResource.DEPTH_INFINITE, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("2.5", e);
	}
	assertEquals("2.6", expected, actual);
	
	// now set all resources to be team private.
	setTeamPrivateMember("3.0", project, true, IResource.DEPTH_INFINITE);
	assertTeamPrivateMember("3.1", project, true, IResource.DEPTH_INFINITE);
	expected.clear();
	expected.add(project.getFullPath());
	actual.clear();
	try {
		project.accept(visitor);
	} catch(CoreException e) {
		fail("3.2", e);
	}
	assertEquals("3.3", expected, actual);
	actual.clear();
	try {
		project.accept(visitor, IResource.DEPTH_INFINITE, IResource.NONE);
	} catch(CoreException e) {
		fail("3.4", e);
	}
	assertEquals("3.5", expected, actual);
	// should see all resources if we include the flag
	expected.clear();
	for (int i=0; i<resources.length; i++)
		expected.add(resources[i].getFullPath());
	expected.add(description.getFullPath());
	actual.clear();
	try {
		project.accept(visitor, IResource.DEPTH_INFINITE, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("3.6", e);
	}
	assertEquals("3.7", expected, actual);
}
public void testMove() {
}public void testFindMarkers() {
}public void testDelete() {
}public void testCopy() {
}public void testDeltaAccept() {
}
/**
 * Resources which are marked as team private members return TRUE
 * in all calls to #exists.
 */
public void testExists() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	IFile file = project.getFile("file.txt");
	IFile subFile = folder.getFile("subfile.txt");
	IResource[] resources = new IResource[] {project, folder, file, subFile};
	ensureExistsInWorkspace(resources, true);
	
	// Check to see if all the resources exist in the workspace tree.
	for (int i=0; i<resources.length; i++)
		assertTrue("1.0." + resources[i].getFullPath(), resources[i].exists());
	
	// set a folder to be a team private member
	try {
		folder.setTeamPrivateMember(true);
	} catch (CoreException e) {
		fail("2.0", e);
	}
	assertTeamPrivateMember("2.1", folder, true, IResource.DEPTH_ZERO);
	for (int i=0; i<resources.length; i++)
		assertTrue("2.2." + resources[i].getFullPath(), resources[i].exists());
	
	// set all resources to be team private
	setTeamPrivateMember("3.0", project, true, IResource.DEPTH_INFINITE);
	assertTeamPrivateMember("3.1", project, true, IResource.DEPTH_INFINITE);
	for (int i=0; i<resources.length; i++)
		assertTrue("3.2." + resources[i].getFullPath(), resources[i].exists());
}
public void testSetGet() {
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
		ropublic void testMove() {
}ot.accept(visitor, depth, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail(message + "resource.accept", e);
	}
}
}

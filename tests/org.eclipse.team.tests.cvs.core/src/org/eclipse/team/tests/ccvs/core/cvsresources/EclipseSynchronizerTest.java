package org.eclipse.team.tests.ccvs.core.cvsresources;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

/**
 * Tests the EclipseSynchronizer.
 * Does not test state change broadcasts.
 */
public class EclipseSynchronizerTest extends EclipseTest {
	private IProject project;
	private static EclipseSynchronizer sync = EclipseSynchronizer.getInstance();
	
	public EclipseSynchronizerTest() {
		super();
	}
	
	public EclipseSynchronizerTest(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Run all tests twice to ensure consistency between batched and non-batched behaviour.
		// 1. First run -- no batching
		suite.addTestSuite(EclipseSynchronizerTest.class);
		// 2. Second run -- with batching
		suite.addTest(new BatchedTestSetup(new TestSuite(EclipseSynchronizerTest.class)));
		return new CVSTestSetup(suite);
	}
	
	public void testFolderSync() throws CoreException, CVSException {
		// Workspace root
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		_testFolderSyncInvalid(root);

		// Non-existant project
		IProject project = root.getProject(getName() + "-" + System.currentTimeMillis());
		assertDoesNotExistInFileSystem(project);
		_testFolderSyncInvalid(project);

		// Project
		project.create(null);
		project.open(null);
		_testFolderSyncValid(project);
		
		// Non-existant folder
		IFolder folder = project.getFolder("folder1");
		assertDoesNotExistInFileSystem(folder);
		_testFolderSyncInvalid(folder);
		
		// Non-existant folder with non-existant parent
		IFolder childFolder = folder.getFolder("folder2");
		assertDoesNotExistInFileSystem(childFolder);
		_testFolderSyncInvalid(childFolder);
		
		// Folder
		folder.create(false /*force*/, true /*local*/, null);
		_testFolderSyncValid(folder);
		
		// Child folder
		childFolder.create(false /*force*/, true /*local*/, null);
		_testFolderSyncValid(childFolder);
		
		// Deleted folder -- sync info should no longer exist
		sync.setFolderSync(folder, dummyFolderSync(folder));
		folder.delete(false /*force*/, null);
		_testFolderSyncInvalid(folder); // verifies sync info was deleted
		
		// Recreated folder -- sync info should not be preserved across deletions
		folder.create(false /*force*/, true /*local*/, null);
		sync.setFolderSync(folder, dummyFolderSync(folder));
		folder.delete(false /*force*/, null);
		folder.create(false /*force*/, true /*local*/, null);
		_testFolderSyncValid(folder); // verifies sync info has not reappeared
		
		// Deleted project
		sync.setFolderSync(project, dummyFolderSync(project));
		project.delete(false /*force*/, null);
		_testFolderSyncInvalid(project);
	}
	
	/*
	 * Test get/set/delete folder sync for things that support it.
	 * Assumes container does not already have sync info.
	 */
	private void _testFolderSyncValid(IContainer container) throws CoreException, CVSException {
		FolderSyncInfo info = sync.getFolderSync(container);
		assertNull(info);
		sync.deleteFolderSync(container);
		FolderSyncInfo newInfo = dummyFolderSync(container);
		sync.setFolderSync(container, newInfo);
		info = sync.getFolderSync(container);
		assertEquals(newInfo, info);
		// verify that deleteFolderSync() does the right thing
		buildResources(container, new String[] { "hassync/", "nosync", "hassync.txt", "nosync.txt" }, true);
		IResource resource = container.getFile(new Path("hassync.txt"));
		sync.setResourceSync(resource, dummyResourceSync(resource));
		resource = container.getFile(new Path("hassync"));
		sync.setResourceSync(resource, dummyResourceSync(resource));
		assertNotNull(sync.getResourceSync(container.getFile(new Path("hassync.txt"))));
		assertNull(sync.getResourceSync(container.getFile(new Path("nosync.txt"))));
		assertNotNull(sync.getResourceSync(container.getFolder(new Path("hassync"))));
		assertNull(sync.getResourceSync(container.getFolder(new Path("nosync"))));
		if (container.getType() == IResource.FOLDER) {
			sync.setResourceSync(container, dummyResourceSync(container));
			assertNotNull(sync.getResourceSync(container));
		}
		// should delete folder sync for self, and resource sync for children
		sync.deleteFolderSync(container);
		info = sync.getFolderSync(container);
		assertNull(info);
		assertNull(sync.getResourceSync(container.getFile(new Path("hassync.txt"))));
		assertNull(sync.getResourceSync(container.getFile(new Path("nosync.txt"))));
		assertNull(sync.getResourceSync(container.getFolder(new Path("hassync"))));
		assertNull(sync.getResourceSync(container.getFolder(new Path("nosync"))));
		if (container.getType() == IResource.FOLDER) {
			assertNotNull(sync.getResourceSync(container));
		}
	}
	
	/*
	 * Test get/set/delete folder sync for things that should not support it.
	 * Assumes container does not already have sync info.
	 */
	private void _testFolderSyncInvalid(IContainer container) throws CVSException {
		FolderSyncInfo info = sync.getFolderSync(container);
		assertNull(info);
		sync.deleteFolderSync(container);
		try {
			sync.setFolderSync(container, dummyFolderSync(container));
			fail("Expected CVSException");
		} catch (CVSException e) {
		}
		info = sync.getFolderSync(container);
		assertNull(info);
	}
	
	public void testResourceSync() throws CoreException, CVSException {
		// Workspace root
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		_testResourceSyncInvalid(root);

		// Project
		IProject project = getUniqueTestProject(getName());
		assertExistsInFileSystem(project);
		_testResourceSyncInvalid(project);
		
		// Folder
		IFolder folder = project.getFolder("folder1");
		folder.create(false /*force*/, true /*local*/, null);
		_testResourceSyncValid(folder);
		
		// File
		IFile file = folder.getFile("file1");
		file.create(getRandomContents(), false /*force*/, null);
		_testResourceSyncValid(file);
		
		// Deleted/recreated file -- if parent exists, sync info should be preserved across deletions
		ResourceSyncInfo info = dummyResourceSync(file);
		sync.setResourceSync(file, info);
		file.delete(false /*force*/, null);
		ResourceSyncInfo newInfo = sync.getResourceSync(file);
		assertEquals(newInfo, info);
		file.create(getRandomContents(), false /*force*/, null);
		newInfo = sync.getResourceSync(file);
		assertEquals(newInfo, info);
		sync.deleteResourceSync(file);
		file.delete(false /*force*/, null);
		_testResourceSyncValid(file);
		
		// Deleted parent -- sync info of children should also be deleted
		sync.setResourceSync(file, info);
		folder.delete(false /*force*/, null);
		info = sync.getResourceSync(file);
		assertNull(info);
		
		// File in non-existant folder
		IFolder dummyFolder = project.getFolder("folder2");
		assertDoesNotExistInFileSystem(dummyFolder);
		IFile dummyFile = dummyFolder.getFile("file2");
		assertDoesNotExistInFileSystem(dummyFile);
		_testResourceSyncInvalid(dummyFile);
	}
	
	/*
	 * Test get/set/delete resource sync for things that support it.
	 * Assumes resource does not already have sync info.
	 */
	private void _testResourceSyncValid(IResource resource) throws CVSException {
		ResourceSyncInfo info = sync.getResourceSync(resource);
		assertNull(info);
		sync.deleteResourceSync(resource);
		ResourceSyncInfo newInfo = dummyResourceSync(resource);
		sync.setResourceSync(resource, newInfo);
		info = sync.getResourceSync(resource);
		assertEquals(newInfo, info);
		sync.deleteResourceSync(resource);
		info = sync.getResourceSync(resource);
		assertNull(info);
	}
	
	/*
	 * Test get/set/delete resource sync for things that should not support it.
	 * Assumes resource does not already have sync info.
	 */
	private void _testResourceSyncInvalid(IResource resource) throws CVSException {
		ResourceSyncInfo info = sync.getResourceSync(resource);
		assertNull(info);
		sync.deleteResourceSync(resource);
		try {
			sync.setResourceSync(resource, dummyResourceSync(resource));
			fail("Expected CVSException");
		} catch (CVSException e) {
		}
		info = sync.getResourceSync(resource);
		assertNull(info);
	}
	
	public void testIgnores() throws CoreException, CVSException {
		// Workspace root
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		_testIgnoresInvalid(root);

		// Non-existant project
		IProject project = root.getProject(getName() + "-" + System.currentTimeMillis());
		assertDoesNotExistInFileSystem(project);
		_testIgnoresInvalid(project);

		// Project
		project.create(null);
		project.open(null);
		_testIgnoresValid(project);
		
		// Non-existant folder
		IFolder folder = project.getFolder("folder1");
		assertDoesNotExistInFileSystem(folder);
		_testIgnoresInvalid(folder);
		
		// Non-existant folder with non-existant parent
		IFolder childFolder = folder.getFolder("folder2");
		assertDoesNotExistInFileSystem(childFolder);
		_testIgnoresInvalid(childFolder);
		
		// Folder
		folder.create(false /*force*/, true /*local*/, null);
		_testIgnoresValid(folder);
		
		// Child folder
		childFolder.create(false /*force*/, true /*local*/, null);
		_testIgnoresValid(childFolder);
		
		// Deleted folder -- ignores should no longer exist
		sync.addIgnored(folder, "*.foo");
		folder.delete(false /*force*/, null);
		_testIgnoresInvalid(folder); // verifies sync info was deleted
		
		// Recreated folder -- sync info should not be preserved across deletions
		folder.create(false /*force*/, true /*local*/, null);
		sync.addIgnored(folder, "*.foo");
		folder.delete(false /*force*/, null);
		folder.create(false /*force*/, true /*local*/, null);
		_testIgnoresValid(folder); // verifies sync info has not reappeared
		
		// Deleted project
		sync.addIgnored(project, "*.foo");
		project.delete(false /*force*/, null);
		_testIgnoresInvalid(project);
	}
	
	/*
	 * Test get/set ignores for things that should not support it.
	 * Assumes resource does not already have ignores.
	 */
	private void _testIgnoresValid(IContainer container) throws CVSException {
		String[] ignored = sync.getIgnored(container);
		assertTrue(ignored.length == 0);
		sync.addIgnored(container, "*.xyz");
		ignored = sync.getIgnored(container);
		assertBijection(ignored, new String[] { "*.xyz" }, null);
		sync.addIgnored(container, "*.abc");
		sync.addIgnored(container, "*.def");
		ignored = sync.getIgnored(container);
		assertBijection(ignored, new String[] { "*.abc", "*.def", "*.xyz" }, null);
	}

	/*
	 * Test get/set ignores for things that should not support it.
	 * Assumes resource does not already have ignores.
	 */
	private void _testIgnoresInvalid(IContainer container) throws CVSException {
		String[] ignored = sync.getIgnored(container);
		assertTrue(ignored.length == 0);
		try {
			sync.addIgnored(container, "*.xyz");
			fail("Expected CVSException");
		} catch (CVSException e) {
		}
		ignored = sync.getIgnored(container);
		assertTrue(ignored.length == 0);
	}
	
	public void testMembers() throws CoreException, CVSException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project1 = getUniqueTestProject(getName() + "1");
		IProject project2 = root.getProject(getName() + "2" + System.currentTimeMillis());

		// Workspace root
		IResource[] members = sync.members(ResourcesPlugin.getWorkspace().getRoot());
		assertBijection(members, ResourcesPlugin.getWorkspace().getRoot().getProjects(), null);

		// Non-existant project
		members = sync.members(project2);
		assertEquals("Non-existant project should have no members", 0, members.length);
		
		// Non-existant folder
		IFolder folder = project1.getFolder("folder1");
		assertDoesNotExistInFileSystem(folder);
		members = sync.members(folder);
		assertEquals("Non-existant folder should have no members", 0, members.length);
		
		// Non-existant folder with non-existant parent
		IFolder childFolder = folder.getFolder("folder2");
		assertDoesNotExistInFileSystem(childFolder);
		members = sync.members(childFolder);
		assertEquals("Non-existant folder should have no members", 0, members.length);
		
		// Project
		buildResources(project1, new String[] {
			"hassync.txt", "deleted_nosync.txt", "deleted.txt", "hassync/", "deleted/", "deleted_nosync/" }, true);

		// initially none of the resources have sync info and they all exist
		Object[] ignores = new Object[] { project1.getFolder("CVS") };
		Set expectedMembers = new HashSet(Arrays.asList(project1.members()));
		members = sync.members(project1);
		assertBijection(expectedMembers.toArray(), members, ignores);
		
		// add sync info, resources should still appear exactly once
		IResource resource = project1.getFile("hassync.txt");
		sync.setResourceSync(resource, dummyResourceSync(resource));
		resource = project1.getFolder("hassync");
		sync.setResourceSync(resource, dummyResourceSync(resource));

		resource = project1.getFile("deleted.txt");
		sync.setResourceSync(resource, dummyResourceSync(resource));
		resource = project1.getFolder("deleted");
		sync.setResourceSync(resource, dummyResourceSync(resource));

		members = sync.members(project1);
		assertBijection(expectedMembers.toArray(), members, ignores);

		// delete resources, those with sync info should still appear, those without should not
		resource = project1.getFile("deleted.txt");
		resource.delete(false /*force*/, null);
		resource = project1.getFolder("deleted");
		resource.delete(false /*force*/, null);

		resource = project1.getFile("deleted_nosync.txt");
		resource.delete(false /*force*/, null);
		expectedMembers.remove(resource);
		resource = project1.getFolder("deleted_nosync");
		resource.delete(false /*force*/, null);
		expectedMembers.remove(resource);

		members = sync.members(project1);
		assertBijection(expectedMembers.toArray(), members, ignores);
		
		// delete sync info, only those that exist should appear
		resource = project1.getFile("hassync.txt");
		sync.deleteResourceSync(resource);
		resource = project1.getFolder("hassync");
		sync.deleteResourceSync(resource);

		resource = project1.getFile("deleted.txt");
		sync.deleteResourceSync(resource);
		expectedMembers.remove(resource);
		resource = project1.getFolder("deleted");
		sync.deleteResourceSync(resource);
		expectedMembers.remove(resource);

		members = sync.members(project1);
		assertBijection(expectedMembers.toArray(), members, ignores);
	}
	
	private FolderSyncInfo dummyFolderSync(IContainer container) {
		return new FolderSyncInfo("repo", "root", CVSTag.DEFAULT, false);
	}

	private ResourceSyncInfo dummyResourceSync(IResource resource) {
		if (resource.getType() == IResource.FILE) {
			MutableResourceSyncInfo info = new MutableResourceSyncInfo(resource.getName(), "1.1");
			info.setTag(CVSTag.DEFAULT);
			return info;
		} else {
			return new ResourceSyncInfo(resource.getName());
		}
	}
	
	/**
	 * Assert that there exists a bijection between the elements of the arrays.
	 */
	private void assertBijection(Object[] a, Object[] b, Object[] ignores) {
		List listA = new LinkedList(Arrays.asList(a));
		List listB = new LinkedList(Arrays.asList(b));
		if (ignores != null) {
			for (int i = 0; i < ignores.length; ++i ) {
				listA.remove(ignores[i]);
				listB.remove(ignores[i]);
			}
		}
		assertEquals("Should have same number of elements", listA.size(), listB.size());
		for (Iterator it = listB.iterator(); it.hasNext();) {
			Object obj = it.next();
			assertTrue("Should contain the same elements", listA.contains(obj));
			listA.remove(obj);
		}
	}
}

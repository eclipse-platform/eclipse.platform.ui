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
package org.eclipse.core.tests.internal.localstore;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.service.environment.Constants;

//
public class RefreshLocalTest extends LocalStoreTest implements ICoreConstants {

	public static Test suite() {
		return new TestSuite(RefreshLocalTest.class);
	}

	public RefreshLocalTest() {
		super();
	}

	public RefreshLocalTest(String name) {
		super(name);
	}

	/**
	 * Tests if files with names that are invalid segments are properly rejected - does not run on Windows.
	 */
	public void testDiscoverFileWithInvalidName() {
		//FIXME Temporarily skip this test due to VM vendor bug #96338

		if (Platform.getOS().equals(Constants.OS_WIN32))
			return;

		/* initialize common objects */
		IProject project = projects[0];

		/* test root deletion */
		IFile file = project.getFile("file.txt");
		ensureExistsInFileSystem(file);

		File fileWithInvalidName = new File(project.getLocation().toFile(), "a\\b");
		try {
			assertTrue("0.1", fileWithInvalidName.createNewFile());
		} catch (IOException e) {
			fail("0.2", e);
		}
		assertTrue("1.0", !file.exists());
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
			fail("2.0 - should have failed");
		} catch (CoreException ce) {
			IStatus status = ce.getStatus();
			assertTrue("2.1", status.isMultiStatus());
			IStatus[] children = status.getChildren();
			assertEquals("2.2", 1, children.length);
			assertTrue("2.3", children[0] instanceof ResourceStatus);
			assertEquals("2.4", IResourceStatus.INVALID_RESOURCE_NAME, children[0].getCode());
		}
		assertTrue("3.0", file.exists());
	}

	/**
	 * Test discovery of a linked resource on refresh.
	 */
	public void testDiscoverLinkedResource() {
		//create a linked resource with local contents missing
		//	IProject project = projects[0];
		//	ensureExistsInWorkspace(project, true);
		//	IPath location = getRandomLocation();
		//	IFile link = project.getFile("Link");
		//	try {
		//		link.createLink(location, IResource.ALLOW_MISSING_LOCAL, getMonitor());
		//	} catch (CoreException e) {
		//		fail("0.99", e);
		//	}
		//
		//	//should not be synchronized (exists in ws, but not in fs)
		//	assertTrue("1.0", !project.isSynchronized(IResource.DEPTH_INFINITE));
		//	assertTrue("1.1", !link.isSynchronized(IResource.DEPTH_ZERO));
		//
		//	//should exist in workspace
		//	assertTrue("1.3", link.exists());
		//	
		//	//refreshing shouldn't get rid of the link
		//	try {
		//		link.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		//	} catch (CoreException e) {
		//		fail("1.99", e);
		//	}
		//	assertTrue("1.4", link.exists());
		//	
		//	//create the contents in the file system
		//	try {
		//		createFileInFileSystem(location);
		//	} catch (CoreException e) {
		//		fail("2.99", e);
		//	}
		//	
		//	//should now be synchronized
		//	assertTrue("2.1", project.isSynchronized(IResource.DEPTH_INFINITE));
		//	assertTrue("2.2", link.isSynchronized(IResource.DEPTH_ZERO));
		//
		//	//refresh
		//	try {
		//		link.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		//	} catch (CoreException e) {
		//		fail("3.99", e);
		//	}
		//	
		//	//assert should exist in workspace
		//	assertTrue("2.3", link.exists());
	}

	/**
	 * Tests discovering a file via refresh local when neither the file
	 * nor its parent exists in the workspace.
	 */
	public void testFileDiscovery() throws Throwable {
		/* initialize common objects */
		IProject project = projects[0];

		IFolder folder = project.getFolder("Folder");
		IFile file = folder.getFile("File");
		IResource[] both = new IResource[] {folder, file};

		ensureExistsInFileSystem(both);
		ensureDoesNotExistInWorkspace(both);

		assertTrue("1.0", !file.exists());
		assertTrue("1.1", !folder.exists());
		assertTrue("1.2", !file.isSynchronized(IResource.DEPTH_ZERO));
		assertTrue("1.3", !folder.isSynchronized(IResource.DEPTH_INFINITE));
		file.refreshLocal(IResource.DEPTH_ZERO, getMonitor());
		assertTrue("1.4", file.exists());
		assertTrue("1.5", folder.exists());

		//try again with deleted project
		project.delete(IResource.FORCE, getMonitor());

		ensureExistsInFileSystem(both);
		ensureDoesNotExistInWorkspace(both);

		assertTrue("2.0", !file.exists());
		assertTrue("2.1", !folder.exists());
		file.refreshLocal(IResource.DEPTH_ZERO, getMonitor());
		assertTrue("2.2", !file.exists());
		assertTrue("2.3", !folder.exists());
	}

	public void testFileToFolder() throws Throwable {
		/* initialize common objects */
		IProject project = projects[0];

		/* */
		IFile file = project.getFile("file");
		file.create(null, true, null);
		ensureDoesNotExistInFileSystem(file);
		//
		File target = file.getLocation().toFile();
		target.mkdirs();
		//
		assertTrue("1.1", file.exists());
		assertTrue("1.2", target.isDirectory());
		file.refreshLocal(IResource.DEPTH_ZERO, null);
		assertTrue("1.3", !file.exists());
		IFolder folder = project.getFolder("file");
		assertTrue("1.4", folder.exists());
	}

	public void testFolderToFile() throws Throwable {
		/* initialize common objects */
		IProject project = projects[0];

		/* test folder to file */
		IFolder folder = project.getFolder("folder");
		folder.create(true, true, null);
		ensureDoesNotExistInFileSystem(folder);
		//
		IFile file = project.getFile("folder");
		ensureExistsInFileSystem(file);
		//
		assertTrue("1.1", folder.exists());
		folder.refreshLocal(IResource.DEPTH_ZERO, null);
		assertTrue("1.2", !folder.exists());
		assertTrue("1.3", file.exists());
	}

	public void testRefreshFolder() throws Throwable {
		/* initialize common objects */
		IProject project = projects[0];

		/* test deletion of a child */
		IFile file = project.getFile("file");
		final IFile hackFile = file;
		final Workspace workspace = (Workspace) getWorkspace();
		IWorkspaceRunnable operation = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				workspace.createResource(hackFile, false);
				((Resource) hackFile).getResourceInfo(false, true).set(M_LOCAL_EXISTS);
			}
		};
		workspace.run(operation, null);
		assertTrue("1.0", file.exists());
		assertTrue("1.1", file.isLocal(IResource.DEPTH_ZERO));
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		assertTrue("1.2", !file.exists());
		ensureDoesNotExistInWorkspace(file);
		ensureDoesNotExistInFileSystem(file);

		/* test creation of a child */
		file = project.getFile("file");
		ensureExistsInFileSystem(file);
		assertTrue("2.0", !file.exists());
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		assertTrue("2.1", file.exists());
		ensureDoesNotExistInWorkspace(file);
		ensureDoesNotExistInFileSystem(file);

		/* test changes of a child (child is folder) */
		IFolder folder = project.getFolder("folder");
		folder.create(true, true, null);
		file = folder.getFile("file");
		ensureExistsInFileSystem(file);
		assertTrue("3.1", folder.exists());
		assertTrue("3.2", folder.isLocal(IResource.DEPTH_ZERO));
		assertTrue("3.3", !file.exists());
		folder.refreshLocal(IResource.DEPTH_ZERO, null);
		assertTrue("3.4", folder.exists());
		assertTrue("3.5", folder.isLocal(IResource.DEPTH_ZERO));
		assertTrue("3.6", !file.exists());
		folder.refreshLocal(IResource.DEPTH_ONE, null);
		assertTrue("3.7", folder.exists());
		assertTrue("3.8", folder.isLocal(IResource.DEPTH_ZERO));
		assertTrue("3.9", file.exists());
		ensureDoesNotExistInWorkspace(folder);
		ensureDoesNotExistInFileSystem(folder);

		/* test changes of a child (child is file) */
		file = project.getFile("file");
		ensureExistsInWorkspace(file, true);
		assertTrue("4.1", file.exists());
		assertTrue("4.2", file.isLocal(IResource.DEPTH_ZERO));
		assertEquals("4.3", CoreFileSystemLibrary.getLastModified(file.getLocation().toOSString()), ((Resource) file).getResourceInfo(false, false).getLocalSyncInfo());
		ensureOutOfSync(file);
		assertTrue("4.4", ((Resource) file).getResourceInfo(false, false).getLocalSyncInfo() != CoreFileSystemLibrary.getLastModified(file.getLocation().toOSString()));
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		assertEquals("4.5", CoreFileSystemLibrary.getLastModified(file.getLocation().toOSString()), ((Resource) file).getResourceInfo(false, false).getLocalSyncInfo());
		ensureDoesNotExistInWorkspace(file);
		ensureDoesNotExistInFileSystem(file);
	}

	public void testSimpleRefresh() throws Throwable {
		/* initialize common objects */
		IProject project = projects[0];

		/* test root deletion */
		IFile file = project.getFile("file");
		File target = file.getLocation().toFile();
		ensureExistsInWorkspace(file, true);
		ensureDoesNotExistInFileSystem(file);
		assertTrue("1.0", file.exists());
		file.refreshLocal(IResource.DEPTH_INFINITE, null);
		assertTrue("1.1", !file.exists());

		/* test root and children creation */
		IFolder folder = project.getFolder("folder");
		target = folder.getLocation().toFile();
		createTree(getTree(target));
		assertTrue("2.0", !folder.exists());
		folder.refreshLocal(IResource.DEPTH_INFINITE, null);
		assertTrue("2.1", folder.exists());
		assertTrue("2.2", ((Resource) folder).countResources(IResource.DEPTH_INFINITE, false) == (getTree(target).length + 1));
	}
}
/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.internal.localstore;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
//
public class RefreshLocalTest extends LocalStoreTest implements ICoreConstants {
public RefreshLocalTest() {
	super();
}
public RefreshLocalTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(RefreshLocalTest.class);
}
public void testFileToFolder() throws Throwable {
	/* initialize common objects */
	IProject project = projects[0];

	/* */
	IFile file = project.getFile("file");
	file.create(null, true, null);
	ensureDoesNotExistInFileSystem(file);
	//
	Thread.sleep(sleepTime);
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
	Thread.sleep(sleepTime);
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
	final Workspace workspace = (Workspace) this.getWorkspace();
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
	assertEquals("4.3", file.getLocation().toFile().lastModified(), ((Resource) file).getResourceInfo(false, false).getLocalSyncInfo());
	Thread.sleep(sleepTime);
	ensureExistsInFileSystem(file);
	assertTrue("4.4", ((Resource) file).getResourceInfo(false, false).getLocalSyncInfo() != file.getLocation().toFile().lastModified());
	project.refreshLocal(IResource.DEPTH_INFINITE, null);
	assertEquals("4.5", file.getLocation().toFile().lastModified(), ((Resource) file).getResourceInfo(false, false).getLocalSyncInfo());
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

package org.eclipse.core.tests.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
///
public class LocalSyncTest extends LocalStoreTest implements ICoreConstants {
public LocalSyncTest() {
	super();
}
public LocalSyncTest(String name) {
	super(name);
}
public void assertExistsInFileSystemWithNoContent(IFile target) throws CoreException {
	assertTrue(existsInFileSystemWithNoContent(target));
}
public String[] defineHierarchy() {
	return new String[] {
		"/File1",
		"/Folder1/",
		"/Folder1/File1",
		"/Folder1/Folder2/"
	};
}
private boolean existsInFileSystemWithNoContent(IResource resource) throws CoreException {
	IPath path = resource.getLocation();
	return path.toFile().exists() && path.toFile().length() == 0;
}
public static Test suite() {
	TestSuite suite = new TestSuite(LocalSyncTest.class);
	return suite;
}
public void testProjectDeletion() throws Throwable {
	/* initialize common objects */
	Project project = (Project)projects[0];

	// create resources
	IResource[] resources = buildResources(project, defineHierarchy());
	ensureExistsInWorkspace(resources, true);

	// delete project's default directory
	Workspace.clear(project.getLocation().toFile());

	// run synchronize
	//The .project file has been deleted, so this will fail
	try {
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		fail("1.0");
	} catch (CoreException e) {
	}

	/* project should still exists */
	assertTrue("1.1", project.exists());

	/* resources should not exist anymore */
	for (int i = 1; i < resources.length; i++)
		assertTrue("1.2", !resources[i].exists());
}
public void testProjectWithNoResources() throws Throwable {
	/* initialize common objects */
	Project project = (Project)projects[0];

	/* check normal behaviour */
	project.refreshLocal(IResource.DEPTH_INFINITE, null);
	assertTrue("1.1", project.exists());
}
/**
 * Simple synchronization test. Uses one solution and one project.
 */
public void testSimpleSync() throws Throwable {
	/* initialize common objects */
	Project project = (Project) projects[0];

	// create resource handles
	IResource index = project.getFile(new Path("index.html"));
	IResource toc = project.getFile(new Path("toc.html"));
	IResource file = project.getFile(new Path("file"));
	IResource folder = project.getFolder(new Path("folder"));

	// add resources to the workspace
	ensureExistsInWorkspace((IFile) index, "");
	ensureExistsInWorkspace((IFile) toc, true);
	ensureExistsInWorkspace((IFile) file, "");
	ensureExistsInWorkspace((IFolder) folder, true);

	// run synchronize
	project.refreshLocal(IResource.DEPTH_INFINITE, null); // test

	//-----------------------------------------------------------
	// test synchronize
	assertExistsInWorkspace(index);
	assertExistsInFileSystemWithNoContent((IFile) index);
	//
	assertExistsInWorkspace(toc);
	assertExistsInFileSystem(toc);
	assertTrue(toc.exists());
	//
	assertExistsInWorkspace(file);
	assertExistsInFileSystemWithNoContent((IFile) file);
	assertTrue(file.exists());
	assertTrue(file.getType() == IResource.FILE);
	//
	assertExistsInWorkspace(folder);
	assertTrue(folder.getType() == IResource.FOLDER);
	//-----------------------------------------------------------

	// make some modifications in the local resources
	// index stays the same
	ensureDoesNotExistInFileSystem(toc);
	//
	ensureDoesNotExistInFileSystem(file);
	ensureDoesNotExistInFileSystem(folder);
	Thread.sleep(5000);
	file = project.getFolder(new Path("file"));
	folder = project.getFile(new Path("folder"));
	ensureExistsInFileSystem((IFolder) file);
	ensureExistsInFileSystem((IFile) folder);

	// run synchronize
	project.refreshLocal(IResource.DEPTH_INFINITE, null); // test

	//-----------------------------------------------------------
	// test synchronize
	assertExistsInWorkspace(index);
	assertExistsInFileSystemWithNoContent((IFile) index);
	//
	assertDoesNotExistInFileSystem((IFile) toc);
	assertDoesNotExistInWorkspace(toc);
	//
	assertExistsInWorkspace(file);
	assertExistsInFileSystem(file);
	assertTrue(file.exists());
	assertTrue(file.getType() == IResource.FOLDER);
	//
	assertExistsInWorkspace(folder);
	assertExistsInFileSystem(folder);
	assertTrue(file.exists());
	assertTrue(folder.getType() == IResource.FILE);
	//-----------------------------------------------------------
}
}

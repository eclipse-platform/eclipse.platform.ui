/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.localstore;

import java.io.InputStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

//
public class FileSystemResourceManagerTest extends LocalStoreTest implements ICoreConstants {
	public FileSystemResourceManagerTest() {
		super();
	}

	public FileSystemResourceManagerTest(String name) {
		super(name);
	}

	public String[] defineHierarchy() {
		return new String[] {"/Folder1/", "/Folder1/File1", "/Folder1/Folder2/", "/Folder1/Folder2/File2", "/Folder1/Folder2/Folder3/"};
	}

	public static Test suite() {
		return new TestSuite(FileSystemResourceManagerTest.class);
	}

	/**
	 */
	public void testContainerFor() throws Throwable {

		/* test null parameter */
		try {
			getLocalManager().containerForLocation(null);
			fail("1.1");
		} catch (RuntimeException e) {
			// expected
		}

		/* test normal conditions under default mapping */

		// project/target
		Path path = new Path("target");
		IFolder folder = projects[0].getFolder(path);
		IPath location = projects[0].getLocation().append(path);
		IFolder testFolder = (IFolder) getLocalManager().containerForLocation(location);
		assertTrue("2.1", folder.equals(testFolder));

		// project/folder/target
		path = new Path("folder/target");
		folder = projects[0].getFolder(path);
		location = projects[0].getLocation().append(path);
		testFolder = (IFolder) getLocalManager().containerForLocation(location);
		assertTrue("2.2", folder.equals(testFolder));

		// project/folder/folder/target
		path = new Path("folder/folder/target");
		folder = projects[0].getFolder(path);
		location = projects[0].getLocation().append(path);
		testFolder = (IFolder) getLocalManager().containerForLocation(location);
		assertTrue("2.3", folder.equals(testFolder));

		/* non-existent location */
		testFolder = (IFolder) getLocalManager().containerForLocation(new Path("../this/path/must/not/exist"));
		assertTrue("3.1", testFolder == null);
	}

	/**
	 * this test should move to FileTest
	 */
	public void testCreateFile() throws Throwable {
		/* initialize common objects */
		IProject project = projects[0];
		File file = (File) project.getFile("foo");
		/* common contents */
		String originalContent = "this string should not be equal the other";

		/* create file with flag false */
		try {
			file.create(getContents(originalContent), false, null);
		} catch (CoreException e) {
			fail("1.1", e);
		}
		assertTrue("1.2", file.exists());
		assertTrue("1.3", file.isLocal(IResource.DEPTH_ZERO));
		assertEquals("1.4", file.getStore().fetchInfo().getLastModified(), file.getResourceInfo(false, false).getLocalSyncInfo());
		try {
			assertTrue("1.5", compareContent(getContents(originalContent), getLocalManager().read(file, true, null)));
		} catch (CoreException e) {
			fail("1.6", e);
		}
	}

	/**
	 */
	public void testFileFor() throws Throwable {

		/* test null parameter */
		try {
			getLocalManager().fileForLocation(null);
			fail("1.1");
		} catch (RuntimeException e) {
			// expected
		}

		/* test normal conditions under default mapping */

		// project/file
		Path path = new Path("file");
		IFile file = projects[0].getFile(path);
		IPath location = projects[0].getLocation().append(path);
		IFile testFile = getLocalManager().fileForLocation(location);
		assertTrue("2.1", file.equals(testFile));

		// project/folder/file
		path = new Path("folder/file");
		file = projects[0].getFile(path);
		location = projects[0].getLocation().append(path);
		testFile = getLocalManager().fileForLocation(location);
		assertTrue("2.2", file.equals(testFile));

		// project/folder/folder/file
		path = new Path("folder/folder/file");
		file = projects[0].getFile(path);
		location = projects[0].getLocation().append(path);
		testFile = getLocalManager().fileForLocation(location);
		assertTrue("2.3", file.equals(testFile));

		/* non-existent location */
		testFile = getLocalManager().fileForLocation(new Path("../this/path/must/not/exist"));
		assertTrue("7.1", testFile == null);
	}

	public void testIsLocal() throws Throwable {
		/* initialize common objects */
		final IProject project = projects[0];

		// create resources
		IResource[] resources = buildResources(project, defineHierarchy());
		ensureExistsInWorkspace(resources, true);
		ensureDoesNotExistInFileSystem(resources);

		// exists
		assertTrue("1.0", project.isLocal(IResource.DEPTH_INFINITE)); // test

		// test the depth parameter
		final IFolder folder = project.getFolder("Folder1");
		IWorkspaceRunnable operation = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IResource[] members = folder.members();
				for (int i = 0; i < members.length; i++)
					((Resource) members[i]).getResourceInfo(false, true).clear(M_LOCAL_EXISTS);
			}
		};
		getWorkspace().run(operation, null);
		assertTrue("2.0", project.isLocal(IResource.DEPTH_ONE));
		assertTrue("3.0", folder.isLocal(IResource.DEPTH_ZERO));
		assertTrue("4.0", !folder.isLocal(IResource.DEPTH_INFINITE));

		// remove the trash
		ensureDoesNotExistInWorkspace(project);
	}

	/**
	 * Scenario:
	 *		1 solution
	 * 		3 projects
	 */
	public void testLocationFor() throws Throwable {

		/* test project */
		IPath location = projects[0].getLocation();
		assertTrue("2.1", location.equals(getLocalManager().locationFor(projects[0])));
		assertTrue("2.2", location.equals(getWorkspace().getRoot().getLocation().append(projects[0].getName())));
	}

	/**
	 * Scenario:
	 *		1 solution
	 * 		3 projects
	 */
	public void testResourcePathFor() throws Throwable {

		///* test null parameter */
		//try {
		//getLocalManager().resourcePathFor(null);
		//fail("1.1");
		//} catch (RuntimeException e) {
		//} catch (CoreException e) {
		//fail("1.2", e);
		//}

		///* test normal conditions under default mapping */

		//// project/file
		//Path path = new Path("file");
		//IFile file = projects[0].getFile(path);
		//IPath location = projects[0].getLocation().append(path);
		//IPath testPath = getLocalManager().resourcePathFor(location);
		//assert("2.1", file.getFullPath().equals(testPath));

		//// project/folder/file
		//path = new Path("folder/file");
		//file = projects[0].getFile(path);
		//location = projects[0].getLocation().append(path);
		//testPath = getLocalManager().resourcePathFor(location);
		//assert("2.2", file.getFullPath().equals(testPath));

		//// project/folder/folder/file
		//path = new Path("folder/folder/file");
		//file = projects[0].getFile(path);
		//location = projects[0].getLocation().append(path);
		//testPath = getLocalManager().resourcePathFor(location);
		//assert("2.3", file.getFullPath().equals(testPath));
	}

	public void testSynchronizeProject() throws Throwable {
		/* test DEPTH parameter */
		/* DEPTH_ZERO */
		IFile file = projects[0].getFile("file");
		ensureExistsInFileSystem(file);
		projects[0].refreshLocal(IResource.DEPTH_ZERO, null);
		assertTrue("1.1", !file.exists());
		/* DEPTH_ONE */
		IFolder folder = projects[0].getFolder("folder");
		IFolder subfolder = folder.getFolder("subfolder");
		IFile subfile = folder.getFile("subfile");
		ensureExistsInFileSystem(folder);
		ensureExistsInFileSystem(subfolder);
		ensureExistsInFileSystem(subfile);
		projects[0].refreshLocal(IResource.DEPTH_ONE, null);
		assertTrue("2.1", file.exists());
		assertTrue("2.2", folder.exists());
		assertTrue("2.3", !subfolder.exists());
		assertTrue("2.4", !subfile.exists());
		/* DEPTH_INFINITE */
		projects[0].refreshLocal(IResource.DEPTH_INFINITE, null);
		assertTrue("3.1", file.exists());
		assertTrue("3.2", folder.exists());
		assertTrue("3.3", subfolder.exists());
		assertTrue("3.4", subfile.exists());

		/* closed project */
		file = projects[0].getFile("closed");
		projects[0].close(null);
		ensureExistsInFileSystem(file);
		projects[0].open(null);
		assertTrue("4.1", !file.exists());
		projects[0].refreshLocal(IResource.DEPTH_INFINITE, null);
		assertTrue("4.2", file.exists());

		/* refreshing an inexisting project should do nothing */
		getWorkspace().getRoot().getProject("inexistingProject").refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	public void testWriteFile() {
		/* initialize common objects */
		IProject project = projects[0];
		IFile file = project.getFile("foo");
		ensureExistsInWorkspace(file, true);
		/* common contents */
		String originalContent = "this string should not be equal the other";
		String anotherContent = "and this string should not... well, you know...";
		InputStream original;
		InputStream another;

		/* write file for the first time */
		original = getContents(originalContent);
		try {
			write(file, original, true, null);
		} catch (CoreException e) {
			fail("1.0", e);
		}
		original = getContents(originalContent);
		try {
			assertTrue("1.1", compareContent(original, getLocalManager().read(file, true, null)));
		} catch (CoreException e) {
			fail("1.2", e);
		}

		/* test the overwrite parameter (false) */
		another = getContents(anotherContent);
		try {
			write(file, another, false, null);
		} catch (CoreException e) {
			fail("2.0", e);
		}
		another = getContents(anotherContent);
		try {
			assertTrue("2.1", compareContent(another, getLocalManager().read(file, true, null)));
		} catch (CoreException e) {
			fail("2.2", e);
		}

		/* test the overwrite parameter (true) */
		original = getContents(originalContent);
		try {
			write(file, original, true, null);
		} catch (CoreException e) {
			fail("3.0", e);
		}
		original = getContents(originalContent);
		try {
			assertTrue("3.1", compareContent(original, getLocalManager().read(file, true, null)));
		} catch (CoreException e) {
			fail("3.2", e);
		}

		/* test the overwrite parameter (false) */
		ensureOutOfSync(file);
		another = getContents(anotherContent);
		try {
			write(file, another, false, null);
			fail("4.0", null);
		} catch (CoreException e) {
			// expected
		}
		try {
			file.setContents(another, false, false, null);
			fail("4.3");
		} catch (CoreException e) {
			// expected
		}
		try {
			file.setContents(another, true, false, null);
		} catch (CoreException e) {
			fail("4.4", e);
		}

		/* test the overwrite parameter (false) */
		ensureDoesNotExistInFileSystem(file);
		another = getContents(anotherContent);
		try {
			write(file, another, false, null);
			fail("5.0");
		} catch (CoreException e) {
			// expected
		}

		/* remove trash */
		ensureDoesNotExistInWorkspace(project);
	}

	public void testWriteFolder() throws Throwable {
		/* initialize common objects */
		IProject project = projects[0];
		IFolder folder = project.getFolder("foo");
		ensureExistsInWorkspace(folder, true);
		boolean ok;

		/* existing file on destination */
		ensureDoesNotExistInFileSystem(folder);
		IFile file = project.getFile("foo");
		ensureExistsInFileSystem(file);
		/* force = true */
		ok = false;
		try {
			write(folder, true, null);
		} catch (CoreException e) {
			ok = true;
		}
		assertTrue("1.1", ok);
		/* force = false */
		ok = false;
		try {
			write(folder, false, null);
		} catch (CoreException e) {
			ok = true;
		}
		assertTrue("1.2", ok);
		ensureDoesNotExistInFileSystem(file);

		/* existing folder on destination */
		ensureExistsInFileSystem(folder);
		/* force = true */
		write(folder, true, null);
		assertTrue("2.1", folder.getLocation().toFile().isDirectory());
		/* force = false */
		ok = false;
		try {
			write(folder, false, null);
		} catch (CoreException e) {
			ok = true;
		}
		assertTrue("2.2", ok);
		ensureDoesNotExistInFileSystem(folder);

		/* inexisting resource on destination */
		/* force = true */
		write(folder, true, null);
		assertTrue("3.1", folder.getLocation().toFile().isDirectory());
		ensureDoesNotExistInFileSystem(folder);
		/* force = false */
		write(folder, false, null);
		assertTrue("3.1", folder.getLocation().toFile().isDirectory());
	}

	/**
	 * 1.2 - write a project with default local location
	 * 2.1 - delete project's local location
	 */
	public void testWriteProject() throws Throwable {
		/* initialize common objects */
		final IProject project = projects[0];
		IFile dotProject = project.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
		final IFileStore fileStore = ((Resource) project).getStore();
		// create project and then delete from file system
		// wrap in runnable to prevent snapshot from occurring in the middle.
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				ensureDoesNotExistInFileSystem(project);
				assertTrue("2.1", !fileStore.fetchInfo().isDirectory());
				//write project in a runnable, otherwise tree will be locked
				((Project) project).writeDescription(IResource.FORCE);
			}
		}, null);
		assertTrue("2.2", fileStore.fetchInfo().isDirectory());
		long lastModified = ((Resource) dotProject).getStore().fetchInfo().getLastModified();
		assertEquals("2.3", lastModified, ((Resource) project).getResourceInfo(false, false).getLocalSyncInfo());
	}

	protected void write(final IFile file, final InputStream contents, final boolean force, IProgressMonitor monitor) throws CoreException {
		IWorkspaceRunnable operation = new IWorkspaceRunnable() {
			public void run(IProgressMonitor pm) throws CoreException {
				int flags = force ? IResource.FORCE : IResource.NONE;
				IFileInfo info = ((Resource) file).getStore().fetchInfo();
				getLocalManager().write(file, contents, info, flags, false, null);
			}
		};
		getWorkspace().run(operation, null);
	}

	protected void write(final IFolder folder, final boolean force, IProgressMonitor monitor) throws CoreException {
		IWorkspaceRunnable operation = new IWorkspaceRunnable() {
			public void run(IProgressMonitor pm) throws CoreException {
				getLocalManager().write(folder, force, null);
			}
		};
		getWorkspace().run(operation, null);
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import java.io.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

/**
 * A parent container (projects and folders) would become out-of-sync if any of
 * its children could not be deleted for some reason. These platform-
 * specific test cases ensure that it does not happen.
 */
public class Bug_26294 extends EclipseWorkspaceTest {

	public Bug_26294(String name) {
		super(name);
	}
	public static Test suite() {
		return new TestSuite(Bug_26294.class);
	}
	/**
	 * Tries to delete an open project containing an irremovable file.
	 * Works only for Windows.
	 */
	public void testDeleteOpenProjectWindows() {
		if (!(BootLoader.getOS().equals(BootLoader.OS_WIN32)))
			return;

		IProject project = null;
		InputStream input = null;
		File projectRoot = null;
		try {
			IWorkspace workspace = getWorkspace();
			project = workspace.getRoot().getProject("MyProject");
			IFolder folder = project.getFolder("a_folder");
			IFile file1 = folder.getFile("file1.txt");
			IFile file2 = project.getFile("file2.txt");
			IFile file3 = folder.getFile("file3.txt");

			ensureExistsInWorkspace(new IResource[] { file1, file2, file3 }, true);
			projectRoot = project.getLocation().toFile();

			// opens a file so it cannot be removed on Windows
			try {
				input = file1.getContents();
			} catch (CoreException ce) {
				ce.printStackTrace();
				fail("1.0");
			}

			IFile projectFile = project.getFile(new Path(".project"));
			assertTrue("1.2", projectFile.exists());
			assertTrue("1.3", projectFile.isSynchronized(IResource.DEPTH_INFINITE));

			try {
				project.delete(IResource.FORCE, getMonitor());
				fail("2.0 - should have failed");
			} catch (CoreException ce) {
				// success - a file couldn't be removed
			}
			assertTrue("2.1", project.exists());
			assertTrue("2.2", file1.exists());
			assertTrue("2.3", !file2.exists());
			assertTrue("2.4", !file3.exists());
			assertTrue("2.5", folder.exists());
			assertTrue("2.6", !projectFile.exists());
			assertTrue("2.7", project.isSynchronized(IResource.DEPTH_INFINITE));

			try {
				input.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				fail("3.0", ioe);
			}

			assertTrue("3.5", project.isSynchronized(IResource.DEPTH_INFINITE));
			try {
				project.delete(IResource.FORCE, getMonitor());
			} catch (CoreException ce) {
				ce.printStackTrace();
				fail("4.0", ce);
			}

			assertTrue("5.1", !project.exists());
			assertTrue("5.2", !file1.exists());
			assertTrue("5.3", file1.isSynchronized(IResource.DEPTH_INFINITE));
			assertTrue("5.4", project.isSynchronized(IResource.DEPTH_INFINITE));

			assertTrue("6.0", !projectRoot.exists());
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException e) {
				fail("7.0", e);
			} finally {
				if (projectRoot != null)
					ensureDoesNotExistInFileSystem(projectRoot);
			}
		}
	}
	/**
	 * Tries to delete an open project containing an irremovable file.
	 * Works only for Linux with natives.
	 */
	public void testDeleteOpenProjectLinux() {
		if (!(BootLoader.getOS().equals(BootLoader.OS_LINUX) && CoreFileSystemLibrary.usingNatives()))
			return;

		IProject project = null;
		File projectRoot = null;
		IFolder folder = null;
		try {
			IWorkspace workspace = getWorkspace();
			project = workspace.getRoot().getProject("MyProject");
			folder = project.getFolder("a_folder");
			IFile file1 = folder.getFile("file1.txt");
			IFile file2 = project.getFile("file2.txt");

			ensureExistsInWorkspace(new IResource[] { file1, file2 }, true);
			projectRoot = project.getLocation().toFile();

			// marks folder as read-only so its files cannot be deleted on Linux
			folder.setReadOnly(true);

			IFile projectFile = project.getFile(new Path(".project"));
			assertTrue("1.2", projectFile.exists());
			assertTrue("1.3", projectFile.isSynchronized(IResource.DEPTH_INFINITE));

			try {
				project.delete(IResource.FORCE, getMonitor());
				fail("2.0 - should have failed");
			} catch (CoreException ce) {
				// success - a file couldn't be removed
			}
			assertTrue("2.1", project.exists());
			assertTrue("2.2", file1.exists());
			assertTrue("2.3", !file2.exists());
			assertTrue("2.5", folder.exists());
			assertTrue("2.6", !projectFile.exists());
			assertTrue("2.7", project.isSynchronized(IResource.DEPTH_INFINITE));

			folder.setReadOnly(false);

			assertTrue("3.5", project.isSynchronized(IResource.DEPTH_INFINITE));
			try {
				project.delete(IResource.FORCE, getMonitor());
			} catch (CoreException ce) {
				ce.printStackTrace();
				fail("4.0", ce);
			}

			assertTrue("5.1", !project.exists());
			assertTrue("5.2", !file1.exists());
			assertTrue("5.3", file1.isSynchronized(IResource.DEPTH_INFINITE));
			assertTrue("5.4", project.isSynchronized(IResource.DEPTH_INFINITE));

			assertTrue("6.0", !projectRoot.exists());
		} finally {
			if (folder != null && folder.exists())
				folder.setReadOnly(false);
			if (projectRoot != null)
				ensureDoesNotExistInFileSystem(projectRoot);
		}
	}

	/**
	 * Tries to delete a closed project containing an irremovable file.
	 * Works only for Windows.
	 */
	public void testDeleteClosedProjectWindows() {
		if (!BootLoader.getOS().equals(BootLoader.OS_WIN32))
			return;

		IProject project = null;
		InputStream input = null;
		File projectRoot = null;
		IFile file1 = null;
		try {
			IWorkspace workspace = getWorkspace();
			project = workspace.getRoot().getProject("MyProject");
			IFolder folder = project.getFolder("a_folder");
			file1 = folder.getFile("file1.txt");
			IFile file2 = project.getFile("file2.txt");
			IFile file3 = folder.getFile("file3.txt");

			ensureExistsInWorkspace(new IResource[] { file1, file2, file3 }, true);

			projectRoot = project.getLocation().toFile();

			// opens a file so it cannot be removed on Windows
			try {
				input = file1.getContents();
			} catch (CoreException ce) {
				ce.printStackTrace();
				fail("1.0");
			}

			try {
				project.close(getMonitor());
			} catch (CoreException e) {
				fail("1.1", e);
			}

			try {
				project.delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, getMonitor());
				fail("2.0 - should have failed");
			} catch (CoreException ce) {
				// success - a file couldn't be removed
			}
			assertTrue("2.1", project.exists());
			assertTrue("2.7", project.isSynchronized(IResource.DEPTH_INFINITE));

			try {
				input.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				fail("3.0", ioe);
			}

			assertTrue("3.5", project.isSynchronized(IResource.DEPTH_INFINITE));
			try {
				project.delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, getMonitor());
			} catch (CoreException ce) {
				ce.printStackTrace();
				fail("4.0", ce);
			}

			assertTrue("5.1", !project.exists());
			assertTrue("5.3", project.isSynchronized(IResource.DEPTH_INFINITE));
			assertTrue("6.0", !projectRoot.exists());
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException e) {
				fail("7.0", e);
			} finally {
				if (projectRoot != null)
					ensureDoesNotExistInFileSystem(projectRoot);
			}
		}
	}
	/**
	 * Tries to delete a closed project containing an irremovable file.
	 * Works only for Linux with natives.
	 */
	public void testDeleteClosedProjectLinux() {
		if (!(BootLoader.getOS().equals(BootLoader.OS_LINUX) && CoreFileSystemLibrary.usingNatives()))
			return;

		IProject project = null;
		File projectRoot = null;
		IFolder folder = null;
		try {
			IWorkspace workspace = getWorkspace();
			project = workspace.getRoot().getProject("MyProject");
			folder = project.getFolder("a_folder");
			IFile file1 = folder.getFile("file1.txt");
			IFile file2 = project.getFile("file2.txt");

			ensureExistsInWorkspace(new IResource[] { file1, file2 }, true);

			projectRoot = project.getLocation().toFile();

			// marks folder as read-only so its files cannot be removed on Linux
			folder.setReadOnly(true);

			try {
				project.close(getMonitor());
			} catch (CoreException e) {
				fail("1.1", e);
			}

			try {
				project.delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, getMonitor());
				fail("2.0 - should have failed");
			} catch (CoreException ce) {
				// success - a file couldn't be removed
			}
			assertTrue("2.1", project.exists());
			assertTrue("2.7", project.isSynchronized(IResource.DEPTH_INFINITE));

			folder.setReadOnly(false);

			assertTrue("3.5", project.isSynchronized(IResource.DEPTH_INFINITE));
			try {
				project.delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, getMonitor());
			} catch (CoreException ce) {
				ce.printStackTrace();
				fail("4.0", ce);
			}

			assertTrue("5.1", !project.exists());
			assertTrue("5.3", project.isSynchronized(IResource.DEPTH_INFINITE));
			assertTrue("6.0", !projectRoot.exists());
		} finally {
			if (folder != null && folder.exists())
				folder.setReadOnly(false);
			if (projectRoot != null)
				ensureDoesNotExistInFileSystem(projectRoot);
		}
	}

	/**
	 * Tries to delete a folder containing an irremovable file.
	 * Works only for Windows.
	 */
	public void testDeleteFolderWindows() {
		if (!BootLoader.getOS().equals(BootLoader.OS_WIN32))
			return;

		IProject project = null;
		InputStream input = null;
		File projectRoot = null;
		try {
			IWorkspace workspace = getWorkspace();
			project = workspace.getRoot().getProject("MyProject");
			IFolder folder = project.getFolder("a_folder");
			IFile file1 = folder.getFile("file1.txt");
			IFile file3 = folder.getFile("file3.txt");

			ensureExistsInWorkspace(new IResource[] { file1, file3 }, true);
			projectRoot = project.getLocation().toFile();

			// opens a file so it cannot be removed on Windows
			try {
				input = file1.getContents();
			} catch (CoreException ce) {
				ce.printStackTrace();
				fail("1.0");
			}

			try {
				folder.delete(IResource.FORCE, getMonitor());
				fail("2.0 - should have failed");
			} catch (CoreException ce) {
				// success - a file couldn't be removed			
			}
			assertTrue("2.2", file1.exists());
			assertTrue("2.4", !file3.exists());
			assertTrue("2.5", folder.exists());
			assertTrue("2.7", folder.isSynchronized(IResource.DEPTH_INFINITE));

			try {
				input.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				fail("3.0", ioe);
			}

			assertTrue("3.5", project.isSynchronized(IResource.DEPTH_INFINITE));
			try {
				folder.delete(IResource.FORCE, getMonitor());
			} catch (CoreException ce) {
				ce.printStackTrace();
				fail("4.0", ce);
			}
			assertTrue("5.1", !file1.exists());
			assertTrue("5.2", !folder.exists());
			assertTrue("5.3", file1.isSynchronized(IResource.DEPTH_INFINITE));
			assertTrue("5.4", folder.isSynchronized(IResource.DEPTH_INFINITE));
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException e) {
				fail("7.0", e);
			} finally {
				if (projectRoot != null)
					ensureDoesNotExistInFileSystem(projectRoot);
			}
		}
	}
	/**
	 * Tries to delete a folder containing an irremovable file.
	 * Works only for Linux with natives.
	 */
	public void testDeleteFolderLinux() {
		if (!(BootLoader.getOS().equals(BootLoader.OS_LINUX) && CoreFileSystemLibrary.usingNatives()))
			return;

		IProject project = null;
		File projectRoot = null;
		IFolder subFolder = null;
		try {
			IWorkspace workspace = getWorkspace();
			project = workspace.getRoot().getProject("MyProject");
			IFolder folder = project.getFolder("a_folder");
			subFolder = folder.getFolder("sub-folder");
			IFile file1 = subFolder.getFile("file1.txt");
			IFile file3 = folder.getFile("file3.txt");

			ensureExistsInWorkspace(new IResource[] { file1, file3 }, true);
			projectRoot = project.getLocation().toFile();

			// marks sub-folder as read-only so its files cannot be removed on Linux
			subFolder.setReadOnly(true);

			try {
				folder.delete(IResource.FORCE, getMonitor());
				fail("2.0 - should have failed");
			} catch (CoreException ce) {
				// success - a file couldn't be removed			
			}
			assertTrue("2.2", file1.exists());
			assertTrue("2.3", subFolder.exists());
			assertTrue("2.4", !file3.exists());
			assertTrue("2.5", folder.exists());
			assertTrue("2.7", folder.isSynchronized(IResource.DEPTH_INFINITE));

			subFolder.setReadOnly(false);

			assertTrue("3.5", project.isSynchronized(IResource.DEPTH_INFINITE));
			try {
				folder.delete(IResource.FORCE, getMonitor());
			} catch (CoreException ce) {
				ce.printStackTrace();
				fail("4.0", ce);
			}
			assertTrue("5.1", !file1.exists());
			assertTrue("5.2", !subFolder.exists());
			assertTrue("5.3", !folder.exists());
			assertTrue("5.4", file1.isSynchronized(IResource.DEPTH_INFINITE));
			assertTrue("5.5", folder.isSynchronized(IResource.DEPTH_INFINITE));
		} finally {
			if (subFolder != null && subFolder.exists())
				subFolder.setReadOnly(false);
			if (projectRoot != null)
				ensureDoesNotExistInFileSystem(projectRoot);
		}
	}

	/**
	 * @see org.eclipse.core.tests.harness.EclipseWorkspaceTest#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}

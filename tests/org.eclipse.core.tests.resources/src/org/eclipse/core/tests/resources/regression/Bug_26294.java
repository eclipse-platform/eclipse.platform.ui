/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.resources.regression;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class Bug_26294 extends EclipseWorkspaceTest {

	public Bug_26294(String name) {
		super(name);
	}
	public static Test suite() {
		return new TestSuite(Bug_26294.class);
	}
	/**
	 * Tries to delete a closed project containing an open file.
	 * Works only for Windows or Linux with natives.
	 */
	public void testDeleteOpenProject() {
		
		boolean windows = BootLoader.getOS() == BootLoader.OS_WIN32;
		boolean linuxWithNatives = BootLoader.getOS() == BootLoader.OS_LINUX && CoreFileSystemLibrary.usingNatives(); 
		if (!windows && !linuxWithNatives)
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

			if (windows)
				// opens a file so it cannot be removed
				try {
					input = file1.getContents();
				} catch (CoreException ce) {
					ce.printStackTrace();
					fail("1.0");
				}
			else
				file1.setReadOnly(true);

			IFile projectFile = project.getFile(new Path(".project"));
			assertTrue("1.2", projectFile.exists());
			assertTrue("1.3", projectFile.isSynchronized(IResource.DEPTH_INFINITE));
			
			try {
				project.delete(IResource.FORCE, getMonitor());
				fail("2.0 - should have failed");				
			} catch (CoreException ce) {				
				// success - a file was open			
			}
			assertTrue("2.1", project.exists());
			assertTrue("2.2", file1.exists());
			assertTrue("2.3", !file2.exists());
			assertTrue("2.4", !file3.exists());
			assertTrue("2.5", folder.exists());
			assertTrue("2.6", !projectFile.exists());
			assertTrue("2.7", project.isSynchronized(IResource.DEPTH_INFINITE));

			if (windows)
				try {
					input.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
					fail("3.0", ioe);
				}
			else
				file1.setReadOnly(false);
				
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
				if (linuxWithNatives && file1 != null && file1.exists())
					file1.setReadOnly(false);
				if (input != null)
					input.close();									
			} catch (IOException e) {
				fail("7.0", e);
			} finally {
				ensureDoesNotExistInFileSystem(projectRoot);
			}			
		}
	}
	/**
	 * Tries to delete a closed project containing an open file (keeps root directory).
	 */
	public void testDeleteClosedProject() {
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
			
			// opens a file so it cannot be removed
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
				// success - a file was open
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
				project.delete(IResource.FORCE| IResource.ALWAYS_DELETE_PROJECT_CONTENT, getMonitor());
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
				fail("6.0", e);
			} finally {
				ensureDoesNotExistInFileSystem(projectRoot);
			}
		}
	}
	/**
	 * @see org.eclipse.core.tests.harness.EclipseWorkspaceTest#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	
}
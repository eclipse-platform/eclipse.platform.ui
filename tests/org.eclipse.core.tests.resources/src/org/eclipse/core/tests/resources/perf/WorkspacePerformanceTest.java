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
package org.eclipse.core.tests.resources.perf;

import java.io.ByteArrayInputStream;
import java.util.Random;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.tests.harness.PerformanceTestRunner;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Basic performance calculations for standard workspace operations.
 */
public class WorkspacePerformanceTest extends ResourceTest {
	private static final String chars = "abcdefghijklmnopqrstuvwxyz";
	static final int REPEATS = 5;
	private static final int TOTAL_RESOURCES = 10000;
	private static final int TREE_WIDTH = 10;

	private final Random random = new Random();
	IFolder testFolder;
	IProject testProject;

	public static Test suite() {
		return new TestSuite(WorkspacePerformanceTest.class);
	}

	public WorkspacePerformanceTest() {
		super();
	}

	public WorkspacePerformanceTest(String name) {
		super(name);
	}

	IFolder copyFolder() {
		IFolder destination = testProject.getFolder("CopyDestination");
		try {
			testFolder.copy(destination.getFullPath(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("Failed to copy project in performance test", e);
		}
		return destination;
	}

	/**
	 * Creates a project and fills it with contents
	 */
	void createAndPopulateProject() {
		try {
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					testProject.create(getMonitor());
					testProject.open(getMonitor());
					createFolder(testFolder);
				}
			}, getMonitor());
		} catch (CoreException e) {
			fail("Failed to create project in performance test", e);
		}
	}

	private byte[] createBytes(int length) {
		byte[] bytes = new byte[length];
		random.nextBytes(bytes);
		return bytes;
	}

	/**
	 * Creates and returns a folder with lots of contents
	 */
	IFolder createFolder(IFolder topFolder) throws CoreException {
		topFolder.create(IResource.NONE, true, getMonitor());
		//tree depth is log of total resource count with the width as the log base
		int depth = (int) (Math.log(TOTAL_RESOURCES) / Math.log(TREE_WIDTH));
		recursiveCreateChildren(topFolder, depth - 1);
		return topFolder;
	}

	private String createString(int length) {
		StringBuffer buf = new StringBuffer(length);
		//fill the string with random characters up to the desired length
		for (int i = 0; i < length; i++) {
			buf.append(chars.charAt(random.nextInt(chars.length())));
		}
		return buf.toString();
	}

	IFolder moveFolder() {
		IFolder destination = testFolder.getProject().getFolder("MoveDestination");
		try {
			testFolder.move(destination.getFullPath(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("Failed to move folder during performance test", e);
		}
		return destination;
	}

	/**
	 * Create children of the given folder, and recurse to the given depth
	 */
	private void recursiveCreateChildren(IFolder parentFolder, int depth) throws CoreException {
		//create TREE_WIDTH files
		for (int i = 0; i < TREE_WIDTH; i++) {
			IFile file = parentFolder.getFile(createString(10));
			file.create(new ByteArrayInputStream(createBytes(5000)), IResource.NONE, getMonitor());
		}
		if (depth <= 0)
			return;
		//create TREE_WIDTH folders
		for (int i = 0; i < TREE_WIDTH; i++) {
			IFolder folder = parentFolder.getFolder(createString(6));
			folder.create(IResource.NONE, true, getMonitor());
			recursiveCreateChildren(folder, depth - 1);
		}
	}

	protected void setUp() throws Exception {
		testProject = getWorkspace().getRoot().getProject("Project");
		testFolder = testProject.getFolder("TopFolder");
	}

	/**
	 * Benchmark test of creating a project and populating it with folders and files.
	 */
	public void testCreateResources() {
		new PerformanceTestRunner() {
			protected void setUp() {
				waitForBackgroundActivity();
			}

			protected void tearDown() throws CoreException {
				testProject.delete(IResource.FORCE, null);
			}

			protected void test() {
				createAndPopulateProject();
			}
		}.run(this, REPEATS, 1);
	}

	public void testDeleteProject() {
		//create the project contents
		new PerformanceTestRunner() {
			protected void setUp() {
				createAndPopulateProject();
				waitForBackgroundActivity();
			}

			protected void test() {
				try {
					testProject.delete(IResource.NONE, null);
				} catch (CoreException e) {
					fail("Failed to delete project during performance test", e);
				}
			}
		}.run(this, REPEATS, 1);
	}

	public void testFolderCopy() {
		//create the project contents
		new PerformanceTestRunner() {
			protected void setUp() {
				createAndPopulateProject();
				waitForBackgroundActivity();
			}

			protected void tearDown() throws CoreException {
				testProject.delete(IResource.FORCE, null);
			}

			protected void test() {
				copyFolder();
			}
		}.run(this, REPEATS, 1);
	}

	public void testFolderMove() {
		//create the project contents
		new PerformanceTestRunner() {
			protected void setUp() {
				createAndPopulateProject();
				waitForBackgroundActivity();
			}

			protected void tearDown() throws CoreException {
				testProject.delete(IResource.FORCE, null);
			}

			protected void test() {
				moveFolder();
			}
		}.run(this, REPEATS, 1);
	}

	/**
	 * Waits until background activity settles down before running a performance test.
	 *
	 */
	public void waitForBackgroundActivity() {
		waitForRefresh();
		waitForBuild();
	}
}
/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.perf;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.internal.localstore.HistoryStoreTest;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Contains a set of use case-oriented performance tests for the local history.
 *  
 * @since 3.1
 */
public class LocalHistoryPerformanceTest extends ResourceTest {

	private IWorkspaceDescription original;

	public static Test suite() {
		//		TestSuite suite = new TestSuite(LocalHistoryPerformanceTest.class.getName());
		//		suite.addTest(new LocalHistoryPerformanceTest("testCopyHistory100x4"));		
		//		suite.addTest(new LocalHistoryPerformanceTest("testCopyHistory20x20"));
		//		suite.addTest(new LocalHistoryPerformanceTest("testCopyHistory4x100"));
		//		suite.addTest(new LocalHistoryPerformanceTest("testGetHistory"));
		//		return suite;
		return new TestSuite(LocalHistoryPerformanceTest.class);
	}

	public LocalHistoryPerformanceTest(String name) {
		super(name);
	}

	/**
	 * Creates a tree of resources containing history. 
	 */
	private void createTree(IFolder base, int filesPerFolder, int statesPerFile) {
		IFolder[] folders = new IFolder[5];
		folders[0] = base.getFolder("folder1");
		folders[1] = base.getFolder("folder2");
		folders[2] = folders[0].getFolder("folder3");
		folders[3] = folders[2].getFolder("folder4");
		folders[4] = folders[3].getFolder("folder5");
		ensureExistsInWorkspace(folders, true);
		for (int i = 0; i < folders.length; i++) {
			for (int j = 0; j < filesPerFolder; j++) {
				IFile file = folders[i].getFile("file" + j);
				ensureExistsInWorkspace(file, getRandomContents());
				try {
					for (int k = 0; k < statesPerFile; k++)
						file.setContents(getRandomContents(), IResource.KEEP_HISTORY, getMonitor());
				} catch (CoreException ce) {
					fail("0.5", ce);
				}
			}
		}
	}

	private IWorkspaceDescription setMaxFileStates(String failureMessage, int maxFileStates) {
		IWorkspaceDescription currentDescription = getWorkspace().getDescription();
		IWorkspaceDescription testDescription = getWorkspace().getDescription();
		testDescription.setMaxFileStates(maxFileStates);
		try {
			getWorkspace().setDescription(testDescription);
		} catch (CoreException e) {
			fail(failureMessage, e);
		}
		return currentDescription;
	}

	protected void setUp() throws Exception {
		super.setUp();
		original = getWorkspace().getDescription();
	}

	protected void tearDown() throws Exception {
		getWorkspace().setDescription(original);
		super.tearDown();
		HistoryStoreTest.wipeHistoryStore(getMonitor());
	}

	public void testAddState() {
		setMaxFileStates("0.01", 100);
		final IFile file = getWorkspace().getRoot().getProject("proj1").getFile("file.txt");
		new CorePerformanceTest() {

			protected void operation() {
				try {
					file.setContents(getRandomContents(), IResource.KEEP_HISTORY, getMonitor());
				} catch (CoreException e) {
					fail("", e);
				}
			}

			protected void setup() {
				ensureExistsInWorkspace(file, getRandomContents());
			}

			protected void teardown() {
				try {
					file.clearHistory(getMonitor());
					file.delete(IResource.FORCE, getMonitor());
				} catch (CoreException e) {
					fail("1.0", e);
				}
			}
		}.run(LocalHistoryPerformanceTest.this, 10, 30);
	}

	private void testClearHistory(final int filesPerFolder, final int statesPerFile) {
		IProject project = getWorkspace().getRoot().getProject("proj1");
		final IFolder base = project.getFolder("base");
		ensureDoesNotExistInWorkspace(base);
		new CorePerformanceTest() {

			protected void operation() {
				try {
					base.clearHistory(getMonitor());
				} catch (CoreException e) {
					fail("", e);
				}
			}

			protected void setup() {
				createTree(base, filesPerFolder, statesPerFile);
				ensureDoesNotExistInWorkspace(base);
			}
		}.run(this, 4, 3);
	}

	public void testClearHistory100x4() {
		testClearHistory(100, 4);
	}

	public void testClearHistory20x20() {
		testClearHistory(20, 20);
	}

	public void testClearHistory4x100() {
		testClearHistory(4, 100);
	}

	private void testCopyHistory(int filesPerFolder, int statesPerFile) {
		IProject project = getWorkspace().getRoot().getProject("proj1");
		IFolder base = project.getFolder("base");
		createTree(base, filesPerFolder, statesPerFile);
		// need a final reference so the inner class can see it
		final IProject[] tmpProject = new IProject[] {project};
		new CorePerformanceTest() {
			protected void operation() {
				try {
					String newProjectName = getUniqueString();
					IProject newProject = getWorkspace().getRoot().getProject(newProjectName);
					tmpProject[0].copy(newProject.getFullPath(), true, getMonitor());
					tmpProject[0] = newProject;
				} catch (CoreException e) {
					fail("", e);
				}
			}
		}.run(this, 10, 1);
	}

	public void testCopyHistory100x4() {
		testCopyHistory(100, 4);
	}

	public void testCopyHistory20x20() {
		testCopyHistory(20, 20);
	}

	public void testCopyHistory4x100() {
		testCopyHistory(4, 100);
	}

	private void testGetDeletedMembers(int filesPerFolder, int statesPerFile) {
		IProject project = getWorkspace().getRoot().getProject("proj1");
		IFolder base = project.getFolder("base");
		createTree(base, filesPerFolder, statesPerFile);
		ensureDoesNotExistInWorkspace(base);
		// need a final reference so the inner class can see it
		final IProject tmpProject = project;
		new CorePerformanceTest() {
			protected void operation() {
				try {
					tmpProject.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
				} catch (CoreException e) {
					fail("", e);
				}
			}
		}.run(this, 2, 5);
	}

	public void testGetDeletedMembers100x4() {
		testGetDeletedMembers(100, 4);
	}

	public void testGetDeletedMembers20x20() {
		testGetDeletedMembers(20, 20);
	}

	public void testGetDeletedMembers4x100() {
		testGetDeletedMembers(4, 100);
	}

	public void testGetHistory() {
		IProject project = getWorkspace().getRoot().getProject("proj1");
		final IFile file = project.getFile("file.txt");
		ensureExistsInWorkspace(file, getRandomContents());
		try {
			for (int i = 0; i < 100; i++)
				file.setContents(getRandomContents(), IResource.KEEP_HISTORY, getMonitor());
		} catch (CoreException ce) {
			fail("0.5", ce);
		}
		new CorePerformanceTest() {
			protected void operation() {
				try {
					file.getHistory(getMonitor());
				} catch (CoreException e) {
					fail("", e);
				}
			}
		}.run(this, 1, 150);
	}
}

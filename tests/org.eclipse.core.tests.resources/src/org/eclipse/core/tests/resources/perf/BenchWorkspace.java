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

import junit.framework.*;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.tests.harness.CorePerformanceTest;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class BenchWorkspace extends CorePerformanceTest {
	IProject project;
	private static final int NUM_FOLDERS = 400;//must be multiple of 10
	private static final int FILES_PER_FOLDER = 20;

	/**
	 * No-arg constructor to satisfy test harness.
	 */
	public BenchWorkspace() {
		super();
	}

	/**
	 * Standard test case constructor
	 */
	public BenchWorkspace(String testName) {
		super(testName);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(BenchWorkspace.class.getName());
		suite.addTest(new BenchWorkspace("benchCountResources"));
		return suite;
	}

	public void benchCountResources() {
		Workspace workspace = (Workspace) getWorkspace();
		int count = 0;
		startBench();
		for (int i = 0; i < 100; i++) {
			count = workspace.countResources(project.getFullPath(), IResource.DEPTH_INFINITE, true);
		}
		stopBench("benchCountResources", 100);
		System.out.println("Count: " + count);
	}

	public void benchCountResourcesDuringOperation() {
		final Workspace workspace = (Workspace) getWorkspace();
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				//touch all files
				workspace.getRoot().accept(new IResourceVisitor() {
					public boolean visit(IResource resource) throws CoreException {
						resource.touch(null);
						return true;
					}
				});
				//now count the resources in the dirty workspace
				int count = 0;
				startBench();
				for (int i = 0; i < 10; i++) {
					count = workspace.countResources(project.getFullPath(), IResource.DEPTH_INFINITE, true);
				}
				stopBench("benchCountResources", 10);
				System.out.println("Count: " + count);
			}
		};
		try {
			workspace.run(runnable, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
	}

	public String[] defineHierarchy() {
		//define a hierarchy with NUM_FOLDERS folders, NUM_FILES files.
		String[] names = new String[NUM_FOLDERS * (FILES_PER_FOLDER + 1)];
		//create the folders
		final int MAX_DEPTH = 10;
		final int MAX_SPAN = NUM_FOLDERS / MAX_DEPTH;
		int i = 0;
		for (int depth = 0; depth < MAX_DEPTH; depth++) {
			for (int span = 0; span < MAX_SPAN; span++) {
				if (depth == 0) {
					names[i] = "TestProject/" + Integer.toString(span) + "/";
				} else {
					names[i] = names[i - MAX_SPAN] + Integer.toString(span) + "/";
				}
				i++;
			}
		}
		//create files for each folder
		for (int folder = 0; folder < NUM_FOLDERS; folder++) {
			for (int file = 0; file < FILES_PER_FOLDER; file++) {
				names[i++] = names[folder] + "file" + Integer.toString(file);
			}
		}
		return names;
	}

	/**
	 * @see EclipseWorkspaceTest#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				//create resources
				project = getWorkspace().getRoot().getProject("TestProject");
				project.create(null);
				project.open(null);
				IResource[] resources = buildResources(project, defineHierarchy());
				ensureExistsInWorkspace(resources, true);
			}
		};
		try {
			getWorkspace().run(runnable, null);
		} catch (CoreException e) {
			fail("1.0", e);
		}
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		project.delete(true, true, null);
	}
}


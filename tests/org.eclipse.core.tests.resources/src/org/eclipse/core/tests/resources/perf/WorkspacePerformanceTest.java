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
import org.eclipse.core.tests.harness.CorePerformanceTest;

/**
 * Basic performance calculations for standard workspace operations.
 */
public class WorkspacePerformanceTest extends CorePerformanceTest {
	private static final String chars = "abcdefghijklmnopqrstuvwxyz";
	private static final String COPYING_TIMER = "COPYING_TIMER";
	private static final String CREATING_TIMER = "CREATING_TIMER";
	private static final String DELETING_TIMER = "DELETING_TIMER";
	private static final String MOVING_TIMER = "MOVING_TIMER";

	private static final String OVERALL_TIMER = "OVERALL_TIMER";
	private static final int TOTAL_RESOURCES = 10000;
	private static final int TREE_WIDTH = 10;

	public static Test suite() {
		return new TestSuite(WorkspacePerformanceTest.class);
	}

	private final Random random = new Random();

	public WorkspacePerformanceTest() {
		super();
	}

	public WorkspacePerformanceTest(String name) {
		super(name);
	}

	private IFolder copyFolder(IFolder source) throws CoreException {
		IFolder destination = source.getProject().getFolder("CopyDestination");
		source.copy(destination.getFullPath(), IResource.NONE, getMonitor());
		return destination;
	}

	private byte[] createBytes(int length) {
		byte[] bytes = new byte[length];
		random.nextBytes(bytes);
		return bytes;
	}

	/**
	 * Creates and returns a folder with lots of contents
	 */
	private IFolder createFolder(IFolder topFolder) throws CoreException {
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

	public void doTestWorkspaceOperations() throws CoreException {
		startTimer(OVERALL_TIMER);
		final IProject project = getWorkspace().getRoot().getProject("Project");
		project.create(getMonitor());
		project.open(getMonitor());
		final IFolder topFolder = project.getFolder("TopFolder");

		//create the project contents
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				startTimer(CREATING_TIMER);
				createFolder(topFolder);
				stopTimer(CREATING_TIMER);
			}
		}, getMonitor());

		//copy the project contents		
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				startTimer(COPYING_TIMER);
				copyFolder(topFolder);
				stopTimer(COPYING_TIMER);
			}
		}, getMonitor());

		//move the project contents
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				startTimer(MOVING_TIMER);
				moveFolder(topFolder);
				stopTimer(MOVING_TIMER);
			}
		}, getMonitor());

		//delete the project contents
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				startTimer(DELETING_TIMER);
				project.delete(IResource.ALWAYS_DELETE_PROJECT_CONTENT, getMonitor());
				stopTimer(DELETING_TIMER);
			}
		}, getMonitor());

		stopTimer(OVERALL_TIMER);
	}

	private IFolder moveFolder(IFolder source) throws CoreException {
		IFolder destination = source.getProject().getFolder("MoveDestination");
		source.move(destination.getFullPath(), IResource.NONE, getMonitor());
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

	public void testPerformance() {
		try {
			doTestWorkspaceOperations();
		} catch (CoreException e) {
			fail("1.99", e);
		}
	}
}
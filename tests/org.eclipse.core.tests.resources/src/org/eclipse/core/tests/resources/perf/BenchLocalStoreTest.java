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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.localstore.HistoryStore;
import org.eclipse.core.internal.localstore.TestingSupport;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.CorePerformanceTest;

public class BenchLocalStoreTest extends CorePerformanceTest {

	public BenchLocalStoreTest() {
		super();
	}

	public BenchLocalStoreTest(String name) {
		super(name);
	}

	public static Test suite() {
		//		TestSuite suite = new TestSuite();
		//		suite.addTest(new BenchLocalStoreTest("testBug28603"));
		//		return suite;
		return new TestSuite(BenchLocalStoreTest.class);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		wipeHistoryStore();
	}

	public void testBug28603() {
		// paths to mimic files in the workspace
		IProject project = getWorkspace().getRoot().getProject("myproject");
		IFolder folder1 = project.getFolder("myfolder1");
		IFolder folder2 = project.getFolder("myfolder2");
		IFile file1 = folder1.getFile("myfile.txt");
		IFile file2 = folder2.getFile(file1.getName());

		ensureExistsInWorkspace(new IResource[] {project, folder1, folder2}, true);
		try {
			file1.create(getRandomContents(), IResource.FORCE, getMonitor());
			file1.setContents(getRandomContents(), IResource.FORCE | IResource.KEEP_HISTORY, getMonitor());
			file1.setContents(getRandomContents(), IResource.FORCE | IResource.KEEP_HISTORY, getMonitor());
			file1.setContents(getRandomContents(), IResource.FORCE | IResource.KEEP_HISTORY, getMonitor());
		} catch (CoreException e) {
			fail("0.0", e);
		}
		int maxStates = ResourcesPlugin.getWorkspace().getDescription().getMaxFileStates();

		IFileState[] states = null;
		try {
			states = file1.getHistory(getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
		assertEquals("1.1", 3, states.length);
		int currentStates = 3;
		long totalMoveTime = 0L;
		int totalMoves = 0;

		for (int i = 0; i < maxStates + 10; i++) {
			long start = 0L;
			long stop = 0L;
			try {
				states = file1.getHistory(getMonitor());
			} catch (CoreException e) {
				fail("2.0", e);
			}
			assertEquals("2.1 file1 states", currentStates, states.length);
			try {
				start = System.currentTimeMillis();
				file1.move(file2.getFullPath(), true, true, getMonitor());
				stop = System.currentTimeMillis();
			} catch (CoreException e) {
				fail("2.2", e);
			}
			totalMoveTime += stop - start;
			totalMoves++;
			System.out.println("move: " + file1.getFullPath() + " to " + file2.getFullPath() + " took: " + (stop - start) + "ms");

			try {
				states = file2.getHistory(getMonitor());
			} catch (CoreException e) {
				fail("2.3", e);
			}
			currentStates = currentStates < maxStates ? currentStates + 1 : maxStates;
			assertEquals("2.4 file2 states", currentStates, states.length);
			try {
				start = System.currentTimeMillis();
				file2.move(file1.getFullPath(), true, true, getMonitor());
				stop = System.currentTimeMillis();
			} catch (CoreException e) {
				fail("2.5", e);
			}
			totalMoveTime += stop - start;
			totalMoves++;
			try {
				states = file1.getHistory(getMonitor());
			} catch (CoreException e) {
				fail("2.6", e);
			}
			currentStates = currentStates < maxStates ? currentStates + 1 : maxStates;
			assertEquals("2.7 file1 states", currentStates, states.length);

			System.out.println("move: " + file2.getFullPath() + " to " + file1.getFullPath() + " took: " + (stop - start) + "ms");
		}
		System.out.println("\nAverage move time = " + totalMoveTime / totalMoves + "ms over " + totalMoves + " moves.");

	}

	/*
	 * This little helper method makes sure that the history store is
	 * completely clean after it is invoked.  If a history store entry or
	 * a file is left, it may become part of the history for another file in
	 * another test (if this file has the same name).
	 */
	private void wipeHistoryStore() {
		HistoryStore store = ((Workspace) getWorkspace()).getFileSystemManager().getHistoryStore();
		// Remove all the entries from the history store index.  Note that
		// this does not cause the history store states to be removed.
		store.removeAll();
		// Now make sure all the states are really removed.
		TestingSupport.removeGarbage(store);
	}

}
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
import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.resources.ResourceTest;

public class BenchHistoryStoreTest extends ResourceTest {

	public BenchHistoryStoreTest() {
		super();
	}

	public BenchHistoryStoreTest(String name) {
		super(name);
	}

	public static Test suite() {
		//		TestSuite suite = new TestSuite();
		//		suite.addTest(new BenchHistoryStoreTest("testBug28603"));
		//		return suite;
		return new TestSuite(BenchHistoryStoreTest.class);
	}

	public void testBug28603() {
		final IProject project = getWorkspace().getRoot().getProject("myproject");
		final IFolder folder1 = project.getFolder("myfolder1");
		final IFolder folder2 = project.getFolder("myfolder2");
		final IFile file1 = folder1.getFile("myfile.txt");
		final IFile file2 = folder2.getFile(file1.getName());

		new CorePerformanceTest() {
			protected void setup() {
				ensureExistsInWorkspace(new IResource[] {project, folder1, folder2}, true);
				try {
					file1.create(getRandomContents(), IResource.FORCE, getMonitor());
					file1.setContents(getRandomContents(), IResource.FORCE | IResource.KEEP_HISTORY, getMonitor());
					file1.setContents(getRandomContents(), IResource.FORCE | IResource.KEEP_HISTORY, getMonitor());
					file1.setContents(getRandomContents(), IResource.FORCE | IResource.KEEP_HISTORY, getMonitor());
				} catch (CoreException e) {
					fail("0.0", e);
				}
			}

			protected void operation() {
				try {
					file1.move(file2.getFullPath(), true, true, getMonitor());
					file2.move(file1.getFullPath(), true, true, getMonitor());
				} catch (CoreException e) {
					fail("1.0", e);
				}
			}

			protected void teardown() {
				try {
					ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
					IHistoryStore store = ((Workspace) getWorkspace()).getFileSystemManager().getHistoryStore();
					// Remove all the entries from the history store index.  Note that
					// this does not cause the history store states to be removed.
					store.remove(Path.ROOT, getMonitor());
					// Now make sure all the states are really removed.
					if (store instanceof HistoryStore)
						TestingSupport.removeGarbage((HistoryStore) store);
				} catch (Exception e) {
					fail("2.0", e);
				}
			}
		}.run(this, 10, 5);
	}
}
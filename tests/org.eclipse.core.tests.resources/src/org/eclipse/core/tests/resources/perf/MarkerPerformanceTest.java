/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.perf;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.PerformanceTestRunner;
import org.eclipse.core.tests.resources.ResourceTest;

public class MarkerPerformanceTest extends ResourceTest {
	IProject project;
	IFile file;
	IMarker[] markers;
	final int NUM_MARKERS = 5000;
	final int REPEAT = 100;

	/**
	 * No-arg constructor to satisfy test harness.
	 */
	public MarkerPerformanceTest() {
		super();
	}

	/**
	 * Standard test case constructor
	 */
	public MarkerPerformanceTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(MarkerPerformanceTest.class);
		//		TestSuite suite = new TestSuite(MarkerPerformanceTest.class.getName());
		//		suite.addTest(new MarkerPerformanceTest("benchSetAttributes1"));
		//		suite.addTest(new MarkerPerformanceTest("benchSetAttributes2"));
		//		return suite;
	}

	public void testSetAttributes1() {
		//benchmark setting many attributes in a single operation
		final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				//set all attributes for each marker
				for (int i = 0; i < NUM_MARKERS; i++) {
					for (int j = 0; j < REPEAT; j++) {
						markers[i].setAttribute("attrib", "hello");
					}
				}
			}
		};
		PerformanceTestRunner runner = new PerformanceTestRunner() {
			protected void test() {
				try {
					getWorkspace().run(runnable, null);
				} catch (CoreException e) {
					fail("2.0", e);
				}
			}
		};
		runner.setFingerprintName("Set marker attributes");
		runner.run(this, 1, 1);
	}

	public void testSetAttributes2() {
		//benchmark setting many attributes in a single operation
		final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				//set one attribute per marker, repeat for all attributes
				for (int j = 0; j < REPEAT; j++) {
					for (int i = 0; i < NUM_MARKERS; i++) {
						markers[i].setAttribute("attrib", "hello");
					}
				}
			}
		};
		new PerformanceTestRunner() {
			protected void test() {
				try {
					getWorkspace().run(runnable, null);
				} catch (CoreException e) {
					fail("2.0", e);
				}
			}
		}.run(this, 1, 1);
	}

	/**
	 * @see ResourceTest#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		final IMarker[] createdMarkers = new IMarker[NUM_MARKERS];
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				//create resources
				project = getWorkspace().getRoot().getProject("TestProject");
				project.create(null);
				project.open(null);
				file = project.getFile(Path.ROOT.append("file.txt"));
				file.create(getRandomContents(), true, null);
				//create markers
				for (int i = 0; i < NUM_MARKERS; i++) {
					createdMarkers[i] = file.createMarker(IMarker.BOOKMARK);
				}
			}
		};

		try {
			getWorkspace().run(runnable, null);
		} catch (CoreException e) {
			fail("1.0", e);
		}
		markers = createdMarkers;
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		project.delete(true, true, null);
	}
}

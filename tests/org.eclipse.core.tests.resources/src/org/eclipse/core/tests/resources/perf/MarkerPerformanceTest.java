package org.eclipse.core.tests.resources.perf;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.CorePerformanceTest;

public class MarkerPerformanceTest extends CorePerformanceTest {
	private IProject project;
	private IFile file;
	private IMarker[] markers;
	private final int NUM_MARKERS = 5000;
	final int REPEAT = 100;
	/**
	 * No-arg constructor to satisfy test harness.
	 */
	public MarkerPerformanceTest() {
	}
	/**
	 * Standard test case constructor
	 */
	public MarkerPerformanceTest(String testName) {
		super(testName);
	}
	public static Test suite() { 
		TestSuite suite= new TestSuite();
		suite.addTest(new MarkerPerformanceTest("benchSetAttributes1"));
		suite.addTest(new MarkerPerformanceTest("benchSetAttributes2"));
	 	return suite;
	}
	public void benchSetAttributes1() {
		//benchmark setting many attributes in a single operation
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				//set all attributes for each marker
				for (int i = 0; i < NUM_MARKERS; i++) {
					for (int j = 0; j < REPEAT; j++) {
						markers[i].setAttribute("attrib", "hello");
					}
				}
			}
		};
		System.out.println("Starting setAttributes1");
		startBench();
		try {
			getWorkspace().run(runnable, null);
		} catch (CoreException e) {
			fail("2.0", e);
		}
		stopBench("benchSetAttributes1", REPEAT*NUM_MARKERS);
	}
	public void benchSetAttributes2() {
		//benchmark setting many attributes in a single operation
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				//set one attribute per marker, repeat for all attributes
				for (int j = 0; j < REPEAT; j++) {
					for (int i = 0; i < NUM_MARKERS; i++) {
						markers[i].setAttribute("attrib", "hello");
					}
				}
			}
		};
		
		System.out.println("Starting setAttributes2");
		startBench();
		try {
			getWorkspace().run(runnable, null);
		} catch (CoreException e) {
			fail("2.0", e);
		}
		stopBench("benchSetAttributes2", REPEAT*NUM_MARKERS);
	}
	/**
	 * @see EclipseWorkspaceTest#setUp()
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
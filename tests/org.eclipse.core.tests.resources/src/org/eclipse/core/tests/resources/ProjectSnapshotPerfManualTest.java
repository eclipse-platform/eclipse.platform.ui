/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Francis Lynch (Wind River) - [301563] adapted from RefreshLocalPerformanceTest
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.io.File;
import java.net.URI;
import java.util.Date;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Measure speed of a project "Import with Snapshot" operation compared
 * to a normal "Project Import" operation, as per bug 301563.
 * 
 * This test is meant to be run manually, and not integrated into the 
 * automated test suite. Requires large project data (thousands of folders)
 * to be available on a slow file system (bigSiteLocation). Modify 
 * bigSiteLocation to suit your needs, then run-as > JUnit Plug-in Test.
 */
public class ProjectSnapshotPerfManualTest extends ResourceTest {
	/** big site default volume (windows) */
	public static final String bigSiteDevice = "d:";

	/** big site initial location. Modify to suit your needs */
	public static final IPath bigSiteLocation = new Path(bigSiteDevice, "/bigsite");

	/** settings directory name */
	private static final String DIR_NAME = ".settings";

	/** location of refresh snapshot file */
	private static final String REFRESH_SNAPSHOT_FILE_LOCATION = ".settings/resource-index.zip";

	/** benchmark */
	public Date startDate;

	public ProjectSnapshotPerfManualTest() {
		super();
	}

	public ProjectSnapshotPerfManualTest(String name) {
		super(name);
	}

	protected int countChildren(File root) {
		String[] children = root.list();
		if (children == null)
			return 0;
		int result = 0;
		for (int i = 0; i < children.length; i++) {
			File child = new File(root, children[i]);
			if (child.isDirectory())
				result += countChildren(child);
			result++;
		}
		return result;
	}

	public String dispTime(long diff) {
		return String.valueOf(diff);
	}

	public void startClock() {
		startDate = new Date();
	}

	public long stopClock() {
		Date stopDate = new Date();
		return stopDate.getTime() - startDate.getTime();
	}

	// this test should not be in AllTests because it is only a performance test
	public static Test suite() {
		TestSuite suite = new TestSuite(ProjectSnapshotPerfManualTest.class.getName());
		suite.addTest(new ProjectSnapshotPerfManualTest("testSnapshotImportPerformance"));
		return suite;
	}

	/**
	 * Open a project and export a refresh snapshot. Re-open the project using
	 * the snapshot and compare the times for opening with and without the
	 * snapshot.
	 */
	public void testSnapshotImportPerformance() throws Exception {
		// test if the test can be done in this machine
		if (!bigSiteLocation.toFile().isDirectory())
			return;

		// create common objects
		IProject project = getWorkspace().getRoot().getProject("MyTestProject");
		IProjectDescription description = getWorkspace().newProjectDescription(project.getName());
		description.setLocation(bigSiteLocation);

		// performance data
		long originalOpen = 0;
		long snapshotOpen = 0;
		long refreshOnly = 0;
		long refresh2Open = 0;

		// report the number of files to be refreshed
		int numberOfFiles = countChildren(bigSiteLocation.toFile());
		System.out.println("Number of local resources: " + numberOfFiles);

		// create the project from the location
		project.create(description, null);

		// open the project, timing the initial refresh
		startClock();
		project.open(null);
		originalOpen = stopClock();

		// dump the snapshot refresh info
		ensureExistsInWorkspace(project.getFolder(DIR_NAME), true);
		IPath projPath = project.getLocation();
		projPath = projPath.append(REFRESH_SNAPSHOT_FILE_LOCATION);
		URI snapshotLocation = org.eclipse.core.filesystem.URIUtil.toURI(projPath);
		project.saveSnapshot(IProject.SNAPSHOT_TREE, snapshotLocation, null);

		// close and delete project but leave contents
		project.close(null);
		project.delete(false, false, null);

		// open the project and import refresh snapshot
		project.create(description, null);
		startClock();
		project.loadSnapshot(IProject.SNAPSHOT_TREE, snapshotLocation, null);
		project.open(IResource.NONE, null);
		snapshotOpen = stopClock();

		// now refresh the project, verifying zero resource delta
		// (except for the creation of .settings/resource-index.zip)
		startClock();
		ResourceDeltaVerifier verifier = new ResourceDeltaVerifier();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(verifier);
		verifier.reset();
		IFolder settings = project.getFolder(DIR_NAME);
		IFile snapshot = project.getFile(REFRESH_SNAPSHOT_FILE_LOCATION);
		verifier.addExpectedChange(settings, IResourceDelta.CHANGED, 0);
		verifier.addExpectedChange(snapshot, IResourceDelta.ADDED, 0);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		refreshOnly = stopClock();
		verifier.verifyDelta(null);
		assertTrue("2.0 " + verifier.getMessage(), verifier.isDeltaValid());

		// close and delete project but leave contents
		project.close(null);
		project.delete(false, false, null);
		IPath snapshotFile = bigSiteLocation.append(REFRESH_SNAPSHOT_FILE_LOCATION);
		snapshotFile.toFile().delete();

		// open the project again with standard refresh
		project.create(description, null);
		startClock();
		project.open(null);
		refresh2Open = stopClock();

		// delete project to avoid getting content deleted in tearDown()
		project.close(null);
		project.delete(false, false, null);

		System.out.println("Original open: " + originalOpen);
		System.out.println("Snapshot open: " + snapshotOpen);
		System.out.println("Forced refresh only: " + refreshOnly);
		System.out.println("Second refresh open: " + refresh2Open);
	}
}

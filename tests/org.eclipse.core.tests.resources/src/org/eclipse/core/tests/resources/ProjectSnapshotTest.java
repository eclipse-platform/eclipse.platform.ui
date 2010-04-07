/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Francis Lynch (Wind River) - adapted from FileSystemResourceManagerTest
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.net.URI;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;

/**
 * Tests API for save/load refresh snapshots introduced in 3.6M6 (bug 301563):
 * <ul>
 * <li>{@link IProject#loadSnapshot(int, URI, org.eclipse.core.runtime.IProgressMonitor)}
 * <li>{@link IProject#saveSnapshot(int, URI, org.eclipse.core.runtime.IProgressMonitor)}
 * <li>{@link IProject#SNAPSHOT_TREE}
 * </ul>
 */
public class ProjectSnapshotTest extends ResourceTest {

	/** location of refresh snapshot file */
	private static final String REFRESH_SNAPSHOT_FILE_LOCATION = "resource-index.zip";
	/** test projects that we operate on */
	protected IProject[] projects = new IProject[2];

	public ProjectSnapshotTest() {
		super();
	}

	public ProjectSnapshotTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(ProjectSnapshotTest.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		projects[0] = getWorkspace().getRoot().getProject("p1");
		projects[1] = getWorkspace().getRoot().getProject("p2");
		ensureExistsInWorkspace(projects, true);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private void populateProject(IProject project) {
		// add files and folders to project
		IFile file = project.getFile("file");
		ensureExistsInFileSystem(file);
		IFolder folder = projects[0].getFolder("folder");
		IFolder subfolder = folder.getFolder("subfolder");
		IFile subfile = folder.getFile("subfile");
		ensureExistsInFileSystem(folder);
		ensureExistsInFileSystem(subfolder);
		ensureExistsInFileSystem(subfile);
	}

	private URI getSnapshotLocation(IProject project) {
		IPath projPath = project.getLocation();
		projPath = projPath.append(REFRESH_SNAPSHOT_FILE_LOCATION);
		return org.eclipse.core.filesystem.URIUtil.toURI(projPath);
	}

	/*
	 * Create project and populate with resources. Save snapshot.
	 * Delete project (also delete resources on disk). Import project
	 * with snapshot. All resources must be marked as "exists" in the
	 * resource tree, although there is no resource actually on disk.
	 */
	public void testLoadNoRefresh() throws Throwable {
		final IProject project = projects[0];
		// add files and folders to project
		populateProject(project);
		// perform refresh to ensure new resources in tree
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		// save project refresh snapshot outside the project
		URI snapshotLocation = getSnapshotLocation(projects[1]);
		project.saveSnapshot(IProject.SNAPSHOT_TREE, snapshotLocation, null);
		// close and delete project contents
		project.close(null);
		// delete the project and import refresh snapshot
		project.delete(true, false, null);
		project.create(null);
		project.loadSnapshot(IProject.SNAPSHOT_TREE, snapshotLocation, null);
		project.open(IResource.NONE, null);
		// verify that the resources are thought to exist
		IFile file = project.getFile("file");
		IFolder folder = project.getFolder("folder");
		IFolder subfolder = folder.getFolder("subfolder");
		IFile subfile = folder.getFile("subfile");
		assertTrue("1.1", file.exists());
		assertTrue("1.2", folder.exists());
		assertTrue("1.3", subfolder.exists());
		assertTrue("1.4", subfile.exists());
	}

	/*
	 * Create project and populate with resources. Save snapshot.
	 * Delete project (also delete resources on disk). Import project
	 * with snapshot and perform a refresh. Resource delta must be created,
	 * and none of the snapshot resources is found any more.
	 */
	public void testLoadWithRefresh() throws Throwable {
		final IProject project = projects[0];
		// add files and folders to project
		populateProject(project);
		// perform refresh to ensure new resources in tree
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		// save project refresh snapshot outside the project
		URI snapshotLocation = getSnapshotLocation(projects[1]);
		project.saveSnapshot(IProject.SNAPSHOT_TREE, snapshotLocation, null);
		// close and delete project contents
		project.close(null);
		// delete the project and import refresh snapshot
		project.delete(true, false, null);
		// wait before recreating the .project file on disk, to ensure it will have
		// a different time stamp and be reported as a modification. This is
		// because some file systems only have a 1 second timestamp granularity.
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			fail("0.0");
		}
		project.create(null);
		project.loadSnapshot(IProject.SNAPSHOT_TREE, snapshotLocation, null);
		project.open(IResource.NONE, null);
		// set up resource delta verifier
		ResourceDeltaVerifier verifier = new ResourceDeltaVerifier();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(verifier);
		verifier.reset();
		IFile file = project.getFile("file");
		IFolder folder = project.getFolder("folder");
		IFolder subfolder = folder.getFolder("subfolder");
		IFile subfile = folder.getFile("subfile");
		verifier.addExpectedChange(file, IResourceDelta.REMOVED, 0);
		verifier.addExpectedChange(folder, IResourceDelta.REMOVED, 0);
		verifier.addExpectedChange(subfolder, IResourceDelta.REMOVED, 0);
		verifier.addExpectedChange(subfile, IResourceDelta.REMOVED, 0);
		verifier.addExpectedChange(project, IResourceDelta.CHANGED, IResourceDelta.DESCRIPTION);
		IFile dotProject = project.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
		verifier.addExpectedChange(dotProject, IResourceDelta.CHANGED, IResourceDelta.CONTENT);
		// perform refresh to create resource delta against snapshot
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		verifier.verifyDelta(null);
		assertTrue("1.0 " + verifier.getMessage(), verifier.isDeltaValid());
		// verify that the resources are no longer thought to exist
		assertTrue("1.1", !file.exists());
		assertTrue("1.2", !folder.exists());
		assertTrue("1.3", !subfolder.exists());
		assertTrue("1.4", !subfile.exists());
	}

}

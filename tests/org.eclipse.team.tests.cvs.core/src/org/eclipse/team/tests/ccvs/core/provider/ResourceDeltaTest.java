/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.tests.ccvs.core.provider;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.AddDeleteMoveListener;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

public class ResourceDeltaTest extends EclipseTest {

	/**
	 * Constructor for ResourceDeltaTest.
	 */
	public ResourceDeltaTest() {
		super();
	}

	/**
	 * Constructor for ResourceDeltaTest.
	 * @param name
	 */
	public ResourceDeltaTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ResourceDeltaTest.class);
		return new CVSTestSetup(suite);
		//return new CVSTestSetup(new ResourceDeltaTest("testOrphanedSubtree"));
	}
	
	public void assertNotManaged(ICVSFile cvsFile) throws CVSException {
		assertTrue("File " + cvsFile.getName() + " should not be managed", ! cvsFile.isManaged());
	}
	
	public void assertNotManaged(ICVSFolder cvsFolder) throws CVSException {
		assertTrue("Folder " + cvsFolder.getName() + " should not be managed", ! cvsFolder.isManaged());
		assertTrue("Folder " + cvsFolder.getName() + " should not be a cvs folder", ! cvsFolder.isCVSFolder());
		cvsFolder.acceptChildren(new ICVSResourceVisitor() {
			public void visitFile(ICVSFile file) throws CVSException {
				assertNotManaged(file);
			}
			public void visitFolder(ICVSFolder folder) throws CVSException {
				assertNotManaged(folder);
			}
		});
	}
	
	public void assertAdditionMarkerFor(IResource resource, boolean exists) throws CoreException {
		if ( ! CVSProviderPlugin.getPlugin().getShowTasksOnAddAndDelete()) return;
		IMarker[] markers = resource.findMarkers(AddDeleteMoveListener.ADDITION_MARKER, false, IResource.DEPTH_ZERO);
		if (exists) {
   			assertTrue("Addition marker doesn't exist for " + resource.getName(), markers.length == 1);
		} else {
   			assertTrue("Addition marker exists for " + resource.getName(), markers.length == 0);
   		}
	}
	
	public void assertDeletionMarkerFor(IResource resource, boolean exists) throws CoreException {
		if ( ! CVSProviderPlugin.getPlugin().getShowTasksOnAddAndDelete()) return;
		IMarker marker = null;
		if (resource.getParent().exists()) {
			String name = resource.getName();
	   		IMarker[] markers = resource.getParent().findMarkers(AddDeleteMoveListener.DELETION_MARKER, false, IResource.DEPTH_ZERO);
	   		for (int i = 0; i < markers.length; i++) {
				IMarker iMarker = markers[i];
				String markerName = (String)iMarker.getAttribute(AddDeleteMoveListener.NAME_ATTRIBUTE);
				if (markerName.equals(name)) {
					marker = iMarker;
					break;
				}
			}
		}
		if (exists) {
   			assertTrue("Deletion marker doesn't exist for " + resource.getName(), marker != null);
		} else {
   			assertTrue("Deletion marker exists for " + resource.getName(), marker == null);
   		}
	}
	
	public void testOrphanedSubtree() throws TeamException, CoreException {
		IProject project = createProject("testOrphanedSubtree", new String[] { "changed.txt", "deleted.txt", "folder1/", "folder1/a.txt", "folder1/folder2/b.txt"});
		IFolder folder = project.getFolder(new Path("folder1"));
		folder.move(new Path("moved"), false, false, null);
		folder = project.getFolder(new Path("moved"));
		ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(folder);
		assertNotManaged(cvsFolder);
		assertAdditionMarkerFor(folder, true);
	}
	
	public void testDeletionHandling() throws TeamException, CoreException {
		IProject project = createProject("testDeletionHandling", new String[] { "changed.txt", "deleted.txt", "folder1/", "folder1/a.txt", "folder1/folder2/b.txt"});
		addResources(project, new String[] {"added.txt"}, false);
		assertAdditionMarkerFor(project.getFile("added.txt"), false);
		deleteResources(project, new String[] {"added.txt", "deleted.txt"}, false);
		ICVSFile file = CVSWorkspaceRoot.getCVSFileFor(project.getFile("added.txt"));
		assertNotManaged(file);
		assertDeletionMarkerFor(project.getFile("added.txt"), false);
		file = CVSWorkspaceRoot.getCVSFileFor(project.getFile("deleted.txt"));
		assertTrue("File " + file.getName() + " should be managed", file.isManaged());
		ResourceSyncInfo info = file.getSyncInfo();
		assertTrue("File " + file.getName() + " should be marked as deleted", info.isDeleted());
		assertDeletionMarkerFor(project.getFile("deleted.txt"), true);
	}
	
	public void testFileAdditionHandling() throws TeamException, CoreException {
		IProject project = createProject("testFileAdditionHandling", new String[] { "changed.txt", "deleted.txt", "folder1/", "folder1/a.txt", "folder1/folder2/b.txt"});
		deleteResources(project, new String[] {"deleted.txt"}, false);
		assertDeletionMarkerFor(project.getFile("deleted.txt"), true);
		addResources(project, new String[] {"deleted.txt"}, false);
		ICVSFile file = CVSWorkspaceRoot.getCVSFileFor(project.getFile("deleted.txt"));
		assertTrue("File " + file.getName() + " should be managed", file.isManaged());
		ResourceSyncInfo info = file.getSyncInfo();
		assertTrue("File " + file.getName() + " should not be marked as deleted", ! info.isDeleted());
		assertTrue("File " + file.getName() + " should not be marked as addition", ! info.isAdded());
		assertDeletionMarkerFor(project.getFile("deleted.txt"), false);
		assertAdditionMarkerFor(project.getFile("added.txt"), false);
	}
	
	public void testFolderAdditionHandling() throws TeamException, CoreException {
		IProject project = createProject("testFolderAdditionHandling", new String[] { "changed.txt", "deleted.txt", "folder1/", "folder1/a.txt", "folder1/folder2/b.txt"});
		IFolder folder = project.getFolder("newfolder");
		folder.create(false, true, null);
		assertAdditionMarkerFor(folder, true);
		getProvider(project).add(new IResource[] {folder}, IResource.DEPTH_ZERO, DEFAULT_MONITOR);
		assertAdditionMarkerFor(folder, false);
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ui.synchronize;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.MutableSyncInfoSet;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.tests.core.TeamTest;
import org.eclipse.team.tests.ui.views.*;

/**
 * Tests for the SyncInfoSet content providers.
 */
public class SyncInfoSetContentProviderTest extends TeamTest {
	
	public static final TestSubscriber subscriber = new TestSubscriber();
	/*
	 * This method creates a project with the given resources, imports
	 * it to CVS and checks it out
	 */
	protected IProject createProject(String prefix, String[] resources) throws CoreException {
		IProject project = getUniqueTestProject(prefix);
		buildResources(project, resources, true);
		return project;
	}
	
	/*
	 * Create a test project using the currently running test case as the project name prefix
	 */
	protected IProject createProject(String[] resources) throws CoreException {
		return createProject(getName(), resources);
	}
	
	private MutableSyncInfoSet createSet(IProject project, String[] resources, int[] syncKind) throws TeamException {
		MutableSyncInfoSet set = new MutableSyncInfoSet();
		adjustSet(
			set,
			project,
			resources,
			syncKind);
		return set;
	}
		
	private void adjustSet(MutableSyncInfoSet set, IProject project, String[] resourceStrings, int[] syncKind) throws TeamException {
		IResource[] resources = buildResources(project, resourceStrings);
		set.beginInput();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			int kind = syncKind[i];
			if (kind == SyncInfo.IN_SYNC) {
				set.remove(resource);
			} else {
				SyncInfo newInfo = subscriber.getSyncInfo(resource, kind);
				if (set.getSyncInfo(resource) != null) {
					set.changed(newInfo);
				} else {
					set.add(newInfo);
				}
			}
		}
		set.endInput(new NullProgressMonitor());
	}

	/**
	 * Ensure that the resource
	 * @param resources
	 */
	private void assertVisible(IProject project, String[] resources) {
		// TODO Auto-generated method stub
		
	}
	
	public void simpleTest() throws CoreException {
		IProject project = createProject(new String[] { "file.txt", "folder1/file2.txt", "folder1/folder2/file3.txt"});
		MutableSyncInfoSet set = createSet(
			project, 
			new String[] { "file.txt" },
			new int[] {SyncInfo.OUTGOING | SyncInfo.CHANGE});
		assertVisible(project, new String[] { "file.txt" });
		
		adjustSet(
			set,
			project,
			new String[] { "folder1/file2.txt", "folder1/folder2/file3.txt" },
			new int[] {
				SyncInfo.OUTGOING | SyncInfo.CHANGE,
				SyncInfo.OUTGOING | SyncInfo.CHANGE});
		assertVisible(project, new String[] { "file.txt", "folder1/file2.txt", "folder1/folder2/file3.txt" });
		
		adjustSet(
			set,
			project,
			new String[] { "folder1/file2.txt"},
			new int[] {
				SyncInfo.IN_SYNC,
			});
		assertVisible(project, new String[] { "file.txt", "folder1/folder2/file3.txt" });
			
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		TestTreeViewer viewer = new TestTreeViewer(null);
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		// TODO Dispose of whatever was created
		super.tearDown();
	}
}

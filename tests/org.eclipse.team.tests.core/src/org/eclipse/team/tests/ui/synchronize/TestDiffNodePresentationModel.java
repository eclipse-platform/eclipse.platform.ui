/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ui.synchronize;

import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Item;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.tests.core.TeamTest;
import org.eclipse.team.tests.ui.views.ContentProviderTestView;
import org.eclipse.team.tests.ui.views.TestTreeViewer;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;


public class TestDiffNodePresentationModel extends TeamTest {
	
	private ContentProviderTestView view;
	private SyncInfoTree set;
	private TreeViewerAdvisor configuration;
	
	public TestDiffNodePresentationModel() {
		super();
	}

	public TestDiffNodePresentationModel(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(TestDiffNodePresentationModel.class);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		TestParticipant p = (TestParticipant)getParticipant(TestParticipant.ID);
		p.reset();
		this.set = p.getSyncInfoSet();
		view = ContentProviderTestView.findViewInActivePage(null);
		configuration.initializeViewer(view.getViewer());
	}
	
	/**
	 * 
	 */
	private ISynchronizeParticipant getParticipant(String id) throws TeamException {
		ISynchronizeParticipantReference reference = TeamUI.getSynchronizeManager().get(id, null);
		if (reference != null) {
			return reference.getParticipant();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		set = null;
		configuration.dispose();
		super.tearDown();
	}
	
	protected SynchronizeModelProvider getDiffNodeController(SyncInfoTree set) {
		//return new HierarchicalModelProvider(set);
		return null;
	}
		
	private void adjustSet(SyncInfoSet set, IProject project, String[] resourceStrings, int[] syncKind) throws TeamException {
		IResource[] resources = buildResources(project, resourceStrings);
		try {
			set.beginInput();
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				int kind = syncKind[i];
				if (kind == SyncInfo.IN_SYNC) {
					set.remove(resource);
				} else {
					SyncInfo newInfo = new TestSyncInfo(resource, kind);
					set.add(newInfo);
				}
			}
		} finally {
			set.endInput(null);
		}
	}

	/**
	 * Ensure that the resource
	 * @param resources
	 */
	protected void assertProperVisibleItems() {
		IResource[] resources = set.getResources();
		List resourceList = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			resourceList.add(resource);
		}
		TestTreeViewer viewer = view.getViewer();
		Item[] items = viewer.getRootItems();
		if (resources.length ==  0) {
			assertTrue("There are items visible when there should not be.", items.length == 0);
			return;
		}
		// Test that all items in the tree are expected
		for (int i = 0; i < items.length; i++) {
			Item item = items[i];
			assertThatAllOutOfSyncResourcesAreShown(item, resourceList);
		}
		// Test that all expected resources and their parents are present
		assertTrue("The tree did not contain all expected resources: " + resourceList.toString(), resourceList.isEmpty());
	}
	
	/**
	 * Traverse every element shown in the view and ensure that every out-of-sync 
	 * resource in the set is at least shown. This doesn't test the actual logical
	 * organization, but does ensure that all out-of-sync resources are shown only
	 * once.
	 */
	protected void assertThatAllOutOfSyncResourcesAreShown(Item item, List outOfSyncResources) {
		Object node = item.getData();
		SyncInfo info = (SyncInfo)Utils.getAdapter(node, SyncInfo.class);
		if(info != null) {
			assertTrue("The tree contained an out-of-sync resource that wasn't in the set", outOfSyncResources.remove(info.getLocal()));
		}
		Item[] children = view.getViewer().getChildren(item);
		for (int i = 0; i < children.length; i++) {
			Item child = children[i];
			assertThatAllOutOfSyncResourcesAreShown(child, outOfSyncResources);
		}
	}
	
	public void testNestedFolder() throws CoreException {
		IProject project = createProject(new String[]{"file.txt", "folder1/file2.txt", "folder1/folder2/file3.txt"});
		adjustSet(set, project, 
				new String[]{"file.txt"}, 
				new int[]{SyncInfo.OUTGOING | SyncInfo.CHANGE});
		assertProperVisibleItems();
		adjustSet(set, project, 
				new String[]{"folder1/file2.txt", "folder1/folder2/file3.txt"}, 
				new int[]{SyncInfo.OUTGOING | SyncInfo.CHANGE, SyncInfo.OUTGOING | SyncInfo.CHANGE});
		assertProperVisibleItems();
		adjustSet(set, project, 
				new String[]{"folder1/file2.txt"}, 
				new int[]{SyncInfo.IN_SYNC,});
		assertProperVisibleItems();
	}

	public void testParentRemovalWithChildRemaining() throws CoreException {
		IProject project = createProject(new String[]{"file.txt", "folder1/file2.txt", "folder1/folder2/file3.txt"});
		adjustSet(set, project, 
				new String[]{"folder1/folder2/", "folder1/folder2/file3.txt"}, 
				new int[]{SyncInfo.CONFLICTING | SyncInfo.CHANGE, SyncInfo.CONFLICTING | SyncInfo.CHANGE});
		assertProperVisibleItems();
		
		adjustSet(set, project, 
				new String[]{"folder1/folder2/", "folder1/folder2/file3.txt"}, 
				new int[]{SyncInfo.IN_SYNC, SyncInfo.OUTGOING | SyncInfo.CHANGE});
		assertProperVisibleItems();
	}
	
	public void testEmptyFolderChange() throws CoreException {
		IProject project = createProject(new String[]{"file.txt", "folder1/file2.txt", "folder1/folder2/file3.txt", "folder3/"});
		adjustSet(set, project, 
				new String[]{"folder1/folder2/", "folder1/folder2/file3.txt"}, 
				new int[]{SyncInfo.CONFLICTING | SyncInfo.CHANGE, SyncInfo.CONFLICTING | SyncInfo.CHANGE});
		assertProperVisibleItems();
		
		adjustSet(set, project, 
				new String[]{"folder1/folder2/", "folder1/folder2/file3.txt"}, 
				new int[]{SyncInfo.IN_SYNC, SyncInfo.OUTGOING | SyncInfo.CHANGE});
		assertProperVisibleItems();
		
		adjustSet(set, project, 
				new String[]{"folder1/folder2/file3.txt"}, 
				new int[]{SyncInfo.IN_SYNC});
		assertProperVisibleItems();
		
		adjustSet(set, project, 
				new String[]{"folder3/"}, 
				new int[]{SyncInfo.INCOMING | SyncInfo.ADDITION});
		assertProperVisibleItems();
	}
}

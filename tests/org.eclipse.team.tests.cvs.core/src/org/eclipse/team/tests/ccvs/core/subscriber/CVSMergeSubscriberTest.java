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
package org.eclipse.team.tests.ccvs.core.subscriber;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSMergeSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.ui.sync.SyncInfoSet;


/**
 * Tests the CVSMergeSubscriber
 */
public class CVSMergeSubscriberTest extends CVSSyncSubscriberTest {

	public static Test suite() {
		String testName = System.getProperty("eclipse.cvs.testName");
		if (testName == null) {
			TestSuite suite = new TestSuite(CVSMergeSubscriberTest.class);
			return new CVSTestSetup(suite);
		} else {
			return new CVSTestSetup(new CVSMergeSubscriberTest(testName));
		}
	}
	
	public CVSMergeSubscriberTest() {
		super();
	}

	public CVSMergeSubscriberTest(String name) {
		super(name);
	}

	private IProject branchProject(IProject project, CVSTag root, CVSTag branch) throws TeamException {
		IProject copy = checkoutCopy(project, "-copy");
		tagProject(project, root, false);
		tagProject(project, branch, false);
		getProvider(copy).update(new IResource[] {copy}, Command.NO_LOCAL_OPTIONS,
			branch, false /*createBackups*/, DEFAULT_MONITOR);
		return copy;
	}
		
	private void mergeResources(CVSMergeSubscriber subscriber, IProject project, String[] resourcePaths, boolean allowOverwrite) throws CoreException, TeamException, InvocationTargetException, InterruptedException {
		IResource[] resources = getResources(project, resourcePaths);
		SyncInfo[] infos = createSyncInfos(subscriber, resources);
		mergeResources(subscriber, infos, allowOverwrite);
	}
	
	private void mergeResources(TeamSubscriber subscriber, SyncInfo[] infos, boolean allowOverwrite) throws TeamException, InvocationTargetException, InterruptedException {
		TestMergeUpdateAction action = new TestMergeUpdateAction(allowOverwrite);
		action.getRunnable(new SyncInfoSet(infos)).run(DEFAULT_MONITOR);
	}
	
	
	private void markAsMerged(CVSMergeSubscriber subscriber, IProject project, String[] resourcePaths) throws CoreException, TeamException, InvocationTargetException, InterruptedException {
		IResource[] resources = getResources(project, resourcePaths);
		SyncInfo[] infos = createSyncInfos(subscriber, resources);
		TestMarkAsMergedAction action = new TestMarkAsMergedAction();
		action.getRunnable(new SyncInfoSet(infos)).run(DEFAULT_MONITOR);
	}

	private CVSMergeSubscriber createMergeSubscriber(IProject project, CVSTag root, CVSTag branch) {
		CVSMergeSubscriber subscriber = new CVSMergeSubscriber(new IResource[] { project }, root, branch);
		TeamSubscriber.getSubscriberManager().registerSubscriber(subscriber);
		return subscriber;
	}
	
	/**
	 * Test the basic incoming changes cases
	 * - incoming addition
	 * - incoming deletion
	 * - incoming change
	 * - incoming addition of a folder containing files
	 */
	public void testIncomingChanges() throws TeamException, CoreException, InvocationTargetException, InterruptedException {
		// Create a test project
		IProject project = createProject("testIncomingChanges", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});

		// Checkout and branch a copy
		CVSTag root = new CVSTag("root_branch1", CVSTag.VERSION);
		CVSTag branch = new CVSTag("branch1", CVSTag.BRANCH);
		IProject copy = branchProject(project, root, branch);
		
		// Modify the branch
		addResources(copy, new String[] {"addition.txt", "folderAddition/", "folderAddition/new.txt"}, true);
		deleteResources(copy, new String[] {"folder1/a.txt"}, true);
		changeResources(copy, new String[] {"file1.txt"}, true);
		
		// create a merge subscriber
		CVSMergeSubscriber subscriber = createMergeSubscriber(project, root, branch);
		
		// check the sync states
		assertSyncEquals("testIncomingChanges", subscriber, project, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "addition.txt", "folderAddition/", "folderAddition/new.txt"}, 
			true, new int[] {
				SyncInfo.INCOMING | SyncInfo.CHANGE,
				SyncInfo.IN_SYNC,
				SyncInfo.INCOMING | SyncInfo.DELETION,
				SyncInfo.INCOMING | SyncInfo.ADDITION,
				SyncInfo.INCOMING | SyncInfo.ADDITION,
				SyncInfo.INCOMING | SyncInfo.ADDITION});
				
		// Perform a merge
		mergeResources(subscriber, project, new String[] { 
			"file1.txt",
			"folder1/a.txt", 
			"addition.txt", 
			"folderAddition/", 
			"folderAddition/new.txt"}, 
			false /* allow overwrite */);
			
		// check the sync states for the workspace subscriber
		assertSyncEquals("testIncomingChanges", getWorkspaceSubscriber(), project, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "addition.txt", "folderAddition/", "folderAddition/new.txt"}, 
			true, new int[] {
				SyncInfo.OUTGOING | SyncInfo.CHANGE,
				SyncInfo.IN_SYNC,
				SyncInfo.OUTGOING  | SyncInfo.DELETION,
				SyncInfo.OUTGOING | SyncInfo.ADDITION,
				SyncInfo.IN_SYNC,
				SyncInfo.OUTGOING | SyncInfo.ADDITION});
	}

	public void testMergableConflicts() throws IOException, TeamException, CoreException, InvocationTargetException, InterruptedException {
		// Create a test project
		IProject project = createProject("testMergableConflicts", new String[] { "file1.txt", "file2.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		setContentsAndEnsureModified(project.getFile("file1.txt"), "some text\nwith several lines\n");
		setContentsAndEnsureModified(project.getFile("file2.txt"), "some text\nwith several lines\n");
		commitProject(project);

		// Checkout and branch a copy
		CVSTag root = new CVSTag("root_branch1", CVSTag.VERSION);
		CVSTag branch = new CVSTag("branch1", CVSTag.BRANCH);
		IProject branchedProject = branchProject(project, root, branch);
		
		// modify the branch
		appendText(branchedProject.getFile("file1.txt"), "first line\n", true);
		appendText(branchedProject.getFile("file2.txt"), "last line\n", false);
		commitProject(branchedProject);
		
		// modify HEAD
		appendText(project.getFile("file1.txt"), "last line\n", false);
		commitProject(project);
		// have one local change
		appendText(project.getFile("file2.txt"), "first line\n", true);
		
		// create a merge subscriber
		CVSMergeSubscriber subscriber = createMergeSubscriber(project, root, branch);
		
		// check the sync states
		assertSyncEquals("testMergableConflicts", subscriber, project, 
			new String[] { "file1.txt", "file2.txt"}, 
			true, new int[] {
				SyncInfo.CONFLICTING | SyncInfo.CHANGE, 
				SyncInfo.CONFLICTING | SyncInfo.CHANGE});
				
		// Perform a merge
		mergeResources(subscriber, project, new String[] { 
			"file1.txt",
			"file2.txt"}, 
			false /* allow overwrite */);

		// check the sync states for the workspace subscriber
		assertSyncEquals("testMergableConflicts", getWorkspaceSubscriber(), project, 
			new String[] { "file1.txt", "file2.txt"}, 
			true, new int[] {
				SyncInfo.OUTGOING | SyncInfo.CHANGE,
				SyncInfo.OUTGOING  | SyncInfo.CHANGE});
				
		//TODO: How do we know if the right thing happened to the file contents?	
	}
	
	public void testUnmergableConflicts() throws IOException, TeamException, CoreException, InvocationTargetException, InterruptedException {
		// Create a test project
		IProject project = createProject("testUnmergableConflicts", new String[] { "delete.txt", "file1.txt", "file2.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		setContentsAndEnsureModified(project.getFile("file1.txt"), "some text\nwith several lines\n");
		setContentsAndEnsureModified(project.getFile("file2.txt"), "some text\nwith several lines\n");
		commitProject(project);

		// Checkout and branch a copy
		CVSTag root = new CVSTag("root_branch1", CVSTag.VERSION);
		CVSTag branch = new CVSTag("branch1", CVSTag.BRANCH);
		IProject branchedProject = branchProject(project, root, branch);
		
		// modify the branch
		appendText(branchedProject.getFile("file1.txt"), "first line\n", true);
		appendText(branchedProject.getFile("file2.txt"), "last line\n", false);
		addResources(branchedProject, new String[] {"addition.txt"}, false);
		deleteResources(branchedProject, new String[] {"delete.txt", "folder1/a.txt"}, false);
		setContentsAndEnsureModified(branchedProject.getFile("folder1/b.txt"));
		commitProject(branchedProject);
		
		// modify local workspace
		appendText(project.getFile("file1.txt"), "conflict line\n", true);
		setContentsAndEnsureModified(project.getFile("folder1/a.txt"));
		deleteResources(project, new String[] {"delete.txt", "folder1/b.txt"}, false);
		addResources(project, new String[] {"addition.txt"}, false);
		appendText(project.getFile("file2.txt"), "conflict line\n", false);
		
		// create a merge subscriber
		CVSMergeSubscriber subscriber = createMergeSubscriber(project, root, branch);
		
		// check the sync states
		assertSyncEquals("testUnmergableConflicts", subscriber, project, 
			new String[] { "delete.txt", "file1.txt", "file2.txt", "addition.txt", "folder1/a.txt", "folder1/b.txt"}, 
			true, new int[] {
				SyncInfo.IN_SYNC, /* TODO: is this OK */
				SyncInfo.CONFLICTING | SyncInfo.CHANGE, 
				SyncInfo.CONFLICTING | SyncInfo.CHANGE,
				SyncInfo.CONFLICTING | SyncInfo.ADDITION,
				SyncInfo.CONFLICTING | SyncInfo.CHANGE,
				SyncInfo.CONFLICTING | SyncInfo.CHANGE});
		
		// TODO: Should actually perform the merge and check the results
		// However, this would require the changes to be redone
		
		// commit to modify HEAD
		commitProject(project);
				
		// check the sync states
		assertSyncEquals("testUnmergableConflicts", subscriber, project, 
			new String[] { "delete.txt", "file1.txt", "file2.txt", "addition.txt", "folder1/a.txt", "folder1/b.txt"}, 
			true, new int[] {
				SyncInfo.IN_SYNC, /* TODO: is this OK */
				SyncInfo.CONFLICTING | SyncInfo.CHANGE, 
				SyncInfo.CONFLICTING | SyncInfo.CHANGE,
				SyncInfo.CONFLICTING | SyncInfo.ADDITION,
				SyncInfo.CONFLICTING | SyncInfo.CHANGE,
				SyncInfo.CONFLICTING | SyncInfo.CHANGE});
				
		// Perform a merge
		mergeResources(subscriber, project, new String[] { "delete.txt", "file1.txt", "file2.txt", "addition.txt", "folder1/a.txt", "folder1/b.txt"}, true /* allow overwrite */);
			
		// check the sync states for the workspace subscriber
		assertSyncEquals("testUnmergableConflicts", getWorkspaceSubscriber(), project, 
		new String[] { "file1.txt", "file2.txt", "addition.txt", "folder1/a.txt", "folder1/b.txt"}, 
			true, new int[] {
				SyncInfo.OUTGOING | SyncInfo.CHANGE,
				SyncInfo.OUTGOING | SyncInfo.CHANGE,
				SyncInfo.OUTGOING | SyncInfo.CHANGE,
				SyncInfo.OUTGOING | SyncInfo.DELETION,
				SyncInfo.OUTGOING | SyncInfo.ADDITION});
		assertDeleted("testUnmergableConflicts", project, new String[] { "delete.txt" });
				
		//TODO: How do we know if the right thing happend to the file contents?
	}
	
	public void testLocalScrub() throws IOException, TeamException, CoreException, InvocationTargetException, InterruptedException {
		// Create a test project
		IProject project = createProject("testLocalScrub", new String[] { "delete.txt", "file1.txt", "file2.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		setContentsAndEnsureModified(project.getFile("file1.txt"), "some text\nwith several lines\n");
		setContentsAndEnsureModified(project.getFile("file2.txt"), "some text\nwith several lines\n");
		commitProject(project);
		
		// Checkout and branch a copy
		CVSTag root = new CVSTag("root_branch1", CVSTag.VERSION);
		CVSTag branch = new CVSTag("branch1", CVSTag.BRANCH);
		IProject branchedProject = branchProject(project, root, branch);
		
		// modify the branch
		appendText(branchedProject.getFile("file1.txt"), "first line\n", true);
		appendText(branchedProject.getFile("file2.txt"), "last line\n", false);
		addResources(branchedProject, new String[] {"addition.txt"}, false);
		deleteResources(branchedProject, new String[] {"delete.txt", "folder1/a.txt"}, false);
		setContentsAndEnsureModified(branchedProject.getFile("folder1/b.txt"));
		commitProject(branchedProject);
		
		// create a merge subscriber
		CVSMergeSubscriber subscriber = createMergeSubscriber(project, root, branch);

		// check the sync states
		assertSyncEquals("testLocalScrub", subscriber, project, 
			new String[] { "delete.txt", "file1.txt", "file2.txt", "addition.txt", "folder1/a.txt", "folder1/b.txt"}, 
			true, new int[] {
				SyncInfo.INCOMING | SyncInfo.DELETION,
				SyncInfo.INCOMING | SyncInfo.CHANGE, 
				SyncInfo.INCOMING | SyncInfo.CHANGE,
				SyncInfo.INCOMING | SyncInfo.ADDITION,
				SyncInfo.INCOMING | SyncInfo.DELETION,
				SyncInfo.INCOMING | SyncInfo.CHANGE});
				
		// scrub the project contents
		IResource[] members = project.members();
		for (int i = 0; i < members.length; i++) {
			IResource resource = members[i];
			if (resource.getName().equals(".project")) continue;
			resource.delete(false, DEFAULT_MONITOR);
		}
		
		// update
		mergeResources(subscriber, project, 
			new String[] { 
				"delete.txt", 
				"file1.txt", 
				"file2.txt", 
				"addition.txt", 
			    "folder1/a.txt",
				"folder1/b.txt"}, 
			true /* allow overwrite */);
		
		// commit
		commitProject(project);
	}
	
	public void testCancel() throws CVSException, CoreException, IOException {
		
		// Create a test project
		IProject project = createProject("testMergableConflicts", new String[] { "file1.txt", "file2.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		setContentsAndEnsureModified(project.getFile("file1.txt"), "some text\nwith several lines\n");
		setContentsAndEnsureModified(project.getFile("file2.txt"), "some text\nwith several lines\n");
		commitProject(project);

		// Checkout and branch a copy
		CVSTag root = new CVSTag("root_branch1", CVSTag.VERSION);
		CVSTag branch = new CVSTag("branch1", CVSTag.BRANCH);
		IProject branchedProject = branchProject(project, root, branch);
		
		// modify the branch
		appendText(branchedProject.getFile("file1.txt"), "first line\n", true);
		appendText(branchedProject.getFile("file2.txt"), "last line\n", false);
		commitProject(branchedProject);
		
		// modify HEAD
		appendText(project.getFile("file1.txt"), "last line\n", false);
		commitProject(project);
		// have one local change
		appendText(project.getFile("file2.txt"), "first line\n", true);
		
		// create a merge subscriber
		CVSMergeSubscriber subscriber = createMergeSubscriber(project, root, branch);
		
		// check the sync states
		assertSyncEquals("testMergableConflicts", subscriber, project, 
			new String[] { "file1.txt", "file2.txt"}, 
			true, new int[] {
				SyncInfo.CONFLICTING | SyncInfo.CHANGE, 
				SyncInfo.CONFLICTING | SyncInfo.CHANGE});
		
		// cancel the subscriber
		subscriber.cancel();			
	}
	
	public void testDisconnectingProject() throws CoreException, IOException, TeamException, InterruptedException {
		// Create a test project (which commits it as well)
		//		Create a test project
		 IProject project = createProject("testDisconnect", new String[] { "file1.txt", "file2.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		 setContentsAndEnsureModified(project.getFile("file1.txt"), "some text\nwith several lines\n");
		 setContentsAndEnsureModified(project.getFile("file2.txt"), "some text\nwith several lines\n");
		 commitProject(project);
	
		 // Checkout and branch a copy
		 CVSTag root = new CVSTag("root_branch1", CVSTag.VERSION);
		 CVSTag branch = new CVSTag("branch1", CVSTag.BRANCH);
		 IProject branchedProject = branchProject(project, root, branch);
	
		 // modify the branch
		 appendText(branchedProject.getFile("file1.txt"), "first line\n", true);
		 appendText(branchedProject.getFile("file2.txt"), "last line\n", false);
		 commitProject(branchedProject);
	
		 // create a merge subscriber
		 CVSMergeSubscriber subscriber = createMergeSubscriber(project, root, branch);
		
		ICVSFolder cvsProject = CVSWorkspaceRoot.getCVSFolderFor(project);
		CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(project);
		cvsProject.unmanage(new NullProgressMonitor());
		assertProjectRemoved(subscriber, project);
	}
	
	public void testMarkAsMerged() throws IOException, TeamException, CoreException, InvocationTargetException, InterruptedException {
		// Create a test project
		IProject project = createProject("testMarkAsMerged", new String[] { "delete.txt", "file1.txt", "file2.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		setContentsAndEnsureModified(project.getFile("file1.txt"), "some text\nwith several lines\n");
		setContentsAndEnsureModified(project.getFile("file2.txt"), "some text\nwith several lines\n");
		commitProject(project);

		// Checkout and branch a copy
		CVSTag root = new CVSTag("root_branch1", CVSTag.VERSION);
		CVSTag branch = new CVSTag("branch1", CVSTag.BRANCH);
		IProject branchedProject = branchProject(project, root, branch);
		
		// modify the branch
		appendText(branchedProject.getFile("file1.txt"), "first line\n", true);
		appendText(branchedProject.getFile("file2.txt"), "last line\n", false);
		addResources(branchedProject, new String[] {"addition.txt"}, false);
		deleteResources(branchedProject, new String[] {"delete.txt", "folder1/a.txt"}, false);
		setContentsAndEnsureModified(branchedProject.getFile("folder1/b.txt"));
		commitProject(branchedProject);
		
		// modify local workspace
		appendText(project.getFile("file1.txt"), "conflict line\n", true);
		setContentsAndEnsureModified(project.getFile("folder1/a.txt"));
		setContentsAndEnsureModified(project.getFile("delete.txt"));
		addResources(project, new String[] {"addition.txt"}, false);
		appendText(project.getFile("file2.txt"), "conflict line\n", false);
		
		// create a merge subscriber
		CVSMergeSubscriber subscriber = createMergeSubscriber(project, root, branch);
		
		// check the sync states
		assertSyncEquals("testMarkAsMergedConflicts", subscriber, project, 
			new String[] { "delete.txt", "file1.txt", "file2.txt", "addition.txt", "folder1/a.txt"}, 
			true, new int[] {
				SyncInfo.CONFLICTING | SyncInfo.CHANGE,
				SyncInfo.CONFLICTING | SyncInfo.CHANGE, 
				SyncInfo.CONFLICTING | SyncInfo.CHANGE,
				SyncInfo.CONFLICTING | SyncInfo.ADDITION,
				SyncInfo.CONFLICTING | SyncInfo.CHANGE});

		markAsMerged(subscriber, project, new String[] { "delete.txt", "file1.txt", "file2.txt", "addition.txt", "folder1/a.txt"});
		
		// check the sync states
		assertSyncEquals("testMarkAsMerged", subscriber, project, 
			 new String[] { "delete.txt", "file1.txt", "file2.txt", "addition.txt", "folder1/a.txt"}, 
			 true, new int[] {
				SyncInfo.IN_SYNC,
				SyncInfo.IN_SYNC, 
				SyncInfo.IN_SYNC,
				SyncInfo.IN_SYNC,
				SyncInfo.IN_SYNC});				
	} 
}

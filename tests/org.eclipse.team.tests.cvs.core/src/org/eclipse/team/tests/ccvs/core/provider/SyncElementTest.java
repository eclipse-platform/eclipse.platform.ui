package org.eclipse.team.tests.ccvs.core.provider;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.EclipseTest;
import org.eclipse.team.tests.ccvs.core.JUnitTestCase;

public class SyncElementTest extends EclipseTest {

	/**
	 * Constructor for SyncElementTest.
	 */
	public SyncElementTest() {
		super();
	}

	/**
	 * Constructor for SyncElementTest.
	 * @param name
	 */
	public SyncElementTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(SyncElementTest.class);
		return new CVSTestSetup(suite);
		//return new CVSTestSetup(new SyncElementTest("testAdditionConflicts"));
	}
	
	/*
	 * Get the child in the sync tree
	 */
	protected ILocalSyncElement getChild(ILocalSyncElement tree, IPath path) throws TeamException {
		if (path.segmentCount() == 0)
			return tree;
		ILocalSyncElement[] children = tree.members(DEFAULT_MONITOR);
		for (int i=0;i<children.length;i++) {
			if (children[i].getName().equals(path.segment(0)))
				return getChild(children[i], path.removeFirstSegments(1));
		}
		assertTrue("Child " + path.toString() + " does not exist", false);
		return null;
	}
	
	/*
	 * Assert that the specified resources in the tree have the specified sync kind
	 * Ignore conflict types if they are not specified in the assert statement
	 */
	public void assertSyncEquals(String message, ILocalSyncElement tree, String[] resources, int[] syncKinds, int granularity) throws TeamException {
		assertTrue(resources.length == syncKinds.length);
		for (int i=0;i<resources.length;i++) {
			int conflictTypeMask = 0x1F; // ignore manual and auto merge sync types for now.
			int kind = getChild(tree, new Path(resources[i])).getSyncKind(granularity, DEFAULT_MONITOR) & conflictTypeMask;
			int kindOther = syncKinds[i] & conflictTypeMask;
			assertTrue(message + ": improper sync state for " + resources[i], kind == kindOther);
		}
	}

	public void assertSyncEquals(String message, ILocalSyncElement tree, String[] resources, int[] syncKinds) throws TeamException {
		assertSyncEquals(message, tree, resources, syncKinds, ILocalSyncElement.GRANULARITY_TIMESTAMP);
	}
	
	/*
	 * Update the sync info of the resources so they can be committed
	 */
	public void makeOutgoing(IRemoteSyncElement tree, String[] hierarchy) throws CoreException, TeamException {
		for (int i=0;i<hierarchy.length;i++) {
			((CVSRemoteSyncElement)getChild(tree, new Path(hierarchy[i]))).makeOutgoing(DEFAULT_MONITOR);
		}
	}
	
	public void makeIncoming(IRemoteSyncElement tree, String[] hierarchy) throws CoreException, TeamException {
		for (int i=0;i<hierarchy.length;i++) {
			((CVSRemoteSyncElement)getChild(tree, new Path(hierarchy[i]))).makeIncoming(DEFAULT_MONITOR);
		}
	}
	
	public void makeInSync(IRemoteSyncElement tree, String[] hierarchy) throws CoreException, TeamException {
		for (int i=0;i<hierarchy.length;i++) {
			((CVSRemoteSyncElement)getChild(tree, new Path(hierarchy[i]))).makeInSync(DEFAULT_MONITOR);
		}
	}
	/* 
	 * Assert that the named resources have no local resource or sync info
	 */
	public void assertDeleted(String message, IRemoteSyncElement tree, String[] resources) throws CoreException, TeamException {
		for (int i=0;i<resources.length;i++) {
			try {
				getChild(tree, new Path(resources[i]));
			} catch (AssertionFailedError e) {
				break;
			}
			assertTrue(message + ": resource " + resources[i] + " still exists in some form", false);
		}
	}
	
	/*
	 * Perform a simple test that checks for the different types of incoming changes
	 */
	public void testIncomingChanges() throws TeamException, CoreException, IOException {
		// Create a test project
		IProject project = createProject("testIncomingChanges", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		
		// Checkout and modify a copy
		IProject copy = checkoutCopy(project, "-copy");
		IFile file = copy.getFile("folder1/a.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		addResources(copy, new String[] { "folder2/folder3/add.txt" }, false);
		deleteResources(copy, new String[] {"folder1/b.txt"}, false);
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		getProvider(copy).checkin(new IResource[] {copy}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);

		// Get the sync tree for the project
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testIncomingChanges", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt", "folder2/", "folder2/folder3/", "folder2/folder3/add.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.DELETION,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION});
				
		// Catch up to the incoming changes
		// XXX SPECIAL CASE: Update must be run on a resource whose parent is managed at the time of the update.
		makeInSync(tree, new String[] {"folder2/", "folder2/folder3/"});
		updateResources(project, new String[] {"folder1/a.txt", "folder1/b.txt", /* "folder2/", "folder2/folder3/", */ "folder2/folder3/add.txt"}, false);
		
		// Verify that we are in sync (except for "folder1/b.txt", which was deleted)
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testIncomingChanges", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder2/", "folder2/folder3/", "folder2/folder3/add.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC});
		
		// Ensure "folder1/b.txt" was deleted
		assertDeleted("testIncomingChanges", tree, new String[] {"folder1/b.txt"});
				
		// Verify that the copy equals the original
		assertEquals(project, copy);
	}
	
	/*
	 * Perform a simple test that checks for the different types of outgoing changes
	 */
	public void testOutgoingChanges() throws TeamException, CoreException {
		// Create a test project (which commits it as well)
		IProject project = createProject("testIncomingChanges", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		
		// Make some modifications
		IFile file = project.getFile("folder1/a.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		addResources(project, new String[] { "folder2/folder3/add.txt" }, false);
		deleteResources(project, new String[] {"folder1/b.txt"}, false);

		// Get the sync tree for the project
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testOutgoingChanges", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt", "folder2/", "folder2/folder3/", "folder2/folder3/add.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.DELETION,
				IRemoteSyncElement.IN_SYNC, /* adding a folder creates it remotely */
				IRemoteSyncElement.IN_SYNC, /* adding a folder creates it remotely */
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.ADDITION});
				
		// Commit the changes
		commitResources(project, new String[] {"folder1/a.txt", "folder1/b.txt", "folder2/folder3/add.txt"});
		
		// Ensure we're in sync
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testOutgoingChanges", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder2/", "folder2/folder3/", "folder2/folder3/add.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC});
				
		// Ensure deleted resource "folder1/b.txt" no longer exists
		assertDeleted("testOutgoingChanges", tree, new String[] {"folder1/b.txt"});
	}
	
	/*
	 * Perform a test that checks for outgoing changes that are CVS questionables (no add or remove)
	 */
	public void testOutgoingQuestionables() throws TeamException, CoreException {
		// Create a test project (which commits it as well)
		IProject project = createProject("testIncomingChanges", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		
		// Make some modifications
		buildResources(project, new String[] {"folder2/folder3/add.txt"}, false);
		IFile file = project.getFile("folder1/b.txt");
		file.delete(true, DEFAULT_MONITOR);

		// Get the sync tree for the project
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testOutgoingQuestionables", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt", "folder2/", "folder2/folder3/", "folder2/folder3/add.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.DELETION,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.ADDITION});
				
		// Update the resource sync info so the resources can be commited
		// Merge won't work for folders so we'll add them explicilty!!!
		addResources(project, new String[] {"folder2/", "folder2/folder3/", "folder2/folder3/add.txt"}, false);
		deleteResources(project, new String[] {"folder1/b.txt"}, false);
		commitResources(project, new String[] {"folder1/b.txt", "folder2/folder3/add.txt"});
		
		// Ensure we are in sync
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testOutgoingQuestionables", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder2/", "folder2/folder3/", "folder2/folder3/add.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC});
				
		// Ensure "folder1/b.txt" was deleted
		assertDeleted("testOutgoingQuestionables", tree, new String[] {"folder1/b.txt"});
	}
	
	/*
	 * Test simple file conflicts
	 */
	public void testFileConflict() throws TeamException, CoreException, IOException {
		// Create a test project (which commits it as well)
		IProject project = createProject("testFileConflict", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		
		// Checkout a copy and make some modifications
		IProject copy = checkoutCopy(project, "-copy");
		IFile file = copy.getFile("file1.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		appendText(file, "prefix\n", true);
		file = copy.getFile("folder1/a.txt");
		file.setContents(new ByteArrayInputStream("Use a custom string to avoid intermitant errors!".getBytes()), false, false, null);
		getProvider(copy).checkin(new IResource[] {copy}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);

		// Make the same modifications to the original (We need to test both M and C!!!)
		file = project.getFile("file1.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		appendText(file, "\npostfix", false); // This will test merges (M)
		file = project.getFile("folder1/a.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null); // This will test conflicts (C)

		// Get the sync tree for the project
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testFileConflict", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt"}, 
			new int[] {
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE });
		
		// Catch up to the file1.txt conflict using UPDATE with ignoreLocalChanges
		getProvider(project).update(new IResource[] {project.getFile("file1.txt")}, 
			new Command.LocalOption[] {Update.IGNORE_LOCAL_CHANGES, Command.DO_NOT_RECURSE}, 
			null, true /*createBackups*/, DEFAULT_MONITOR);					 
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testFileConflict", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE });
				
		// Release the folder1/a.txt conflict by merging and then committing
		makeOutgoing(tree, new String[] {"folder1/a.txt"});
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testFileConflict", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.CHANGE });
		getProvider(project).checkin(new IResource[] {project.getFile("folder1/a.txt")}, IResource.DEPTH_ZERO, DEFAULT_MONITOR);
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testFileConflict", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC });
	}
	
	/*
	 * Test conflicts involving additions
	 */
	public void testAdditionConflicts() throws TeamException, CoreException {
		
		// CASE 1: The user adds (using CVS add) a remotely added file
		//     (a) catchup is simply get?
		//     (b) release must do a merge
		// CASE 2: The user adds (but not using cvs add) a remotely added file
		//     (a) catchup is simply get?
		//     (b) release must do a merge
		// CASE 3: The user adds a remotely added then deleted file
		//     catchup is not applicable
		//     release is normal
		
		// Create a test project (which commits it as well) and add an uncommited resource
		IProject project = createProject("testAdditionConflicts", new String[] { "file.txt"});
		addResources(project, new String[] { "add1a.txt", "add1b.txt" }, false);
		addResources(project, new String[] { "add3.txt" }, false);
		buildResources(project, new String[] {"add2a.txt", "add2b.txt"}, false);
		
		// Checkout a copy, add the same resource and commit
		IProject copy = checkoutCopy(project, "-copy");
		addResources(copy, new String[] { "add1a.txt", "add1b.txt", "add2a.txt", "add2b.txt", "add3.txt"}, true);
		deleteResources(copy, new String[] { "add3.txt"}, true);

		// Get the sync tree for the project
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testAdditionConflicts", tree, 
			new String[] { "file.txt", "add1a.txt", "add1b.txt", "add2a.txt", "add2b.txt", "add3.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.ADDITION });
				
		// Release the conflict cases (MERGE is not required for add3.txt but we do it anyway to ensure it doesn't cause problems)
		makeOutgoing(tree, new String[]{"add1b.txt", "add2b.txt", "add3.txt"});
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testAdditionConflicts", tree, 
			new String[] { "file.txt", "add1b.txt", "add2b.txt", "add3.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.ADDITION });
		getProvider(project).checkin(new IResource[] {project.getFile("add1b.txt"), project.getFile("add2b.txt"), project.getFile("add3.txt")}, IResource.DEPTH_ZERO, DEFAULT_MONITOR);
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testAdditionConflicts", tree, 
			new String[] { "file.txt", "add1b.txt", "add2b.txt", "add3.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC });
				
		// Catch-up to conflicting cases using UPDATE
		// XXX SPECIAL CASE: We need to unmanage the resources and delete it before getting the remote
		makeIncoming(tree, new String[] {"add1a.txt"});
		IFile file = project.getFile("add1a.txt");
		file.delete(false, DEFAULT_MONITOR);
		file = project.getFile("add2a.txt");
		file.delete(false, DEFAULT_MONITOR);
		getProvider(project).update(new IResource[] {project.getFile("add1a.txt"), project.getFile("add2a.txt")}, 
			new Command.LocalOption[] {Command.DO_NOT_RECURSE}, 
			null, true /*createBackups*/, DEFAULT_MONITOR);
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testAdditionConflicts", tree, 
			new String[] { "add1a.txt", "add2a.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC });
	}
	
	/*
	 * Test conflicts involving deletions
	 */
	public void testDeletionConflicts() throws TeamException, CoreException {
		
		// CASE 1: The user deletes a remotely modified file
		//    (a) catchup must do an update
		//    (b) release must do a merge
		// CASE 2: The user deletes (and removes) a remotely modified file	
		//    (a) catchup must do an unmanage and update
		//    (b) release must do a merge
		// CASE 3: The user modified a remotely deleted file
		//    (a) catchup must do an unmanage and local delete
		//    (b) release must do a merge
		// CASE 4: The user deletes a remotely deleted file
		//    (a) catchup can update (or unmanage?)
		//    (b) release must unmanage
		// CASE 5: The user deletes (and removes) a remotely deleted file
		//    (a) catchup can update (or unmanage?)
		//    (b) release must unmanage
		
		// Perform the test case for case A first
		
		// Create a test project (which commits it as well) and delete the resource without committing
		IProject project = createProject("testDeletionConflictsA", new String[] { "delete1.txt", "delete2.txt", "delete3.txt", "delete4.txt", "delete5.txt"});
		IFile file = project.getFile("delete1.txt"); // WARNING: This does a "cvs remove"!!!
		file.delete(false, DEFAULT_MONITOR);
		deleteResources(project, new String[] {"delete2.txt"}, false);
		file = project.getFile("delete3.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		file = project.getFile("delete4.txt");
		file.delete(false, DEFAULT_MONITOR);
		deleteResources(project, new String[] {"delete5.txt"}, false);
		
		// Checkout a copy and commit the deletion
		IProject copy = checkoutCopy(project, "-copy");
		file = copy.getFile("delete1.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		file = copy.getFile("delete2.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		deleteResources(copy, new String[] {"delete3.txt", "delete4.txt", "delete5.txt"}, false);
		getProvider(copy).checkin(new IResource[] {copy}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);

		
		// Get the sync tree for the project
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testDeletionConflictsA", tree, 
			new String[] { "delete1.txt", "delete2.txt", "delete3.txt", "delete4.txt", "delete5.txt"}, 
			new int[] {
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC });
				
		// Catch up to remote changes.
		// XXX SPECIAL CASE: delete1.txt must be unmanaged before the catch-up
		makeIncoming(tree, new String[] {"delete1.txt"});
		// XXX SPECIAL CASE: delete2.txt must be unmanaged before the catch-up
		makeIncoming(tree, new String[] {"delete2.txt"});
		// XXX SPECIAL CASE: delete3.txt must ignore local changes (and -C doesn't work so we'll unmanage and delete the local resource)
		makeIncoming(tree, new String[] {"delete3.txt"});
		project.getFile("delete3.txt").delete(false, DEFAULT_MONITOR);
		updateResources(project, new String[] {"delete1.txt", "delete2.txt", "delete3.txt", "delete4.txt", "delete5.txt"}, true);
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testDeletionConflictsA", tree, 
			new String[] { "delete1.txt", "delete2.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC });
		assertDeleted("testDeletionConflictsA", tree, new String[] {"delete3.txt", "delete4.txt", "delete5.txt"});
		
		// Now redo the test case for case B
		
		// Create a test project (which commits it as well) and delete the resource without committing
		project = createProject("testDeletionConflictsB", new String[] { "delete1.txt", "delete2.txt", "delete3.txt", "delete4.txt", "delete5.txt"});
		file = project.getFile("delete1.txt");
		file.delete(false, DEFAULT_MONITOR);
		deleteResources(project, new String[] {"delete2.txt"}, false);
		file = project.getFile("delete3.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		file = project.getFile("delete4.txt");
		file.delete(false, DEFAULT_MONITOR);
		deleteResources(project, new String[] {"delete5.txt"}, false);
		
		// Checkout a copy and commit the deletion
		copy = checkoutCopy(project, "-copy");
		file = copy.getFile("delete1.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		file = copy.getFile("delete2.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		deleteResources(copy, new String[] {"delete3.txt", "delete4.txt", "delete5.txt"}, false);
		getProvider(copy).checkin(new IResource[] {copy}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);

		
		// Get the sync tree for the project
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testDeletionConflictsB", tree, 
			new String[] { "delete1.txt", "delete2.txt", "delete3.txt", "delete4.txt", "delete5.txt"}, 
			new int[] {
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC });

		// Release the resources
		// XXX SPECIAL CASE: "delete1.txt", "delete2.txt" and "delete3.txt" must be merged
		makeOutgoing(tree, new String[]{"delete1.txt", "delete2.txt", "delete3.txt"});
		// XXX SPECIAL CASE: "delete4.txt" and "delete5.txt" must be unmanaged
		unmanageResources(project, new String[]{"delete4.txt", "delete5.txt"});
		commitResources(project, new String[] { "delete1.txt", "delete2.txt", "delete3.txt", "delete4.txt", "delete5.txt"});
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testDeletionConflictsB", tree, 
			new String[] { "delete3.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC });
		assertDeleted("testDeletionConflictsB", tree, new String[] {"delete1.txt", "delete2.txt", "delete4.txt", "delete5.txt"});
	}
	
	/*
	 * Test the creation and sync of an empty local project that has remote contents
	 */
	public void testSyncOnEmptyProject() throws TeamException {
	}
	
	/*
	 * Test syncing on a folder that has been deleted from the server
	 */
	public void testSyncOnDeletedFolder() throws TeamException {
	}
	
	/*
	 * Test syncing on a folder that is empty on the server and has been pruned, then added locally
	 */
	public void testSyncOnPrunedFolder() throws TeamException {
	}
	
	/*
	 * Test sync involving pruned directories
	 */
	public void testSyncWithPruning() throws TeamException {
	}
	
	/*
	 * Test a conflict with an incomming foler addition and an unmanaqged lcoal folder
	 */
	public void testFolderConflict()  throws TeamException, CoreException {
		
		// Create a test project (which commits it as well) and delete the resource without committing
		IProject project = createProject("testFolderConflict", new String[] { "file.txt"});
		
		// Checkout a copy and add some folders
		IProject copy = checkoutCopy(project, "-copy");
		addResources(copy, new String[] {"folder1/file.txt", "folder2/file.txt"}, true);
		
		// Add a folder to the original project (but not using cvs)
		IResource[] resources = buildResources(project, new String[] {"folder1/"});
		((IFolder)resources[0]).create(false, true, DEFAULT_MONITOR);
		
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testFolderConflict", tree, 
			new String[] { "file.txt", "folder1/", "folder1/file.txt", "folder2/", "folder2/file.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION});
				
		makeInSync(tree, new String[] {"folder1/"});
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testFolderConflict", tree, 
			new String[] { "file.txt", "folder1/", "folder1/file.txt", "folder2/", "folder2/file.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION});
	}
	 
	/*
	 * Test that a deleted file can still be deleted through the team provider
	 */
	public void testOutgoingDeletion() throws TeamException, CoreException {
		
		// Create a test project (which commits it as well)
		IProject project = createProject("testOutgoingDeletion", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		
		// Delete a file
		IFile file = project.getFile("folder1/b.txt");
		file.delete(true, DEFAULT_MONITOR); // WARNING: As of 2002/03/05, this is equivalent to a cvs remove

		// Get the sync tree for the project
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testOutgoingDeletion", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.DELETION});
				
		// Commit the deletion
		getProvider(file).checkin(new IResource[] {file}, IResource.DEPTH_ZERO, DEFAULT_MONITOR);
		
		// Get the sync tree again for the project and ensure others aren't effected
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testOutgoingDeletion", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC});
				
		// Assert that deletion no longer appears in remote tree
		assertDeleted("testOutgoingDeletion", tree, new String[] {"folder1/b.txt"});
	}
	
	/*
	 * Test catching up to an incoming addition
	 */
	public void testIncomingAddition() throws TeamException, CoreException {
		// Create a test project
		IProject project = createProject("testIncomingAddition", new String[] { "file1.txt", "folder1/", "folder1/a.txt"});
		
		// Checkout and modify a copy
		IProject copy = checkoutCopy(project, "-copy");
		addResources(copy, new String[] { "folder1/add.txt" }, true);

		// Get the sync tree for the project
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testIncomingAddition", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/add.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION});
		
		// Get the resource from the tree
		ILocalSyncElement element = getChild(tree, new Path("folder1/add.txt"));
		
		// Catch up to the addition by updating
		getProvider(project).update(new IResource[] {element.getLocal()}, new Command.LocalOption[] {Command.DO_NOT_RECURSE}, 
			null, true /*createBackups*/, DEFAULT_MONITOR);
		
		// Get the sync tree again for the project and ensure the added resource is in sync
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testIncomingAddition", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/add.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC});
	}
	
	/* 
	 * Test changes using a granularity of contents
	 */
	 public void testGranularityContents() throws TeamException, CoreException, IOException {
	 	// Create a test project (which commits it as well)
		IProject project = createProject("testGranularityContents", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		
		// Checkout a copy and make some modifications
		IProject copy = checkoutCopy(project, "-copy");
		IFile file = copy.getFile("file1.txt");
		appendText(file, "", true);
		file = copy.getFile("folder1/a.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		getProvider(copy).checkin(new IResource[] {copy}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);

		// Make the same modifications to the original
		file = project.getFile("file1.txt");
		appendText(file, "", false);
		file = project.getFile("folder1/a.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(new ByteArrayInputStream("unique text".getBytes()), false, false, null);

		// Get the sync tree for the project
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testGranularityContents", tree, 
			new String[] { "file1.txt", "folder1/", "folder1/a.txt"}, 
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE },
			IRemoteSyncElement.GRANULARITY_CONTENTS);
	 }
	 
	 public void testGetBase() throws TeamException, CoreException, IOException {
		// Create a test project (which commits it as well)
		IProject project = createProject("testIncomingChanges", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		
		// Checkout and modify a copy
		IProject copy = checkoutCopy(project, "-copy");
		
		// Make some modifications
		IFile file = project.getFile("folder1/a.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		addResources(project, new String[] { "folder1/add.txt" }, false);
		deleteResources(project, new String[] {"folder1/b.txt"}, false);
		
		// Get the sync tree for the project
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertEquals(Path.EMPTY, (ICVSResource)tree.getBase(), CVSWorkspaceRoot.getCVSResourceFor(copy), false, false);

	 }
	 
	 public void testSimpleMerge() throws TeamException, CoreException, IOException {
		// Create a test project (which commits it as well)
		IProject project = createProject("testSimpleMerge", new String[] { "file1.txt", "file2.txt", "file3.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
	
		// Checkout and modify a copy
		IProject copy = checkoutCopy(project, "-copy");
		copy.refreshLocal(IResource.DEPTH_INFINITE, DEFAULT_MONITOR);
		
		tagProject(project, new CVSTag("v1", CVSTag.VERSION));
		tagProject(project, new CVSTag("branch1", CVSTag.BRANCH));
		
		getProvider(copy).update(new IResource[] {copy}, Command.NO_LOCAL_OPTIONS,
			new CVSTag("branch1", CVSTag.BRANCH), true /*createBackups*/, DEFAULT_MONITOR);
		
		// make changes on the branch		
		addResources(copy, new String[] {"addition.txt", "folderAddition/", "folderAddition/new.txt"}, true);
		deleteResources(copy, new String[] {"folder1/b.txt"}, true);
		changeResources(copy, new String[] {"file1.txt", "file2.txt"}, true);
		
		// make change to workspace working on HEAD
		changeResources(project, new String[] {"file2.txt"}, false);
		changeResources(project, new String[] {"file3.txt"}, true);
		
		IRemoteResource base = CVSWorkspaceRoot.getRemoteTree(project, new CVSTag("v1", CVSTag.VERSION), DEFAULT_MONITOR);
		IRemoteResource remote = CVSWorkspaceRoot.getRemoteTree(project, new CVSTag("branch1", CVSTag.BRANCH), DEFAULT_MONITOR);
		IRemoteSyncElement tree = new CVSRemoteSyncElement(true /*three way*/, project, base, remote);
		
		// watch for empty directories and the prune option!!!
		assertSyncEquals("testSimpleMerge sync check", tree,
						 new String[] { "addition.txt", "folderAddition/", "folderAddition/new.txt", 
						 				 "folder1/b.txt", "file1.txt", "file2.txt", "file3.txt"},
						 new int[] { IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION,
						 			  IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION,
						 			  IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION,
						 			  IRemoteSyncElement.INCOMING | IRemoteSyncElement.DELETION,
						 			  IRemoteSyncElement.INCOMING | IRemoteSyncElement.CHANGE,
						 			  IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
						 			  IRemoteSyncElement.OUTGOING | IRemoteSyncElement.CHANGE });				 			  					 			  
	 }
	 
	 public void testSyncOnBranch() throws TeamException, CoreException, IOException {
	 	
	 	// Create a test project and a branch
		IProject project = createProject("testSyncOnBranch", new String[] { "file1.txt", "file2.txt", "file3.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		CVSTag branch = new CVSTag("branch1", CVSTag.BRANCH);
		tagProject(project, branch);
		getProvider(project).update(new IResource[] {project}, Command.NO_LOCAL_OPTIONS, branch, true /*createBackups*/, DEFAULT_MONITOR);

		// Checkout and modify a copy
		IProject copy = checkoutCopy(project, branch);
		addResources(copy, new String[] {"addition.txt", "folderAddition/", "folderAddition/new.txt"}, true);
		deleteResources(copy, new String[] {"folder1/b.txt"}, true);
		changeResources(copy, new String[] {"file1.txt", "file2.txt"}, true);
		
		// Sync on the original and assert the result equals the copy
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, null, DEFAULT_MONITOR);
		assertEquals(Path.EMPTY, (ICVSResource)tree.getRemote(), CVSWorkspaceRoot.getCVSResourceFor(copy), false, false);
	 }
	 
 	public void testRenameProject() throws TeamException, CoreException, IOException {
		String[] resourceNames = new String[] { "changed.txt", "folder1/", "folder1/a.txt" };
		int[] inSync = new int[] {IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC};
		IProject project = createProject("testRenameProject", new String[] { "changed.txt", "folder1/", "folder1/a.txt" });
		
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("sync should be in sync", tree, resourceNames, inSync);
		IProjectDescription desc = project.getDescription();
		String newName = project.getName() + "_renamed";
		desc.setName(newName);
		project.move(desc, false, null);
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(newName);
		assertTrue(project.exists());
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("sync should be in sync", tree, resourceNames, inSync);
	}
	
	public void testFolderDeletion() throws TeamException, CoreException {
		
		IProject project = createProject("testFolderDeletion", new String[] { "changed.txt", "deleted.txt", "folder1/", "folder1/a.txt" });
		
		// Delete a folder and ensure that the file is managed but doesn't exist
		// (Special behavior is provider by the CVS move/delete hook but this is not part of CVS core)
		project.getFolder("folder1").delete(false, false, null);
		ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(project.getFolder("folder1"));
		assertTrue("Deleted folder not in proper state", ! folder.exists() && folder.isManaged());
		
		// The folder should show up as an incomming addition
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testFolderDeletion sync check", tree,
						 new String[] { "folder1", "folder1/a.txt"},
						 new int[] { IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION,
						 			  IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION});
		
		// Update and ensure everything is back
		updateProject(project, null, false);		 			  					 			  
		tree = CVSWorkspaceRoot.getRemoteSyncTree(project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertSyncEquals("testFolderDeletion sync check", tree,
						 new String[] { "folder1", "folder1/a.txt"},
						 new int[] { IRemoteSyncElement.IN_SYNC,
						 			  IRemoteSyncElement.IN_SYNC});
	}
	/**
	  * There is special handling required when building a sync tree for a tag when there are undiscovered folders
	  * that only contain other folders.
	  */
	 public void testTagRetrievalForFolderWithNoFile() throws TeamException, CoreException {
	 	IProject project = createProject("testTagRetrievalForFolderWithNoFile", new String[] { "changed.txt", "deleted.txt", "folder1/", "folder1/a.txt"});
		// Checkout, branch and modify a copy
		IProject copy = checkoutCopy(project, "-copy");
		CVSTag version = new CVSTag("v1", CVSTag.BRANCH);
		CVSTag branch = new CVSTag("branch1", CVSTag.BRANCH);
		getProvider(copy).makeBranch(new IResource[] {copy}, version, branch, true, true, DEFAULT_MONITOR);
		addResources(copy, new String[] {"folder2/folder3/a.txt"}, true);
		
		// Fetch the tree corresponding to the branch using the original as the base.
		// XXX This will fail for CVSNT with directory pruning on
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, branch, DEFAULT_MONITOR);
	 }
}
package org.eclipse.team.tests.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Member;

import junit.framework.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.*;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;
import org.eclipse.team.internal.core.target.UrlUtil;
import org.eclipse.team.internal.ui.target.RemoteTargetSyncElement;

public class SyncElementTest extends TeamTest {
	/*
	 * Constructor for SyncElementTest.
	 */
	public SyncElementTest() {
		super();
	}

	/*
	 * Constructor for SyncElementTest.
	 * @param name
	 */
	public SyncElementTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(SyncElementTest.class);
		return new TargetTestSetup(suite);
		//return new CVSTestSetup(new SyncElementTest("testAdditionConflicts"));
	}
	/*
	 * Get the child in the sync tree
	 */
	protected ILocalSyncElement getChild(ILocalSyncElement tree, IPath path) throws TeamException {
		if (path.segmentCount() == 0)
			return tree;
		ILocalSyncElement[] children = tree.members(DEFAULT_MONITOR);
		for (int i = 0; i < children.length; i++) {
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
	public void assertSyncEquals(String message, ILocalSyncElement tree, String[] resources, int[] syncKinds, int granularity)
		throws TeamException {
		assertTrue(resources.length == syncKinds.length);
		for (int i = 0; i < resources.length; i++) {
			int conflictTypeMask = 0x0F; // ignore manual and auto merge sync types for now.
			ILocalSyncElement child = getChild(tree, new Path(resources[i]));
			int kind = child.getSyncKind(granularity, DEFAULT_MONITOR) & conflictTypeMask;
			int kindOther = syncKinds[i] & conflictTypeMask;
			assertTrue(
				message
					+ ": improper sync state for "
					+ resources[i]
					+ " expected "
					+ RemoteSyncElement.kindToString(kindOther)
					+ " but was "
					+ RemoteSyncElement.kindToString(kind),
				kind == kindOther);
		}
	}

	public void assertSyncEquals(String message, ILocalSyncElement tree, String[] resources, int[] syncKinds) throws TeamException {
		assertSyncEquals(message, tree, resources, syncKinds, ILocalSyncElement.GRANULARITY_TIMESTAMP);
	}

	/* 
	 * Assert that the named resources have no local resource or sync info
	 */
	public void assertDeleted(String message, IRemoteSyncElement tree, String[] resources) throws CoreException, TeamException {
		for (int i = 0; i < resources.length; i++) {
			try {
				getChild(tree, new Path(resources[i]));
			} catch (AssertionFailedError e) {
				break;
			}
			assertTrue(message + ": resource " + resources[i] + " still exists in some form", false);
		}
	}

	IRemoteSyncElement getRemoteSyncTree(IProject project, IProgressMonitor monitor) throws TeamException {
		IRemoteSyncElement result = new RemoteTargetSyncElement(project,getProvider(project).getRemoteResource());
		return result;
	}
	protected IProject checkoutCopy(IProject project, String postfix) throws CoreException,TeamException {
		TargetProvider provider = getProvider(project);
		IProject result = getUniqueTestProject(project.getName()+postfix);
		TargetManager.map(result, provider.getSite(), UrlUtil.getTrailingPath(provider.getURL(), provider.getSite().getURL()));
		TargetProvider target = TargetManager.getProvider(result);
		target.get(new IResource[] { result }, null);
		return result;
	}
	protected void getResourcesFromTarget(IProject project, String[] resourceNames)throws TeamException,CoreException {
		TargetProvider provider=getProvider(project);
		IResource[] resources=getResources(project,resourceNames);
		provider.get(resources,null);
	}
	protected void putResourcesOntoTarget(IProject project, String[] resourceNames)throws TeamException,CoreException {
		TargetProvider provider=getProvider(project);
		IResource[] resources=getResources(project,resourceNames);
		provider.put(resources,null);
	}
	/**
	 * Add the resources to an existing container and optionally upload them to the remote server
	 */
	public IResource[] addResources(IProject container, String[] hierarchy, boolean checkin) throws CoreException, TeamException {
		IResource[] newResources = buildResources(container, hierarchy, false);
		if (checkin) getProvider(container).put(newResources, DEFAULT_MONITOR);
		return newResources;
	}
	/**
	 * Delete the resources from an existing container and optionally add the changes to the remote server
	 */
	public IResource[] deleteResources(IProject container, String[] hierarchy, boolean checkin) throws CoreException, TeamException {
		IResource[] resources = getResources(container, hierarchy);
		for (int i = 0; i < resources.length; i++) {
			resources[0].delete(true, null);
		}
		if (checkin) getProvider(container).put(resources, DEFAULT_MONITOR);
		return resources;
	}
	/*
	 * Perform a simple test that checks for the different types of incoming changes
	 */
	public void testIncomingChanges() throws TeamException, CoreException, IOException {
		// Create a test project
		IProject project = createAndPut("testIncomingChanges", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt" });

		// Checkout and modify a copy
		IProject copy = checkoutCopy(project, "-copy");
		IFile file = copy.getFile("folder1/a.txt");
		sleep(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(new ByteArrayInputStream("This will be different".getBytes()), false, false, null);
		addResources(copy, new String[] { "folder2/folder3/add.txt" }, false);
		deleteResources(copy, new String[] { "folder1/b.txt" }, false);
		sleep(1500); // Wait so that timestamp of modified file differs from original
		putResourcesOntoTarget(copy,new String[] { "folder1/a.txt","folder2/folder3/add.txt","folder1/b.txt" });

		// Get the sync tree for the project
		IRemoteSyncElement tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testIncomingChanges",
			tree,
			new String[] {
				"file1.txt",
				"folder1/",
				"folder1/a.txt",
				"folder1/b.txt",
				"folder2/",
				"folder2/folder3/",
				"folder2/folder3/add.txt" },
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.DELETION,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION });

		// Verify that we are in sync (except for "folder1/b.txt", which was deleted)
		getResourcesFromTarget(project, 
			new String[] {
				"file1.txt",
				"folder1/",
				"folder1/a.txt",
				"folder1/b.txt",
				"folder2/",
				"folder2/folder3/",
				"folder2/folder3/add.txt" });
		tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testIncomingChanges",
			tree,
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder2/", "folder2/folder3/", "folder2/folder3/add.txt" },
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC });

		// Ensure "folder1/b.txt" was deleted
		assertDeleted("testIncomingChanges", tree, new String[] { "folder1/b.txt" });

		// Verify that the copy equals the original
		assertEquals(project, copy);
	}

	/*
	 * Perform a simple test that checks for the different types of outgoing changes
	 */
	public void testOutgoingChanges() throws TeamException, CoreException {
		// Create a test project (which commits it as well)
		IProject project = createAndPut("testIncomingChanges", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt" });

		// Make some modifications
		IFile file = project.getFile("folder1/a.txt");
		sleep(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		addResources(project, new String[] { "folder2/folder3/add.txt" }, false);
		deleteResources(project, new String[] { "folder1/b.txt" }, false);

		// Get the sync tree for the project
		IRemoteSyncElement tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testOutgoingChanges",
			tree,
			new String[] {
				"file1.txt",
				"folder1/",
				"folder1/a.txt",
				"folder1/b.txt",
				"folder2/",
				"folder2/folder3/",
				"folder2/folder3/add.txt" },
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.DELETION,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.ADDITION,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.ADDITION });

		// Commit the changes
		putResourcesOntoTarget(project, new String[] { "folder1/a.txt", "folder1/b.txt", "folder2/folder3/add.txt" });

		// Ensure we're in sync
		tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testOutgoingChanges",
			tree,
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder2/", "folder2/folder3/", "folder2/folder3/add.txt" },
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC });

		// Ensure deleted resource "folder1/b.txt" no longer exists
		assertDeleted("testOutgoingChanges", tree, new String[] { "folder1/b.txt" });
	}

		/*
	 * Test simple file conflicts
	 */
	public void testFileConflict() throws TeamException, CoreException, IOException {
		// Create a test project (which commits it as well)
		IProject project = createAndPut("testFileConflict", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt" });

		// Checkout a copy and make some modifications
		IProject copy = checkoutCopy(project, "-copy");
		IFile file = copy.getFile("file1.txt");
		sleep(1500); // Wait so that timestamp of modified file differs from original
		appendText(file, "prefix\n", true);
		file = copy.getFile("folder1/a.txt");
		file.setContents(new ByteArrayInputStream("Use a custom string to avoid intermitant errors!".getBytes()), false, false, null);
		getProvider(copy).put(new IResource[] { copy }, DEFAULT_MONITOR);

		// Make the same modifications to the original (We need to test both M and C!!!)
		file = project.getFile("file1.txt");
		sleep(1500); // Wait so that timestamp of modified file differs from original
		appendText(file, "\npostfix", false); // This will test merges (M)
		file = project.getFile("folder1/a.txt");
		sleep(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null); // This will test conflicts (C)

		// Get the sync tree for the project
		IRemoteSyncElement tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testFileConflict",
			tree,
			new String[] { "file1.txt", "folder1/", "folder1/a.txt" },
			new int[] {
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE });

		// Catch up to the file1.txt conflict using UPDATE with ignoreLocalChanges
		getResourcesFromTarget(project, new String[] { "file1.txt" });
		tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testFileConflict",
			tree,
			new String[] { "file1.txt", "folder1/", "folder1/a.txt" },
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE });

		// Release the folder1/a.txt conflict uploading
		getProvider(project).put(new IResource[] { project.getFile("folder1/a.txt")}, DEFAULT_MONITOR);
		tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testFileConflict",
			tree,
			new String[] { "file1.txt", "folder1/", "folder1/a.txt" },
			new int[] { IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC });
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
		IProject project = createAndPut("testAdditionConflicts", new String[] { "file.txt" });
		addResources(project, new String[] { "add1a.txt", "add1b.txt" }, false);
		addResources(project, new String[] { "add3.txt" }, false);
		buildResources(project, new String[] { "add2a.txt", "add2b.txt" }, false); //useless code!???

		// Checkout a copy, add the same resource and commit
		IProject copy = checkoutCopy(project, "-copy");
		addResources(copy, new String[] { "add1a.txt", "add1b.txt", "add2a.txt", "add2b.txt", "add3.txt" }, true);
		deleteResources(copy, new String[] { "add3.txt" }, true);

		// Get the sync tree for the project
		IRemoteSyncElement tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testAdditionConflicts",
			tree,
			new String[] { "file.txt", "add1a.txt", "add1b.txt", "add2a.txt", "add2b.txt", "add3.txt" },
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.ADDITION });

		getProvider(project).put(
			new IResource[] { project.getFile("add1b.txt"), project.getFile("add2b.txt"), project.getFile("add3.txt")},
			DEFAULT_MONITOR);
		tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testAdditionConflicts",
			tree,
			new String[] { "file.txt", "add1b.txt", "add2b.txt", "add3.txt" },
			new int[] { IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC });

		IFile file = project.getFile("add1a.txt");
		file.delete(false, DEFAULT_MONITOR);
		file = project.getFile("add2a.txt");
		file.delete(false, DEFAULT_MONITOR);
		getResourcesFromTarget(project, new String[] { "add1a.txt","add2a.txt" });//This replaces the chunk commented out below:
		/*getProvider(project).update(
			new IResource[] { project.getFile("add1a.txt"), project.getFile("add2a.txt")},
			new Command.LocalOption[] { Command.DO_NOT_RECURSE },
			null,
			true,	//createBackups
			DEFAULT_MONITOR
		);*/
		tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testAdditionConflicts",
			tree,
			new String[] { "add1a.txt", "add2a.txt" },
			new int[] { IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC });
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
		IProject project =
			createAndPut(
				"testDeletionConflictsA",
				new String[] { "delete1.txt", "delete2.txt", "delete3.txt", "delete4.txt", "delete5.txt" });
		IFile file = project.getFile("delete1.txt");
		file.delete(false, DEFAULT_MONITOR);
		deleteResources(project, new String[] { "delete2.txt" }, false);
		file = project.getFile("delete3.txt");
		sleep(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(new ByteArrayInputStream("unique text for delete3.txt".getBytes()), false, false, null);
		file = project.getFile("delete4.txt");
		file.delete(false, DEFAULT_MONITOR);
		deleteResources(project, new String[] { "delete5.txt" }, false);

		// Checkout a copy and commit the deletion
		IProject copy = checkoutCopy(project, "-copy");
		file = copy.getFile("delete1.txt");
		sleep(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(new ByteArrayInputStream("unique text for delete1.txt".getBytes()), false, false, null);
		file = copy.getFile("delete2.txt");
		sleep(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(new ByteArrayInputStream("unique text for delete2.txt".getBytes()), false, false, null);
		deleteResources(copy, new String[] { "delete3.txt", "delete4.txt", "delete5.txt" }, false);
		getProvider(copy).put(new IResource[] { copy }, DEFAULT_MONITOR);

		// Get the sync tree for the project
		IRemoteSyncElement tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testDeletionConflictsA",
			tree,
			new String[] { "delete1.txt", "delete2.txt", "delete3.txt", "delete4.txt", "delete5.txt" },
			new int[] {
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE | IRemoteSyncElement.PSEUDO_CONFLICT,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE | IRemoteSyncElement.PSEUDO_CONFLICT });

		// Catch up to remote changes.

		project.getFile("delete3.txt").delete(false, DEFAULT_MONITOR);

		tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testDeletionConflictsA",
			tree,
			new String[] { "delete1.txt", "delete2.txt" },
			new int[] {
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE | IRemoteSyncElement.PSEUDO_CONFLICT,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE | IRemoteSyncElement.PSEUDO_CONFLICT });
		assertDeleted("testDeletionConflictsA", tree, new String[] { "delete3.txt", "delete4.txt", "delete5.txt" });

		// Now redo the test case for case B

		// Create a test project (which commits it as well) and delete the resource without committing
		project =
			createAndPut(
				"testDeletionConflictsB",
				new String[] { "delete1.txt", "delete2.txt", "delete3.txt", "delete4.txt", "delete5.txt" });
		file = project.getFile("delete1.txt");
		file.delete(false, DEFAULT_MONITOR);
		deleteResources(project, new String[] { "delete2.txt" }, false);
		file = project.getFile("delete3.txt");
		sleep(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		file = project.getFile("delete4.txt");
		file.delete(false, DEFAULT_MONITOR);
		deleteResources(project, new String[] { "delete5.txt" }, false);

		// Checkout a copy and commit the deletion
		copy = checkoutCopy(project, "-copy");
		file = copy.getFile("delete1.txt");
		sleep(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		file = copy.getFile("delete2.txt");
		sleep(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		deleteResources(copy, new String[] { "delete3.txt", "delete4.txt", "delete5.txt" }, false);
		getProvider(copy).put(new IResource[] { copy }, DEFAULT_MONITOR);

		// Get the sync tree for the project
		tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testDeletionConflictsB",
			tree,
			new String[] { "delete1.txt", "delete2.txt", "delete3.txt", "delete4.txt", "delete5.txt" },
			new int[] {
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC });

		// Release the resources

		putResourcesOntoTarget(project, new String[] { "delete1.txt", "delete2.txt", "delete3.txt", "delete4.txt", "delete5.txt" });
		tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals("testDeletionConflictsB", tree, new String[] { "delete3.txt" }, new int[] { IRemoteSyncElement.IN_SYNC });
		assertDeleted("testDeletionConflictsB", tree, new String[] { "delete1.txt", "delete2.txt", "delete4.txt", "delete5.txt" });
	}

	/*
	* Test that a deleted file can still be deleted through the team provider
	*/
	public void testOutgoingDeletion() throws TeamException, CoreException {

		// Create a test project (which commits it as well)
		IProject project = createAndPut("testOutgoingDeletion", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt" });

		// Delete a file
		IFile file = project.getFile("folder1/b.txt");
		file.delete(true, DEFAULT_MONITOR); // WARNING: As of 2002/03/05, this is equivalent to a cvs remove

		// Get the sync tree for the project
		IRemoteSyncElement tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testOutgoingDeletion",
			tree,
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt" },
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.DELETION });

		// Commit the deletion
		getProvider(file.getProject()).put(new IResource[] { file }, DEFAULT_MONITOR);

		// Get the sync tree again for the project and ensure others aren't effected
		tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testOutgoingDeletion",
			tree,
			new String[] { "file1.txt", "folder1/", "folder1/a.txt" },
			new int[] { IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC });

		// Assert that deletion no longer appears in remote tree
		assertDeleted("testOutgoingDeletion", tree, new String[] { "folder1/b.txt" });
	}

	/*
	 * Test catching up to an incoming addition
	 */
	public void testIncomingAddition() throws TeamException, CoreException {
		// Create a test project
		IProject project = createAndPut("testIncomingAddition", new String[] { "file1.txt", "folder1/", "folder1/a.txt" });

		// Checkout and modify a copy
		IProject copy = checkoutCopy(project, "-copy");
		addResources(copy, new String[] { "folder1/add.txt" }, true);

		// Get the sync tree for the project
		IRemoteSyncElement tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testIncomingAddition",
			tree,
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/add.txt" },
			new int[] {
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION });

		// Get the resource from the tree
		ILocalSyncElement element = getChild(tree, new Path("folder1/add.txt"));

		// Catch up to the addition by updating
		getResourcesFromTarget(project, new String[] { "folder1/add.txt" });

		// Get the sync tree again for the project and ensure the added resource is in sync
		tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testIncomingAddition",
			tree,
			new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/add.txt" },
			new int[] { IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC });
	}

	/* 
	 * Test changes using a granularity of contents
	 */
	public void testGranularityContents() throws TeamException, CoreException, IOException {
		// Create a test project (which commits it as well)
		IProject project =
			createAndPut("testGranularityContents", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt" });

		// Checkout a copy and make some modifications
		IProject copy = checkoutCopy(project, "-copy");
		IFile file = copy.getFile("file1.txt");
		sleep(1500); // Wait so that timestamp of modified file differs from original
		appendText(file, "a", true);
		file = copy.getFile("folder1/a.txt");
		file.setContents(getRandomContents(), false, false, null);
		putResourcesOntoTarget(copy, new String[] { "file1.txt","folder1/a.txt" });

		// Make the same modifications to the original
		file = project.getFile("file1.txt");
		sleep(1500); // Wait so that timestamp of modified file differs from original
		appendText(file, "a", false);
		file = project.getFile("folder1/a.txt");
		file.setContents(new ByteArrayInputStream("unique text".getBytes()), false, false, null);

		// Get the sync tree for the project
		IRemoteSyncElement tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testGranularityContents",
			tree,
			new String[] { "file1.txt", "folder1/", "folder1/a.txt" },
			new int[] {
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE | IRemoteSyncElement.PSEUDO_CONFLICT,
				IRemoteSyncElement.IN_SYNC,
				IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.CHANGE },
			IRemoteSyncElement.GRANULARITY_CONTENTS);
	}

	public void testRenameProject() throws TeamException, CoreException, IOException {
		String[] resourceNames = new String[] { "changed.txt", "folder1/", "folder1/a.txt" };
		int[] inSync = new int[] { IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC, IRemoteSyncElement.IN_SYNC };
		IProject project = createAndPut("testRenameProject", new String[] { "changed.txt", "folder1/", "folder1/a.txt" });

		IRemoteSyncElement tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals("sync should be in sync", tree, resourceNames, inSync);
		IProjectDescription desc = project.getDescription();
		String newName = project.getName() + "_renamed";
		desc.setName(newName);
		project.move(desc, false, null);
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(newName);
		assertTrue(project.exists());
		tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals("sync should be in sync", tree, resourceNames, inSync);
	}

	public void testFolderDeletion() throws TeamException, CoreException {

		IProject project =
			createAndPut(
				"testFolderDeletion",
				new String[] { "changed.txt", "deleted.txt", "folder1/", "folder1/a.txt", "folder1/folder2/file.txt" });

		// Delete a folder and ensure that the file is managed but doesn't exist
		project.getFolder("folder1").delete(false, false, null);

		// The folders and files should show up as outgoing deletions
		IRemoteSyncElement tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testFolderDeletion sync check",
			tree,
			new String[] { "folder1", "folder1/a.txt", "folder1/folder2", "folder1/folder2/file.txt" },
			new int[] {
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.DELETION,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.DELETION,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.DELETION,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.DELETION });

		// commit folder1/a.txt
		putResourcesOntoTarget(project, new String[] { "folder1/a.txt" });

		// Resync and verify that above file is gone and others remain the same
		tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertSyncEquals(
			"testFolderDeletion sync check",
			tree,
			new String[] { "folder1", "folder1/folder2", "folder1/folder2/file.txt" },
			new int[] {
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.DELETION,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.DELETION,
				IRemoteSyncElement.OUTGOING | IRemoteSyncElement.DELETION });
		assertDeleted("testFolderDeletion", tree, new String[] { "folder1/a.txt" });

		// Commit folder1/folder2/file.txt
		putResourcesOntoTarget(project, new String[] { "folder1/", "folder1/folder2/", "folder1/folder2/file.txt" });

		// Resync and verify that all are deleted
		tree = getRemoteSyncTree(project, DEFAULT_MONITOR);
		assertDeleted("testFolderDeletion", tree, new String[] { "folder1", "folder1/folder2", "folder1/folder2/file.txt" });
	}
}
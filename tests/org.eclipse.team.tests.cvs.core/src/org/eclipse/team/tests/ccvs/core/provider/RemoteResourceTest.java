/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.core.provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import junit.framework.Test;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.CachedResourceVariant;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderTree;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderTreeBuilder;
import org.eclipse.team.internal.ccvs.ui.operations.CheckoutToRemoteFolderOperation;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

public class RemoteResourceTest extends EclipseTest {

	public RemoteResourceTest() {
		super();
	}
	
	public RemoteResourceTest(String name) {
		super(name);
	}
	
	public static Test suite() {
		return suite(RemoteResourceTest.class);
	}
	
	protected void assertRemoteMatchesLocal(String message, RemoteFolder remote, IContainer container) throws CVSException, IOException, CoreException {
		assertEquals(Path.EMPTY, (ICVSResource)remote, CVSWorkspaceRoot.getCVSFolderFor(container), false, false);
	}
	
	protected void getMembers(ICVSRemoteFolder folder, boolean deep) throws TeamException {
		ICVSRemoteResource[] children = folder.members(DEFAULT_MONITOR);
		if (deep) {
			for (int i=0;i<children.length;i++) {
				if (children[i].isContainer())
					getMembers((ICVSRemoteFolder)children[i], deep);
			}
		}
	}
	
	/**
	 * RemoteFolderTreeBuilder test
	 * 
	 * Perform some remote additions, changes and deletions 
	 */
	public void testSimpleChanges() throws TeamException, CoreException, IOException {
		
		// Create a test project and a copy of it
		IProject project = createProject("testRemoteTreeBuilder", new String[] { "changed.txt", "deleted.txt", "folder1/", "folder1/a.txt" });
		IProject copy = checkoutCopy(project, "-copy");
		
		// Make some changes to the copy and commit
		IResource[] newResources = buildResources(copy, new String[] { "added.txt", "folder2/", "folder2/added.txt" }, false);
		setContentsAndEnsureModified(copy.getFile("changed.txt"));
		addResources(newResources);
		deleteResources(new IResource[] {copy.getFile("deleted.txt")});
		commitResources(new IResource[] {copy}, IResource.DEPTH_INFINITE);
		
		// Build the remote tree from the original and ensure it matches the copy
		RemoteFolderTree tree = RemoteFolderTreeBuilder.buildRemoteTree(getRepository(), project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertRemoteMatchesLocal("testSimpleChanges", tree, copy);
	}
	
	/**
	 * RemoteFolderTreeBuilder test
	 * 
	 * Create a remote tree from a local workspace with no remote changes.
	 */
	public void testNoRemoteChanges() throws TeamException, CoreException, IOException {
		IProject project = createProject("testNoRemoteChanges", new String[] { "file1.txt", "file2.txt", "folder1/a.txt", "folder2/folder3/b.txt"});
		RemoteFolderTree tree = RemoteFolderTreeBuilder.buildRemoteTree(getRepository(), project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertRemoteMatchesLocal("testNoRemoteChanges", tree, project);
	}
	
	/**
	 * RemoteFolderTreeBuilder test
	 * 
	 * Create a base remote tree from a local workspace with no remote changes.
	 */
	public void testGetBase() throws TeamException, CoreException, IOException {
		IProject project = createProject("testGetBase", new String[] { "file1.txt", "file2.txt", "folder1/a.txt", "folder2/folder3/b.txt"});
		RemoteFolder tree = RemoteFolderTreeBuilder.buildBaseTree(getRepository(), CVSWorkspaceRoot.getCVSFolderFor(project), CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertRemoteMatchesLocal("testGetBase", tree, project);
	}
	
	/**
	 * RemoteFolderTreeBuilder test
	 * 
	 * Add a nested folder structure remotely and build the remote tree
	 * from the root.
	 */
	public void testFolderAddition() throws TeamException, CoreException, IOException {
		
		// Create a test project
		IProject project = createProject("testFolderAddition", new String[] { "file1.txt", "file2.txt", "folder1/", "folder1/a.txt"});
		
		// Checkout and modify a copy
		IProject copy = checkoutCopy(project, "-copy");
		addResources(copy, new String[] { "folder2/folder3/b.txt" }, false);
		commitResources(new IResource[] {copy}, IResource.DEPTH_INFINITE);

		// Build the remote tree from the project
		RemoteFolderTree tree = RemoteFolderTreeBuilder.buildRemoteTree(getRepository(), project, CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertRemoteMatchesLocal("testFolderAddition", tree, copy);
	}
	
	/**
	 * RemoteFolderTreeBuilder test
	 * 
	 * Add a nested folder structure remotely and build the remote tree
	 * from a child
	 */
	public void testNonRootBuild() throws CoreException, TeamException, IOException {
		
		// Create a test project
		IProject project = createProject("testNonRootBuild", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder2/", "folder2/a.txt", "folder2/folder3/", "folder2/folder3/b.txt", "folder2/folder3/c.txt"});

		// Checkout and modify a copy
		IProject copy = checkoutCopy(project, "-copy");
		setContentsAndEnsureModified(copy.getFile("folder2/folder3/c.txt"));
		addResources(copy, new String[] { "folder2/folder3/add.txt" }, false);
		deleteResources(new IResource[] {copy.getFile("folder2/folder3/b.txt")});
		commitResources(new IResource[] {copy}, IResource.DEPTH_INFINITE);

		// Build the remote tree from the project
		RemoteFolderTree tree = RemoteFolderTreeBuilder.buildRemoteTree(getRepository(), project.getFolder("folder2"), CVSTag.DEFAULT, DEFAULT_MONITOR);
		assertRemoteMatchesLocal("testNonRootBuild", tree, copy.getFolder("folder2"));
	}
	
	/**
	 * RemoteResource test
	 * 
	 */
	public void testGetRemoteResource() throws CoreException, TeamException, IOException {
		IProject project = createProject("testGetRemoteResource", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder2/", "folder2/a.txt", "folder2/folder3/", "folder2/folder3/b.txt", "folder2/folder3/c.txt"});
		ICVSRemoteResource file = CVSWorkspaceRoot.getRemoteResourceFor(project.getFile("folder1/a.txt"));
		assertTrue("File should exist remotely", file.exists(DEFAULT_MONITOR));
		assertEquals(Path.EMPTY, file, CVSWorkspaceRoot.getRemoteResourceFor(project.getFile("folder1/a.txt")), false, false);
		ICVSRemoteResource folder = CVSWorkspaceRoot.getRemoteResourceFor(project.getFolder("folder2/folder3/"));
		getMembers((ICVSRemoteFolder)folder, true);
		assertTrue("Folder should exist remotely", folder.exists(DEFAULT_MONITOR));
		// XXX this didn't work right. I'll need to check into it later
//		assertEquals("Remote folder should match local folder", (ICVSResource)folder, (ICVSResource)Client.getManagedFolder(project.getFolder("folder2/folder3/").getLocation().toFile()));
	}
	
	/*
	 * Test that the fetch of a tagged tree matches what is checked out for that tag
	 */
	public void testVersionTag() throws TeamException, CoreException, IOException {
		// Create a test project and version it
		CVSTag v1Tag = new CVSTag("v1", CVSTag.VERSION);
		IProject project = createProject("testVersionTag", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt", "folder2/folder3/c.txt"});
		tagProject(project, v1Tag, false);

		// Make some changes, additions (including folders) and deletions and commit
		IFile file = project.getFile("folder1/a.txt");
		file.setContents(getRandomContents(), false, false, null);
		addResources(project, new String[] { "folder2/folder3/add.txt" }, false);
		deleteResources(project, new String[] {"folder1/b.txt"}, false);
		commitResources(new IResource[] {project}, IResource.DEPTH_INFINITE);
		
		// Fetch the remote tree for the version
		ICVSRemoteResource tree = getRemoteTree(project, v1Tag, DEFAULT_MONITOR);

		// Check out the project version
		project = checkoutCopy(project, v1Tag);
		
		// Compare the two
		assertEquals(Path.EMPTY, tree, CVSWorkspaceRoot.getCVSResourceFor(project), false, false);
	}
	
	/*
	 * Test the fetching of the contents of an empty file
	 */
	public void testEmptyFile() throws TeamException, CoreException, IOException {
	 	
	 	// Create a project with an empty file
		IProject project = createProject("testEmptyFile", new String[] { "file.txt"});
		IFile file = project.getFile("file.txt");
		setContentsAndEnsureModified(file, "");
		commitResources(project, new String[] {"file.txt"});
		
		IResourceVariant remote = (IResourceVariant)CVSWorkspaceRoot.getRemoteResourceFor(file);
		InputStream in = remote.getStorage(DEFAULT_MONITOR).getContents();
		int count = 0;
		while(in.read() != -1) {
			count++;
		}
		assertTrue("Remote file should be empty", count==0);
	 }
	 
	 /*
	  * Test the fetching of the contents from multiple remote revisions of a file
	  */
	 public void testFileRevisions() throws TeamException, CoreException, IOException {
	 	
	 	// Create a project with an empty file
		IProject project = createProject("testFileRevisions", new String[] { "file.txt"});
		setContentsAndEnsureModified(project.getFile("file.txt"), "hi there");
		commitResources(project, new String[] {"file.txt"});
		setContentsAndEnsureModified(project.getFile("file.txt"), "bye there");
		commitResources(project, new String[] {"file.txt"});

		ICVSRemoteFile remote = (ICVSRemoteFile)CVSWorkspaceRoot.getRemoteResourceFor(project.getFile("file.txt"));
		ILogEntry[] entries = remote.getLogEntries(DEFAULT_MONITOR);
		for (int i=0;i<entries.length;i++) {
			InputStream in = entries[i].getRemoteFile().getContents(DEFAULT_MONITOR);
			
			if (entries[i].getRevision().equals("1.2")) {
				int count = 0;
				byte[] buffer = new byte[1024];
				int c;
				while((c = in.read()) != -1) {
					buffer[count] = (byte)c;
					count++;
				}
				String contents = new String(buffer, 0, count);
				assertEquals("the contents of revision 1.2 are not equal", contents, "hi there");
			} else if (entries[i].getRevision().equals("1.3")) {
				int count = 0;
				byte[] buffer = new byte[1024];
				int c;
				while((c = in.read()) != -1) {
					buffer[count] = (byte)c;
					count++;
				}
				String contents = new String(buffer, 0, count);
				assertEquals("the contents of revision 1.3 are not equal", contents, "bye there");
			}
		}
	 }
	 
	 public void testTag() throws TeamException, CoreException, IOException {
	 	IProject project = createProject("testTag", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder2/folder3/b.txt", "folder2/folder3/c.txt"});
	 	
		ICVSRemoteFolder remote = (ICVSRemoteFolder)CVSWorkspaceRoot.getRemoteResourceFor(project);
		CVSTag tag = new CVSTag("v1", CVSTag.VERSION);
		tagRemoteResource(remote, tag, false);
		ICVSRemoteFolder v1 = (ICVSRemoteFolder)getRemoteTree(project, tag, DEFAULT_MONITOR);
		assertEquals(Path.EMPTY, remote, v1, false);
		
		CVSTag tag2 = new CVSTag("v2", CVSTag.VERSION);
		tagRemoteResource(v1, tag2, false);
		ICVSRemoteFolder v2 = (ICVSRemoteFolder)getRemoteTree(project, tag2, DEFAULT_MONITOR);
		assertEquals(Path.EMPTY, remote, v2, false);
		
		// Test tag with existing
		setContentsAndEnsureModified(project.getFile("file1.txt"));
		commitProject(project);
		remote = (ICVSRemoteFolder)CVSWorkspaceRoot.getRemoteResourceFor(project);
		tagRemoteResource(remote, tag, true /* force */);
		v1 = (ICVSRemoteFolder)getRemoteTree(project, tag, DEFAULT_MONITOR);
		assertEquals(Path.EMPTY, remote, v1, false);
		
		// Test local tag with existing.
		setContentsAndEnsureModified(project.getFile("file1.txt"));
		commitProject(project);
		tagProject(project, tag2, true);
		IProject copy = checkoutCopy(project, tag2);
		assertEquals(project, copy, false, false);
	 }
	 
	 public void testExists() throws TeamException, CoreException {
	 	IProject project = createProject("testExists", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder2/", "folder2/a.txt", "folder2/folder3/", "folder2/folder3/b.txt", "folder2/folder3/c.txt"});
	 	ICVSRemoteResource resource1 = CVSWorkspaceRoot.getRemoteResourceFor(project.getFile("file1.txt"));
	 	assertTrue(resource1.exists(DEFAULT_MONITOR));
	 	ICVSRemoteResource resource2 = CVSWorkspaceRoot.getRemoteResourceFor(project.getFolder("folder2/folder3/"));
	 	assertTrue(resource2.exists(DEFAULT_MONITOR));
	 	deleteResources(project, new String[] {"file1.txt", "folder2/folder3/b.txt", "folder2/folder3/c.txt" }, true);
	 	assertTrue( ! resource1.exists(DEFAULT_MONITOR));
	 	assertTrue(resource2.exists(DEFAULT_MONITOR));
	 	if (CVSTestSetup.INITIALIZE_REPO) {
	 		CVSTestSetup.executeRemoteCommand(getRepository(), "rm -rf " + ((ICVSFolder)resource2).getFolderSyncInfo().getRemoteLocation());
	 		assertTrue( ! resource2.exists(DEFAULT_MONITOR));
	 	}
	 }
	 
//	 /**
//	  * Test building a sync tree using the RemoteFolderTreeBuilder using a remote resource as the
//	  * starting point instead of a local one.
//	  */
//	 public void testBuildRemoteTree() throws TeamException, CoreException, IOException, InterruptedException {
//	 	// Create a project and then delete it locally
//	 	IProject project = createProject("testBuildRemoteTree", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder2/", "folder2/a.txt", "folder2/folder3/", "folder2/folder3/b.txt", "folder2/folder3/c.txt"});
//	 	String name = project.getName();
//	 	project.delete(true, false, DEFAULT_MONITOR);
//	 	// Create a remote resource for the project and build a sync tree from it
//	 	RemoteFolder folder = new RemoteFolder(null, getRepository(), new Path(name), null);
//	 	RemoteFolderTree tree = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)folder.getRepository(), folder, null, DEFAULT_MONITOR);
//		// Reload the project from the repository and ensure that the tree and project are equal.
//		checkoutProject(project, name, null);
//		assertEquals(Path.EMPTY, CVSWorkspaceRoot.getCVSResourceFor(project), tree, false, true);
//	 }
	 
	 public void testCheckoutIntoRemoteFolder() throws CoreException, IOException, CVSException, InvocationTargetException, InterruptedException {
	 	IProject project = createProject(new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder2/folder3/b.txt", "folder2/folder3/c.txt"});
	 	
	 	ICVSRemoteFolder remote = (ICVSRemoteFolder)CVSWorkspaceRoot.getRemoteResourceFor(project);
	 	remote = checkoutRemote(remote);
	 	assertEquals(Path.EMPTY, CVSWorkspaceRoot.getCVSResourceFor(project), remote, false, true);
	 	
	 	// Try a version
	 	CVSTag tag = new CVSTag("v1", CVSTag.VERSION);
	 	tagProject(project, tag, false);
	 	IProject copy = checkoutCopy(project, tag);
	 	setContentsAndEnsureModified(project.getFile("file1.txt"));
	 	commitProject(project);
	 	remote = (ICVSRemoteFolder)CVSWorkspaceRoot.getRemoteResourceFor(project);
	 	((RemoteFolder)remote).setTag(tag);
	 	remote = checkoutRemote(remote);
	 	assertEquals(Path.EMPTY, CVSWorkspaceRoot.getCVSResourceFor(copy), remote, false, true);
	 }
	 
	public void testBug244425() throws CVSException, CoreException,
			IOException {
		IProject projectHead = createProject("test", new String[] { "a/",
				"a/file1.txt", "a/file2.txt" });
		appendText(projectHead.getFile("a/file1.txt"), "dummy", true);
		appendText(projectHead.getFile("a/file2.txt"), "dummy", true);
		commitNewProject(projectHead);

		// Checkout and branch a copy
		CVSTag root = new CVSTag("root_branch1", CVSTag.VERSION);
		CVSTag branch = new CVSTag("branch1", CVSTag.BRANCH);
		IProject projectBranch = checkoutCopy(projectHead, "branch");
		tagProject(projectHead, root, false);
		tagProject(projectHead, branch, false);
		updateProject(projectBranch, branch, false);

		// replace the file in original project with a branched one
		replace(new IResource[] { projectHead.getFile("a/file2.txt") }, branch,
				true);

		// modify the file on branch
		appendText(projectBranch.getFile("a/file2.txt"), "dummy2", true);
		commitProject(projectBranch);
		
		// check the file from HEAD 
		ICVSRemoteResource file1 = getRemoteTree(projectHead
				.getFile("a/file1.txt"), null, DEFAULT_MONITOR);
		assertEquals(null, file1.getSyncInfo().getTag());
		assertEquals("1.2", file1.getSyncInfo().getRevision());

		// we want to be sure that the tree notices that the file is from
		// a different branch
		ICVSRemoteResource file2 = getRemoteTree(projectHead
				.getFile("a/file2.txt"), null, DEFAULT_MONITOR);
		assertEquals(branch, file2.getSyncInfo().getTag());
		assertEquals("1.2.2.1", file2.getSyncInfo().getRevision());
	}
	
	public void testBug244425_compare() throws CVSException, CoreException,
	IOException {
		IProject projectHead = createProject("test", new String[] { "a/",
				"a/file1.txt", "a/file2.txt" });
		appendText(projectHead.getFile("a/file1.txt"), "dummy", true);
		appendText(projectHead.getFile("a/file2.txt"), "dummy", true);
		// tag files
		CVSTag tag = new CVSTag("testtag", CVSTag.VERSION);
		tagProject(projectHead, tag, false);
		
		// modify files
		appendText(projectHead.getFile("a/file2.txt"), "dummy2", true);
		commitProject(projectHead);
		
		// revert to tagged version
		replace(new IResource[] { projectHead.getFile("a/file2.txt") }, tag,
				true);
		assertEquals("1.1", CVSWorkspaceRoot.getCVSFileFor(
				projectHead.getFile("a/file2.txt")).getSyncInfo().getRevision());

		// compare with head
		// this is the crucial part of this test. It is necessary that
		// getRemoteTree ignores existing tag when comparing with f.e. HEAD.
		ICVSRemoteResource remoteFile = getRemoteTree(projectHead
				.getFile("a/file2.txt"), new CVSTag() /* HEAD */, DEFAULT_MONITOR);
		assertEquals("1.2", remoteFile.getSyncInfo().getRevision());
	}

	private ICVSRemoteFolder checkoutRemote(ICVSRemoteFolder remote) throws CVSException, InvocationTargetException, InterruptedException {
		return CheckoutToRemoteFolderOperation.checkoutRemoteFolder(null, remote, DEFAULT_MONITOR);
	}
	
	public void testContentFetchForLocalDeletion() throws TeamException, IOException, CoreException {
		IProject project = createProject(new String[] { "file1.txt"});
		String contents = "the file contents";
		setContentsAndEnsureModified(project.getFile("file1.txt"), contents);
		commitProject(project);
		project.getFile("file1.txt").delete(false, null);
		IResourceVariant remote = (IResourceVariant)CVSWorkspaceRoot.getRemoteResourceFor(project.getFile("file1.txt"));
		String fetchedContents = asString(remote.getStorage(DEFAULT_MONITOR).getContents());
		assertEquals("Contents do not match", contents, fetchedContents);
	}

	private String asString(InputStream stream) throws IOException {
		StringBuffer buffer = new StringBuffer();
		int b = stream.read();
		while (b != -1) {
			buffer.append((char)b);
			b = stream.read();
		}
		return buffer.toString();
	}
	
	public void testResourceVariant() throws TeamException, CoreException {
		IProject project = createProject(new String[] { "file1.txt"});
		IFile file = project.getFile("file1.txt");
		ICVSRemoteResource resource = CVSWorkspaceRoot.getRemoteResourceFor(file);
		IResourceVariant variant = (IResourceVariant)resource;
		IStorage storage = variant.getStorage(DEFAULT_MONITOR);
		assertEquals(storage.getFullPath(), ((CachedResourceVariant)resource).getDisplayPath());
	}
	
	public String getCachePath(ICVSRemoteResource resource) throws CVSException {
		ICVSRepositoryLocation location = resource.getRepository();
		IPath path = new Path(location.getHost());
		path = path.append(location.getRootDirectory());
		path = path.append(resource.getParent().getRepositoryRelativePath());
		path = path.append(resource.getName() + ' ' + ((IResourceVariant)resource).getContentIdentifier());
		return path.toString();
	}
}


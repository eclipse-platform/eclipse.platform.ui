package org.eclipse.team.tests.ccvs.core.provider;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.ccvs.core.ILogEntry;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderTree;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderTreeBuilder;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.EclipseTest;
import org.eclipse.team.tests.ccvs.core.JUnitTestCase;

public class RemoteResourceTest extends EclipseTest {

	public RemoteResourceTest() {
		super();
	}
	
	public RemoteResourceTest(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(RemoteResourceTest.class);
		return new CVSTestSetup(suite);
		//return new CVSTestSetup(new RemoteResourceTest("testFileRevisions"));
	}
	
	protected void assertRemoteMatchesLocal(String message, RemoteFolder remote, IContainer container) throws CVSException, IOException, CoreException {
		assertEquals(Path.EMPTY, (ICVSResource)remote, CVSWorkspaceRoot.getCVSFolderFor(container), false, false);
	}
	
	protected void getMembers(ICVSRemoteFolder folder, boolean deep) throws TeamException {
		IRemoteResource[] children = folder.members(DEFAULT_MONITOR);
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
		IFile file = copy.getFile("changed.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		CVSTeamProvider provider = getProvider(copy);
		provider.add(newResources, IResource.DEPTH_ZERO, DEFAULT_MONITOR);
		provider.delete(new IResource[] {copy.getFile("deleted.txt")}, DEFAULT_MONITOR);
		provider.checkin(new IResource[] {copy}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);
		
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
		RemoteFolderTree tree = RemoteFolderTreeBuilder.buildBaseTree(getRepository(), CVSWorkspaceRoot.getCVSFolderFor(project), CVSTag.DEFAULT, DEFAULT_MONITOR);
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
		getProvider(copy).checkin(new IResource[] {copy}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);

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
		IFile file = copy.getFile("folder2/folder3/c.txt");
		JUnitTestCase.waitMsec(1500); // Wait so that timestamp of modified file differs from original
		file.setContents(getRandomContents(), false, false, null);
		addResources(copy, new String[] { "folder2/folder3/add.txt" }, false);
		getProvider(copy).delete(new IResource[] {copy.getFile("folder2/folder3/b.txt")}, DEFAULT_MONITOR);
		getProvider(copy).checkin(new IResource[] {copy}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);

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
		assertTrue("File should exist remotely", file.exists());
		assertEquals(Path.EMPTY, (ICVSResource)file, (ICVSResource)CVSWorkspaceRoot.getRemoteResourceFor(project.getFile("folder1/a.txt")), false, false);
		ICVSRemoteResource folder = CVSWorkspaceRoot.getRemoteResourceFor(project.getFolder("folder2/folder3/"));
		getMembers((ICVSRemoteFolder)folder, true);
		assertTrue("Folder should exist remotely", folder.exists());
		// XXX this didn't work right. I'll need to check into it later
//		assertEquals("Remote folder should match local folder", (ICVSResource)folder, (ICVSResource)Client.getManagedFolder(project.getFolder("folder2/folder3/").getLocation().toFile()));
	}
	
	/*
	 * Test that the fetch of a tagged tree matches what is checked out for that tag
	 */
	public void testVersionTag() throws TeamException, CoreException, IOException {
		// Create a test project and version it
		CVSTag v1Tag = new CVSTag("v1", CVSTag.VERSION);
		IProject project = createProject("testVersionTag", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
		tagProject(project, v1Tag);

		// Make some changes, additions (including folders) and deletions and commit
		IFile file = project.getFile("folder1/a.txt");
		file.setContents(getRandomContents(), false, false, null);
		addResources(project, new String[] { "folder2/folder3/add.txt" }, false);
		deleteResources(project, new String[] {"folder1/b.txt"}, false);
		getProvider(project).checkin(new IResource[] {project}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);
		
		// Fetch the remote tree for the version
		IRemoteSyncElement tree = CVSWorkspaceRoot.getRemoteSyncTree(project, v1Tag, DEFAULT_MONITOR);

		// Check out the project version
		project = checkoutCopy(project, v1Tag);
		
		// Compare the two
		assertEquals(Path.EMPTY, (ICVSResource)tree.getRemote(), (ICVSResource)CVSWorkspaceRoot.getCVSResourceFor(project), false, false);
	}
	
	/*
	 * Test the fetching of the contents of an empty file
	 */
	public void testEmptyFile() throws TeamException, CoreException, IOException {
	 	
	 	// Create a project with an empty file
		IProject project = createProject("testEmptyFile", new String[] { "file.txt"});
		IFile file = project.getFile("file.txt");
		JUnitTestCase.waitMsec(1500);
		file.setContents(new ByteArrayInputStream(new byte[0]), false, false, DEFAULT_MONITOR);
		commitResources(project, new String[] {"file.txt"});
		
		ICVSRemoteResource remote = CVSWorkspaceRoot.getRemoteResourceFor(file);
		InputStream in = remote.getContents(DEFAULT_MONITOR);
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
		IFile file = project.getFile("file.txt");
		JUnitTestCase.waitMsec(1500);
		file.setContents(new ByteArrayInputStream("hi there".getBytes()), false, false, DEFAULT_MONITOR);
		commitResources(project, new String[] {"file.txt"});
		JUnitTestCase.waitMsec(1500);
		file.setContents(new ByteArrayInputStream("bye there".getBytes()), false, false, DEFAULT_MONITOR);
		commitResources(project, new String[] {"file.txt"});

		ICVSRemoteFile remote = (ICVSRemoteFile)CVSWorkspaceRoot.getRemoteResourceFor(file);
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
	 	IProject project = createProject("testTag", new String[] { "file1.txt", "folder1/", "folder1/a.txt", "folder2/", "folder2/a.txt", "folder2/folder3/", "folder2/folder3/b.txt", "folder2/folder3/c.txt"});
		ICVSRemoteFolder remote = (ICVSRemoteFolder)CVSWorkspaceRoot.getRemoteResourceFor(project);
		CVSTag tag = new CVSTag("v1", CVSTag.VERSION);
		remote.tag(tag, Command.NO_LOCAL_OPTIONS, DEFAULT_MONITOR);
		ICVSRemoteFolder v1 = (ICVSRemoteFolder)CVSWorkspaceRoot.getRemoteTree(project, tag, DEFAULT_MONITOR);
		assertEquals(Path.EMPTY, remote, v1, false);
		CVSTag tag2 = new CVSTag("v2", CVSTag.VERSION);
		v1.tag(tag2, Command.NO_LOCAL_OPTIONS, DEFAULT_MONITOR);
		ICVSRemoteFolder v2 = (ICVSRemoteFolder)CVSWorkspaceRoot.getRemoteTree(project, tag2, DEFAULT_MONITOR);
		assertEquals(Path.EMPTY, remote, v2, false);
	 }
}


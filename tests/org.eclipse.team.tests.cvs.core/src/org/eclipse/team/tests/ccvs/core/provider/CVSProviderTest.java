package org.eclipse.team.tests.ccvs.core.provider;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.EclipseTest;
import org.eclipse.team.tests.ccvs.core.JUnitTestCase;

/*
 * This class tests both the CVSProvider and the CVSTeamProvider
 */
public class CVSProviderTest extends EclipseTest {

	/**
	 * Constructor for CVSProviderTest
	 */
	public CVSProviderTest() {
		super();
	}

	/**
	 * Constructor for CVSProviderTest
	 */
	public CVSProviderTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CVSProviderTest.class);
		return new CVSTestSetup(suite);
		//return new CVSTestSetup(new CVSProviderTest("testVersionTag"));
	}
	
	public void testAdd() throws TeamException, CoreException {
		
		// Test add with cvsignores
		IProject project = createProject("testAdd", new String[] { "changed.txt", "deleted.txt", "folder1/", "folder1/a.txt" });
		IFile file = project.getFile(".cvsignore");
		file.create(new ByteArrayInputStream("ignored.txt".getBytes()), false, null);
		file = project.getFile("ignored.txt");
		file.create(new ByteArrayInputStream("some text".getBytes()), false, null);
		file = project.getFile("notignored.txt");
		file.create(new ByteArrayInputStream("some more text".getBytes()), false, null);
		file = project.getFile("folder1/.cvsignore");
		file.create(new ByteArrayInputStream("ignored.txt".getBytes()), false, null);
		file = project.getFile("folder1/ignored.txt");
		file.create(new ByteArrayInputStream("some text".getBytes()), false, null);
		file = project.getFile("folder1/notignored.txt");
		file.create(new ByteArrayInputStream("some more text".getBytes()), false, null);
		
		getProvider(project).add(new IResource[] {project}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);
		
		assertTrue( ! Session.getManagedResource(project.getFile("ignored.txt")).isManaged());
		assertTrue( ! Session.getManagedResource(project.getFile("folder1/ignored.txt")).isManaged());
		
		assertTrue(Session.getManagedResource(project.getFile("notignored.txt")).isManaged());
		assertTrue(Session.getManagedResource(project.getFile("folder1/notignored.txt")).isManaged());
		assertTrue(Session.getManagedResource(project.getFile(".cvsignore")).isManaged());
		assertTrue(Session.getManagedResource(project.getFile("folder1/.cvsignore")).isManaged());
	}
	
	public void testDelete() throws TeamException, CoreException {
		// Not supported yet
	}
	
	public void testCheckin() throws TeamException, CoreException, IOException {
		IProject project = createProject("testCheckin", new String[] { "changed.txt", "deleted.txt", "folder1/", "folder1/a.txt" });
		
		// Perform some operations on the project
		IResource[] newResources = buildResources(project, new String[] { "added.txt", "folder2/", "folder2/added.txt" }, false);
		IFile file = project.getFile("changed.txt");
		JUnitTestCase.waitMsec(1500);
		file.setContents(getRandomContents(), false, false, null);
		getProvider(project).add(newResources, IResource.DEPTH_ZERO, DEFAULT_MONITOR);
		getProvider(project).delete(new IResource[] {project.getFile("deleted.txt")}, DEFAULT_MONITOR);
		assertIsModified("testDeepCheckin: ", newResources);
		assertIsModified("testDeepCheckin: ", new IResource[] {project.getFile("deleted.txt"), project.getFile("changed.txt")});
		getProvider(project).checkin(new IResource[] {project}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);
		assertLocalStateEqualsRemote(project);
	}
	
	public void testMoved() throws TeamException, CoreException {
		// Not supported yet
	}
	
	public void testUpdate() throws TeamException, CoreException, IOException {
		// Create a test project, import it into cvs and check it out
		IProject project = createProject("testUpdate", new String[] { "changed.txt", "deleted.txt", "folder1/", "folder1/a.txt" });

		// Check the project out under a different name
		IProject copy = checkoutCopy(project, "-copy");
		
		// Perform some operations on the copy
		addResources(copy, new String[] { "added.txt", "folder2/", "folder2/added.txt" }, false);
		IFile file = copy.getFile("changed.txt");
		JUnitTestCase.waitMsec(1500);
		file.setContents(getRandomContents(), false, false, null);
		getProvider(copy).delete(new IResource[] {copy.getFile("deleted.txt")}, DEFAULT_MONITOR);
		
		// Commit the copy and update the project
		getProvider(copy).checkin(new IResource[] {copy}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);
		getProvider(project).update(new IResource[] {project}, Command.NO_LOCAL_OPTIONS, null, null, DEFAULT_MONITOR);
		assertEquals(project, copy);
	}
	
	public void testVersionTag() throws TeamException, CoreException, IOException {
		
		// Create a test project, import it into cvs and check it out
		IProject project = createProject("testTag", new String[] { "changed.txt", "deleted.txt", "folder1/", "folder1/a.txt" });
		
		// Perform some operations on the copy and commit
		IProject copy = checkoutCopy(project, "-copy");
		addResources(copy, new String[] { "added.txt", "folder2/", "folder2/added.txt" }, false);
		changeResources(copy, new String[] {"changed.txt"}, false);
		deleteResources(copy, new String[] {"deleted.txt"}, false);
		checkinResources(copy, true);
		
		// Tag the original, checkout the tag and compare with original
		CVSTag v1Tag = new CVSTag("v1", CVSTag.VERSION);
		getProvider(project).tag(new IResource[] {project}, IResource.DEPTH_INFINITE, v1Tag, DEFAULT_MONITOR);
		IProject v1 = checkoutCopy(project, v1Tag);
		assertEquals(project, v1);
		
		// Update original to HEAD and compare with copy including tags
		updateProject(project, null, false);
		assertEquals(project, copy, false, true);
		
		// Update copy to v1 and compare with the copy (including tag)
		updateProject(copy, v1Tag, false);
		assertEquals(copy, v1, false, true);
		
		// Update copy back to HEAD and compare with project (including tag)
		updateProject(copy, CVSTag.DEFAULT, false);
		assertEquals(project, copy, false, true);
	}
	
	public void testBranchTag() throws TeamException, CoreException, IOException {
	}
	 
	public void testPruning() throws TeamException, CoreException, IOException {
		// Create a project with empty folders
		CVSProviderPlugin.getPlugin().setPruneEmptyDirectories(false);
		IProject project = createProject("testPruning", new String[] { "file.txt", "folder1/", "folder2/folder3/" });

		// Disable pruning, checkout a copy and ensure original and copy are the same
		IProject copy = checkoutCopy(project, "-copy");
		assertEquals(project, copy); 

		// Enable pruning, update copy and ensure emtpy folders are gone
		CVSProviderPlugin.getPlugin().setPruneEmptyDirectories(true);
		updateProject(copy, null, false);
		assertDoesNotExistInFileSystem(new IResource[] {copy.getFolder("folder1"), copy.getFolder("folder2"), copy.getFolder("folder2/folder3")});
		
		// Checkout another copy and ensure that the two copies are the same (with pruning enabled)
		IProject copy2 = checkoutCopy(project, "-copy2");
		assertEquals(copy, copy2); 
		
		// Disable pruning, update copy and ensure directories come back
		CVSProviderPlugin.getPlugin().setPruneEmptyDirectories(false);
		updateProject(copy, null, false);
		assertEquals(project, copy);
		
		// Enable pruning again since it's the default
		CVSProviderPlugin.getPlugin().setPruneEmptyDirectories(true);
	}

	public void testGet() throws TeamException, CoreException, IOException {
		
		// Create a project
		IProject project = createProject("testGet", new String[] { "changed.txt", "deleted.txt", "folder1/", "folder1/a.txt" });
		
		// Checkout a copy and modify locally
		IProject copy = checkoutCopy(project, "-copy");
		addResources(copy, new String[] { "added.txt", "folder2/", "folder2/added.txt" }, false);
		deleteResources(copy, new String[] {"deleted.txt"}, false);
		IFile file = copy.getFile("changed.txt");
		JUnitTestCase.waitMsec(1500);
		file.setContents(getRandomContents(), false, false, null);

		// get the remote conetns
		getProvider(copy).get(new IResource[] {copy}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);
		assertEquals(project, copy);
	}

}


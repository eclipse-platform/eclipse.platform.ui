package org.eclipse.team.tests.ccvs.core.provider;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.core.IFileTypeRegistry;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
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
		/*
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
		
		assertTrue( ! CVSWorkspaceRoot.getCVSResourceFor(project.getFile("ignored.txt")).isManaged());
		assertTrue( ! CVSWorkspaceRoot.getCVSResourceFor(project.getFile("folder1/ignored.txt")).isManaged());
		
		assertTrue(CVSWorkspaceRoot.getCVSResourceFor(project.getFile("notignored.txt")).isManaged());
		assertTrue(CVSWorkspaceRoot.getCVSResourceFor(project.getFile("folder1/notignored.txt")).isManaged());
		assertTrue(CVSWorkspaceRoot.getCVSResourceFor(project.getFile(".cvsignore")).isManaged());
		assertTrue(CVSWorkspaceRoot.getCVSResourceFor(project.getFile("folder1/.cvsignore")).isManaged());
		*/
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
		tagProject(project, v1Tag);
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
	
	public void testMakeBranch() throws TeamException, CoreException, IOException {
		// Create a test project
		IProject project = createProject("testSyncOnBranch", new String[] { "file1.txt", "file2.txt", "file3.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});

		// Make some local modifications including "cvs adds" and "cvs removes"
		addResources(project, new String[] {"folder1/c.txt"}, false);
		deleteResources(project, new String[] {"folder1/b.txt"}, false);
		changeResources(project, new String[] {"file2.txt"}, false);
		
		// Make the branch including a pre-version
		CVSTag version = new CVSTag("v1", CVSTag.BRANCH);
		CVSTag branch = new CVSTag("branch1", CVSTag.BRANCH);
		getProvider(project).makeBranch(new IResource[] {project}, version, branch, true, true, DEFAULT_MONITOR);

		// Checkout a copy from the branch and version and compare
		IProject branchCopy = checkoutCopy(project, branch);
		IProject versionCopy = checkoutCopy(project, branch);
		assertEquals(branchCopy, versionCopy, true, false);
		
		// Commit the project, update the branch and compare
		commitProject(project);
		updateProject(branchCopy, null, false);
		assertEquals(branchCopy, project, false, true);
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
	
	public void testCleanLineDelimiters() throws TeamException, CoreException, IOException {
		// Create a project
		IProject project = getUniqueTestProject("testCleanLineDelimiters");
		IFile file = project.getFile("testfile");
		IProgressMonitor monitor = new NullProgressMonitor();

		// empty file
		setFileContents(file, "");
		CVSTeamProvider.cleanLineDelimiters(file, false, monitor);
		assertEqualsFileContents(file, "");
		CVSTeamProvider.cleanLineDelimiters(file, true, monitor);
		assertEqualsFileContents(file, "");
		
		// one byte
		setFileContents(file, "a");
		CVSTeamProvider.cleanLineDelimiters(file, false, monitor);
		assertEqualsFileContents(file, "a");
		CVSTeamProvider.cleanLineDelimiters(file, true, monitor);
		assertEqualsFileContents(file, "a");
		
		// single orphan carriage return (should be preserved)
		setFileContents(file, "\r");
		CVSTeamProvider.cleanLineDelimiters(file, false, monitor);
		assertEqualsFileContents(file, "\r");
		CVSTeamProvider.cleanLineDelimiters(file, true, monitor);
		assertEqualsFileContents(file, "\r");

		// single line feed
		setFileContents(file, "\n");
		CVSTeamProvider.cleanLineDelimiters(file, false, monitor);
		assertEqualsFileContents(file, "\n");
		CVSTeamProvider.cleanLineDelimiters(file, true, monitor);
		assertEqualsFileContents(file, "\r\n");
		
		// single carriage return line feed
		setFileContents(file, "\r\n");
		CVSTeamProvider.cleanLineDelimiters(file, true, monitor);
		assertEqualsFileContents(file, "\r\n");
		CVSTeamProvider.cleanLineDelimiters(file, false, monitor);
		assertEqualsFileContents(file, "\n");
		
		// mixed text with orphaned CR's
		setFileContents(file, "The \r\n quick brown \n fox \r\r\r\n jumped \n\n over \r\n the \n lazy dog.\r\n");
		CVSTeamProvider.cleanLineDelimiters(file, false, monitor);
		assertEqualsFileContents(file, "The \n quick brown \n fox \r\r\n jumped \n\n over \n the \n lazy dog.\n");
		setFileContents(file, "The \r\n quick brown \n fox \r\r\r\n jumped \n\n over \r\n the \n lazy dog.\r\n");
		CVSTeamProvider.cleanLineDelimiters(file, true, monitor);
		assertEqualsFileContents(file, "The \r\n quick brown \r\n fox \r\r\r\n jumped \r\n\r\n over \r\n the \r\n lazy dog.\r\n");
	}
	
	public void testKeywordSubstitution() throws TeamException, CoreException, IOException {
		testKeywordSubstitution(Command.KSUBST_BINARY); // -kb
		testKeywordSubstitution(Command.KSUBST_TEXT); // -ko
		testKeywordSubstitution(Command.KSUBST_TEXT_EXPAND); // -kkv
	}

	private void testKeywordSubstitution(KSubstOption ksubst) throws TeamException, CoreException, IOException {
		// setup some known file types
		TeamPlugin.getFileTypeRegistry().setValue("xbin", IFileTypeRegistry.BINARY);
		TeamPlugin.getFileTypeRegistry().setValue("xtxt", IFileTypeRegistry.TEXT);
		
		// create a test project
		IProject project = createProject("testKeywordSubstitution", new String[] { "dummy" });
		addResources(project, new String[] { "binary.xbin", "text.xtxt", "folder1/", "folder1/a.xtxt" }, true);
		addResources(project, new String[] { "added.xbin", "added.xtxt" }, false);
		assertHasKSubstOption(project, "binary.xbin", Command.KSUBST_BINARY);
		assertHasKSubstOption(project, "added.xbin", Command.KSUBST_BINARY);
		// XXX should these be KSUBST_TEXT instead?
		assertHasKSubstOption(project, "text.xtxt", Command.KSUBST_TEXT_EXPAND);
		assertHasKSubstOption(project, "folder1/a.xtxt", Command.KSUBST_TEXT_EXPAND);
		assertHasKSubstOption(project, "added.xtxt", Command.KSUBST_TEXT_EXPAND);
		
		// change keyword substitution
		Map map = new HashMap();
		map.put(project.getFile("binary.xbin"), ksubst);
		map.put(project.getFile("added.xbin"), ksubst);
		map.put(project.getFile("text.xtxt"), ksubst);
		map.put(project.getFile("folder1/a.xtxt"), ksubst);
		map.put(project.getFile("added.xtxt"), ksubst);
		
		IStatus status = getProvider(project).setKeywordSubstitution(map, null);
		assertTrue("Status should be ok, was: " + status.toString(), status.isOK());
		assertHasKSubstOption(project, "binary.xbin", ksubst);
		assertHasKSubstOption(project, "text.xtxt", ksubst);
		assertHasKSubstOption(project, "folder1/a.xtxt", ksubst);
		assertHasKSubstOption(project, "added.xtxt", ksubst);
		assertHasKSubstOption(project, "added.xbin", ksubst);

		// verify that substitution mode changed remotely and "added.xtxt", "added.xbin" don't exist
		IProject copy = checkoutCopy(project, "-copy");
		assertHasKSubstOption(copy, "binary.xbin", ksubst);
		assertHasKSubstOption(copy, "text.xtxt", ksubst);
		assertHasKSubstOption(copy, "folder1/a.xtxt", ksubst);
		assertDoesNotExistInWorkspace(copy.getFile("added.xtxt"));
		assertDoesNotExistInWorkspace(copy.getFile("added.xbin"));
		
		// commit added files then checkout the copy again
		commitResources(project, new String[] { "added.xbin", "added.xtxt" });
		IProject copy2 = checkoutCopy(project, "-copy2");
		assertHasKSubstOption(copy2, "added.xtxt", ksubst);
		assertHasKSubstOption(copy2, "added.xbin", ksubst);
		
		// verify that local contents are up to date
		assertEquals(project, copy2);
	}
	
	public static void setFileContents(IFile file, String string) throws CoreException {
		InputStream is = new ByteArrayInputStream(string.getBytes());
		if (file.exists()) {
			file.setContents(is, false /*force*/, true /*keepHistory*/, null);
		} else {
			file.create(is, false /*force*/, null);
		}
	}
	public static String getFileContents(IFile file) throws CoreException, IOException {
		StringBuffer buf = new StringBuffer();
		InputStream is = new BufferedInputStream(file.getContents());
		try {
			int c;
			while ((c = is.read()) != -1) buf.append((char)c);
		} finally {
			is.close();
		}
		return buf.toString();
	}
	
	public static void assertEqualsFileContents(IFile file, String string) throws CoreException, IOException {
		String other = getFileContents(file);
		assertEquals(string, other);
	}
	
	public static void assertHasKSubstOption(IContainer container, String filename, KSubstOption ksubst)
		throws TeamException {
		IFile file = container.getFile(new Path(filename));
		ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(file);
		ResourceSyncInfo info = cvsFile.getSyncInfo();
		KSubstOption other = KSubstOption.fromMode(info.getKeywordMode());
		assertEquals(ksubst, other);
	}
}


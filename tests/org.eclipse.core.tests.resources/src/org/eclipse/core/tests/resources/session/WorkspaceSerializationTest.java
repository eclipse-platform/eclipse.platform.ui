package org.eclipse.core.tests.resources.session;

import java.io.ByteArrayInputStream;
import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.eclipse.core.tests.harness.FussyProgressMonitor;
import org.eclipse.core.tests.harness.*;
import org.eclipse.core.tests.internal.builders.SortBuilder;
import org.eclipse.core.tests.internal.resources.*;

public class WorkspaceSerializationTest extends WorkspaceSessionTest {
	protected IWorkspace workspace;
	protected IPath location;
	protected IProgressMonitor monitor = new FussyProgressMonitor();
	
	protected static final String PROJECT = "CrashProject";
	protected static final String FOLDER = "CrashFolder";
	protected static final String FILE = "CrashFile";

/**
 * Creates a new WorkspaceSerializationTest.
 */
public WorkspaceSerializationTest() {
	super("");
}
/**
 * Creates a new WorkspaceSerializationTest.
 * @param name the name of the test method to run
 */
public WorkspaceSerializationTest(String name) {
	super(name);
}
/**
 * Crashes the workspace, nulling out all references to it,
 * and then reopens the workspace on the same local content
 */
protected void crash() throws CoreException {
	//this is a bit of a hack.  It basically creates a new workspace instance.
	//hopefully this is sufficient for testing purposes because it's difficult to
	//shutdown and restart the entire platform in the middle of a test.
	ResourcesPlugin.getPlugin().startup();
	gc();
}
/**
 * Silently attempts to delete the given file and its children.
 */
protected static void deleteSilently(java.io.File file) {
	if (!file.exists()) {
		return;
	}
	if (file.isDirectory()) {
		java.io.File[] files = file.listFiles();
		//file.list() can return null
		if (files != null) {
			for (int i = 0; i < files.length; ++i) {
				deleteSilently(files[i]);
			}
		}
	}
	file.delete();
}
/**
 * Sets up the fixture, for example, open a network connection.
 * This method is called before a test is executed.
 */
protected void setUp() throws Exception {
	super.setUp();
	workspace = ResourcesPlugin.getWorkspace();
}
protected void setUpMonitor() {
	if (monitor == null) {
		monitor = new FussyProgressMonitor();
	} else {
		if (monitor instanceof FussyProgressMonitor) {
			((FussyProgressMonitor)monitor).prepare();
		}
	}
}
public static Test suite() {
	TestSuite suite = new TestSuite(WorkspaceSerializationTest.class);
//	suite.addTest(new WorkspaceSerializationTest("testSaveCreateProjectCloseOpen"));
	
	return suite;
}
/**
 * Tests closing a workspace without save.
 */
public void testClose() throws CoreException {
	/* create some resource handles */
	IProject project = workspace.getRoot().getProject(PROJECT);
	project.create(getMonitor());
	project.open(getMonitor());
	IFolder folder = project.getFolder(FOLDER);
	folder.create(true, true, getMonitor());
	IFile file = folder.getFile(FILE);
	file.create(getRandomContents(), true, getMonitor());

	/* simulate a crash and recover */
	project = null;
	folder = null;
	file = null;
	crash();

	/* workspace should be empty */
	IResource[] members = workspace.getRoot().members();
	assertEquals("1.0", 1, members.length);
	assertTrue("1.1", members[0].getType() == IResource.PROJECT);
	project  = (IProject)members[0];
}
///**
// * Tests snapshot on a closed workspace.
// */
//public void testCloseSnap() throws CoreException {
//	/* create some resource handles */
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//	IProject project = solution.getProject(PROJECT);
//	project.create(getMonitor());
//	project.open(getMonitor());
//
//	/* close the workspace */
//	workspace.close(getMonitor());
//
//	/* snapshot -- should do nothing */
//	setUpMonitor();
//	workspace.snapshot(monitor);
//
//	/* simulate a crash and recover */
//	solution = null;
//	project = null;
//	crash();
//
//	/* see if the workspace contains the solution and project */
//	Enumeration solutions = workspace.solutions();
//	assert("solution exists", !solutions.hasMoreElements());
//}
///**
// * Tests recovery after adding solution and not saving
// */
//public void testCreateSolutionCloseOpen() throws CoreException {
//	/* create some resource handles */
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//
//	/* simulate a crash and recover */
//	solution = null;
//	crash();
//
//	/* see if the workspace contains the open solution */
//	Enumeration solutions = workspace.solutions();
//	assert("solution exists", solutions.hasMoreElements());
//	solution = (ISolution)solutions.nextElement();
//	assert("solution is open", solution.isOpen());
//}
///**
// * Tests performing multiple snapshots on a workspace that has
// * never been saved, then crashing and recovering.
// */
//public void testMultiSnap() throws CoreException {
//	/* create some resource handles */
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//	
//	IProject project = solution.getProject(PROJECT);
//	project.create(getMonitor());
//	project.open(getMonitor());
//
//	/* snapshot */
//	setUpMonitor();
//	workspace.snapshot(monitor);
//
//	/* do more stuff */
//	IFolder folder = project.getFolder(FOLDER);
//	folder.create(true, getMonitor());
//
//	setUpMonitor();
//	workspace.snapshot(monitor);
//
//	/* do even more stuff */
//	IFile file = folder.getFile(FILE);
//	byte[] bytes = "Test bytes".getBytes();
//	java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(bytes);
//	file.create(in, true, getMonitor());
//
//	setUpMonitor();
//	workspace.snapshot(monitor);
//	
//	/* simulate a crash and recover */
//	solution = null;
//	project = null;
//	folder = null;
//	file = null;
//	crash();
//
//	solution = workspace.getSolution(SOLUTION);
//	project = solution.getProject(PROJECT);
//	folder = project.getFolder(FOLDER);
//	file = folder.getFile(FILE);
//
//	/* see if the workspace contains the solution and project */
//	Enumeration solutions = workspace.solutions();
//	assert("solution exists", solutions.hasMoreElements());
//
//	ISolution solution2 = (ISolution)solutions.nextElement();
//	assertEquals("Solution is the same", solution, solution2);
//
//	Enumeration projects = solution2.members();
//	assert("project exists", projects.hasMoreElements());
//
//	IProject project2 = (IProject)projects.nextElement();
//	assertEquals("Project is the same", project, project2);
//
//	Enumeration folders = project2.members();
//	assert("folder exists", folders.hasMoreElements());
//
//	IFolder folder2 = (IFolder)folders.nextElement();
//	assertEquals("Project is the same", folder, folder2);
//
//	Enumeration files = folder2.members();
//	assert("file exists", files.hasMoreElements());
//
//	IFile file2 = (IFile)files.nextElement();
//	assertEquals("File is the same", file, file2);
//}
///**
// * Tests snapshot on a closed workspace.
// */
//public void testPR() throws CoreException {
//	/* create some resource handles */
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//	solution.close(getMonitor());
//
//	/* save and close the workspace */
//	setUpMonitor();
//	workspace.save(monitor);
//	workspace.close(getMonitor());
//
//	/* simulate a crash and recover */
//	solution = null;
//
//	crash();
//
//	setUpMonitor();
//	workspace.save(monitor);
//	workspace.close(getMonitor());
//	workspace.open(getMonitor());
//}
///**
// * Tests performing a save on a workspace, then crashing and recovering.
// */
//public void testSave() throws CoreException {
//	/* create some resource handles */
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//	IProject project = solution.getProject(PROJECT);
//	project.create(getMonitor());
//	project.open(getMonitor());
//
//	setUpMonitor();
//	workspace.save(monitor);
//
//	/* simulate a crash and recover */
//	solution = null;
//	project = null;
//	crash();
//
//	solution = workspace.getSolution(SOLUTION);
//	project = solution.getProject(PROJECT);
//
//	/* see if the workspace contains the solution and project */
//	Enumeration solutions = workspace.solutions();
//	assert("solution exists", solutions.hasMoreElements());
//	ISolution solution2 = (ISolution) solutions.nextElement();
//	assertEquals("Solution is the same", solution, solution2);
//	assert("Solution is open", solution2.isOpen());
//
//	Enumeration projects = solution.members();
//	assert("project exists", projects.hasMoreElements());
//	IProject project2 = (IProject) projects.nextElement();
//	assertEquals("Project is the same", project, project2);
//	assert("Project is open", project2.isOpen());
//}
///**
// * Tests snapshot on a closed workspace.
// */
//public void testSaveClose() throws CoreException {
//	/* create some resource handles */
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//	IProject project = solution.getProject(PROJECT);
//	project.create(getMonitor());
//	project.open(getMonitor());
//	IFolder folder = project.getFolder(FOLDER);
//	folder.create(true, getMonitor());
//	IFile file = folder.getFile(FILE);
//	file.create(getRandomContents(), true, getMonitor());
//
//	/* save and close the workspace */
//	setUpMonitor();
//	workspace.save(monitor);
//	workspace.close(getMonitor());
//
//	/* simulate a crash and recover */
//	solution = null;
//	project = null;
//	folder = null;
//	file = null;
//
//	crash();
//
//	solution = workspace.getSolution(SOLUTION);
//	project = solution.getProject(PROJECT);
//	folder = project.getFolder(FOLDER);
//	file = folder.getFile(FILE);
//
//	/* see if the workspace contains the solution and project */
//	Enumeration solutions = workspace.solutions();
//	assert("solution exists", solutions.hasMoreElements());
//	ISolution solution2 = (ISolution) solutions.nextElement();
//	assertEquals("Solution is the same", solution, solution2);
//	assert("Solution is open", solution2.isOpen());
//
//	Enumeration projects = solution.members();
//	assert("project exists", projects.hasMoreElements());
//	IProject project2 = (IProject) projects.nextElement();
//	assertEquals("Project is the same", project, project2);
//	assert("Project is open", project2.isOpen());
//
//	Enumeration folders = project2.members();
//	assert("folder exists", folders.hasMoreElements());
//
//	IFolder folder2 = (IFolder)folders.nextElement();
//	assertEquals("Project is the same", folder, folder2);
//
//	Enumeration files = folder2.members();
//	assert("file exists", files.hasMoreElements());
//
//	IFile file2 = (IFile)files.nextElement();
//	assertEquals("File is the same", file, file2);
//}
///**
// * Tests recovery after adding a project and not saving
// */
//public void testSaveCreateProjectCloseOpen() throws CoreException {
//	// Create a solution and save
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//
//	setUpMonitor();
//	workspace.save(monitor);
//
//	//now create a project and close without saving
//	IProject project = solution.getProject(PROJECT);
//	project.create(getMonitor());
//	project.open(getMonitor());
//
//	IPath projectPath = workspace.getLocation().append(solution.getName()).append(project.getName());
//	// NOTE: this accesses the internal representation
//	IPath projectMetaPath = ((Workbench)workspace).getMetaArea().getPathFor((Resource)project);
//
//	/* simulate a crash and recover */
//	solution = null;
//	project = null;
//	crash();
//
//	/* see if the workspace contains the solution and project */
//	solution = workspace.getSolution(SOLUTION);
//	project = solution.getProject(PROJECT);
//	Enumeration solutions = workspace.solutions();
//	assert("solution exists", solutions.hasMoreElements());
//	assertEquals("equal solution", solution, solutions.nextElement());
//
//	Enumeration projects = solution.members();
//	assert("project exists", projects.hasMoreElements());
//	assertEquals("equal projects", project, projects.nextElement());
//	assert("project is open", project.isOpen());
//
//	/* make sure there is a project directory */
//	assert("project file exists", projectPath.toFile().exists());
//	assert("project metafile exists", projectMetaPath.toFile().exists());
//}
///**
// * Tests saving the workspace, then performing snapshots, then crashing and recovering
// */
//public void testSaveSnap() throws CoreException {
//	/* create some resource handles */
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//	
//	IProject project = solution.getProject(PROJECT);
//	project.create(getMonitor());
//	project.open(getMonitor());
//
//	/* save */
//	setUpMonitor();
//	workspace.save(monitor);
//
//	/* do more stuff */
//	IFolder folder = project.getFolder(FOLDER);
//	folder.create(true, getMonitor());
//	IFile file = folder.getFile(FILE);
//	byte[] bytes = "Test bytes".getBytes();
//	java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(bytes);
//	file.create(in, true, getMonitor());
//
//	setUpMonitor();
//	workspace.snapshot(monitor);
//	
//	/* simulate a crash and recover */
//	solution = null;
//	project = null;
//	folder = null;
//	file = null;
//
//	crash();
//
//	solution = workspace.getSolution(SOLUTION);
//	project = solution.getProject(PROJECT);
//	folder = project.getFolder(FOLDER);
//	file = folder.getFile(FILE);
//
//	/* see if the workspace contains the solution and project */
//	Enumeration solutions = workspace.solutions();
//	assert("solution exists", solutions.hasMoreElements());
//
//	ISolution solution2 = (ISolution)solutions.nextElement();
//	assertEquals("Solution is the same", solution, solution2);
//
//	Enumeration projects = solution2.members();
//	assert("project exists", projects.hasMoreElements());
//
//	IProject project2 = (IProject)projects.nextElement();
//	assertEquals("Project is the same", project, project2);
//
//	Enumeration folders = project2.members();
//	assert("folder exists", folders.hasMoreElements());
//
//	IFolder folder2 = (IFolder)folders.nextElement();
//	assertEquals("Project is the same", folder, folder2);
//
//	Enumeration files = folder2.members();
//	assert("file exists", files.hasMoreElements());
//
//	IFile file2 = (IFile)files.nextElement();
//	assertEquals("File is the same", file, file2);
//}
///**
// * Tests saving the workspace, then performing snapshots, then crashing and recovering
// */
//public void testSaveSnapSnap() throws CoreException {
//	/* create some resource handles */
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//	
//	IProject project = solution.getProject(PROJECT);
//	project.create(getMonitor());
//	project.open(getMonitor());
//
//	/* save */
//	setUpMonitor();
//	workspace.save(monitor);
//
//	/* do more stuff */
//	IFolder folder = project.getFolder(FOLDER);
//	folder.create(true, getMonitor());
//	setUpMonitor();
//	workspace.snapshot(monitor);
//
//	/* do even more stuff */
//	IFile file = folder.getFile(FILE);
//	byte[] bytes = "Test bytes".getBytes();
//	java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(bytes);
//	file.create(in, true, getMonitor());
//
//	setUpMonitor();
//	workspace.snapshot(monitor);
//	
//	/* simulate a crash and recover */
//	solution = null;
//	project = null;
//	folder = null;
//	file = null;
//	crash();
//
//	solution = workspace.getSolution(SOLUTION);
//	project = solution.getProject(PROJECT);
//	folder = project.getFolder(FOLDER);
//	file = folder.getFile(FILE);
//
//	/* see if the workspace contains the solution and project */
//	Enumeration solutions = workspace.solutions();
//	assert("solution exists", solutions.hasMoreElements());
//
//	ISolution solution2 = (ISolution)solutions.nextElement();
//	assertEquals("Solution is the same", solution, solution2);
//
//	Enumeration projects = solution2.members();
//	assert("project exists", projects.hasMoreElements());
//
//	IProject project2 = (IProject)projects.nextElement();
//	assertEquals("Project is the same", project, project2);
//
//	Enumeration folders = project2.members();
//	assert("folder exists", folders.hasMoreElements());
//
//	IFolder folder2 = (IFolder)folders.nextElement();
//	assertEquals("Project is the same", folder, folder2);
//
//	Enumeration files = folder2.members();
//	assert("file exists", files.hasMoreElements());
//
//	IFile file2 = (IFile)files.nextElement();
//	assertEquals("File is the same", file, file2);
//}
///**
// * Tests performing a save on a workspace, then crashing and recovering.
// */
//public void testSaveWithClosedProject() throws CoreException {
//	/* create some resource handles */
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//	IProject project = solution.getProject(PROJECT);
//	project.create(getMonitor());
//	project.open(getMonitor());
//	project.close(getMonitor());
//
//	setUpMonitor();
//	workspace.save(monitor);
//
//	/* simulate a crash and recover */
//	solution = null;
//	project = null;
//	crash();
//
//	solution = workspace.getSolution(SOLUTION);
//	project = solution.getProject(PROJECT);
//
//	/* see if the workspace contains the solution and project */
//	Enumeration solutions = workspace.solutions();
//	assert("solution exists", solutions.hasMoreElements());
//	ISolution solution2 = (ISolution) solutions.nextElement();
//	assertEquals("Solution is the same", solution, solution2);
//	assert("Solution is open", solution2.isOpen());
//
//	Enumeration projects = solution.members();
//	assert("project exists", projects.hasMoreElements());
//	IProject project2 = (IProject) projects.nextElement();
//	assertEquals("Project is the same", project, project2);
//	assert("Project is closed", !project2.isOpen());
//
//}
///**
// * Tests performing a snapshot on a workspace that has never been
// * saved, then crashing and recovering.
// */
//public void testSnap() throws CoreException {
//	/* create some resource handles */
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//	IProject project = solution.getProject(PROJECT);
//	project.create(getMonitor());
//	project.open(getMonitor());
//
//	/* snapshot */
//	setUpMonitor();
//	workspace.snapshot(monitor);
//
//	/* simulate a crash and recover */
//	solution = null;
//	project = null;
//	crash();
//
//	solution = workspace.getSolution(SOLUTION);
//	project = solution.getProject(PROJECT);
//
//	/* see if the workspace contains the solution and project */
//	Enumeration solutions = workspace.solutions();
//	assert("solution exists", solutions.hasMoreElements());
//	ISolution solution2 = (ISolution) solutions.nextElement();
//	assertEquals("Solution is the same", solution, solution2);
//	assert("Solution is open", solution2.isOpen());
//
//	Enumeration projects = solution.members();
//	assert("project exists", projects.hasMoreElements());
//	IProject project2 = (IProject) projects.nextElement();
//	assertEquals("Project is the same", project, project2);
//	assert("Project is open", project2.isOpen());
//}
///**
// * Tests performing snapshots, then saving, then crashing and recovering.
// */
//public void testSnapSave() throws CoreException {
//	/* create some resource handles */
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//	
//	IProject project = solution.getProject(PROJECT);
//	project.create(getMonitor());
//	project.open(getMonitor());
//
//	/* snap */
//	setUpMonitor();
//	workspace.snapshot(monitor);
//
//	/* do more stuff */
//	IFolder folder = project.getFolder(FOLDER);
//	folder.create(true, getMonitor());
//
//	setUpMonitor();
//	workspace.snapshot(monitor);
//
//	/* do even more stuff */
//	IFile file = folder.getFile(FILE);
//	byte[] bytes = "Test bytes".getBytes();
//	java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(bytes);
//	file.create(in, true, getMonitor());
//
//	setUpMonitor();
//	workspace.save(monitor);
//	
//	/* simulate a crash and recover */
//	solution = null;
//	project = null;
//	folder = null;
//	file = null;
//	crash();
//
//	solution = workspace.getSolution(SOLUTION);
//	project = solution.getProject(PROJECT);
//	folder = project.getFolder(FOLDER);
//	file = folder.getFile(FILE);
//
//	/* see if the workspace contains the solution and project */
//	Enumeration solutions = workspace.solutions();
//	assert("solution exists", solutions.hasMoreElements());
//
//	ISolution solution2 = (ISolution)solutions.nextElement();
//	assertEquals("Solution is the same", solution, solution2);
//
//	Enumeration projects = solution2.members();
//	assert("project exists", projects.hasMoreElements());
//
//	IProject project2 = (IProject)projects.nextElement();
//	assertEquals("Project is the same", project, project2);
//
//	Enumeration folders = project2.members();
//	assert("folder exists", folders.hasMoreElements());
//
//	IFolder folder2 = (IFolder)folders.nextElement();
//	assertEquals("Project is the same", folder, folder2);
//
//	Enumeration files = folder2.members();
//	assert("file exists", files.hasMoreElements());
//
//	IFile file2 = (IFile)files.nextElement();
//	assertEquals("File is the same", file, file2);
//}
///**
// * Tests performing a snapshot, saving, performing another snapshot,
// * then crashing and recovering.
// */
//public void testSnapSaveSnap() throws CoreException {
//	/* create some resource handles */
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//	
//	IProject project = solution.getProject(PROJECT);
//	project.create(getMonitor());
//	project.open(getMonitor());
//
//	setUpMonitor();
//	workspace.snapshot(monitor);
//
//	/* do more stuff */
//	IFolder folder = project.getFolder(FOLDER);
//	folder.create(true, getMonitor());
//
//	setUpMonitor();
//	workspace.save(monitor);
//
//	/* do even more stuff */
//	IFile file = folder.getFile(FILE);
//	byte[] bytes = "Test bytes".getBytes();
//	java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(bytes);
//	file.create(in, true, getMonitor());
//
//	setUpMonitor();
//	workspace.snapshot(monitor);
//	
//	/* simulate a crash and recover */
//	solution = null;
//	project = null;
//	folder = null;
//	file = null;
//	crash();
//
//	solution = workspace.getSolution(SOLUTION);
//	project = solution.getProject(PROJECT);
//	folder = project.getFolder(FOLDER);
//	file = folder.getFile(FILE);
//
//	/* see if the workspace contains the solution and project */
//	Enumeration solutions = workspace.solutions();
//	assert("solution exists", solutions.hasMoreElements());
//
//	ISolution solution2 = (ISolution)solutions.nextElement();
//	assertEquals("Solution is the same", solution, solution2);
//
//	Enumeration projects = solution2.members();
//	assert("project exists", projects.hasMoreElements());
//
//	IProject project2 = (IProject)projects.nextElement();
//	assertEquals("Project is the same", project, project2);
//
//	Enumeration folders = project2.members();
//	assert("folder exists", folders.hasMoreElements());
//
//	IFolder folder2 = (IFolder)folders.nextElement();
//	assertEquals("Project is the same", folder, folder2);
//
//	Enumeration files = folder2.members();
//	assert("file exists", files.hasMoreElements());
//
//	IFile file2 = (IFile)files.nextElement();
//	assertEquals("File is the same", file, file2);
//}
///**
// * Tests performing a snapshot on a workspace that has never been
// * saved, then crashing and recovering.
// */
//public void testSnapWithClosedProject() throws CoreException {
//	/* create some resource handles */
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//	IProject project = solution.getProject(PROJECT);
//	project.create(getMonitor());
//	project.open(getMonitor());
//	project.close(getMonitor());
//
//	/* snapshot */
//	setUpMonitor();
//	workspace.snapshot(monitor);
//
//	/* simulate a crash and recover */
//	solution = null;
//	project = null;
//	crash();
//
//	solution = workspace.getSolution(SOLUTION);
//	project = solution.getProject(PROJECT);
//
//	/* see if the workspace contains the solution and project */
//	Enumeration solutions = workspace.solutions();
//	assert("solution exists", solutions.hasMoreElements());
//	ISolution solution2 = (ISolution) solutions.nextElement();
//	assertEquals("Solution is the same", solution, solution2);
//	assert("Solution is open", solution2.isOpen());
//
//	Enumeration projects = solution.members();
//	assert("project exists", projects.hasMoreElements());
//	IProject project2 = (IProject) projects.nextElement();
//	assertEquals("Project is the same", project, project2);
//	assert("Project is closed", !project2.isOpen());
//}
///**
// * Tests performing a snapshot on a workspace that has never been
// * saved, then crashing and recovering.
// */
//public void testSnapWithClosedSolution() throws CoreException {
//	/* create some resource handles */
//	ISolution solution = workspace.getSolution(SOLUTION);
//	solution.create(getMonitor());
//	solution.open(getMonitor());
//	IProject project = solution.getProject(PROJECT);
//	project.create(getMonitor());
//	project.open(getMonitor());
//	project.close(getMonitor());
//	solution.close(getMonitor());
//
//	/* snapshot */
//	setUpMonitor();
//	workspace.snapshot(monitor);
//
//	/* simulate a crash and recover */
//	solution = null;
//	project = null;
//	crash();
//
//	solution = workspace.getSolution(SOLUTION);
//	project = solution.getProject(PROJECT);
//
//	/* see if the workspace contains the solution and project */
//	Enumeration solutions = workspace.solutions();
//	assert("solution exists", solutions.hasMoreElements());
//	ISolution solution2 = (ISolution) solutions.nextElement();
//	assertEquals("Solution is the same", solution, solution2);
//	assert("Solution is open", !solution2.isOpen());
//
//	Enumeration projects = solution.members();
//	assert("project exists", !projects.hasMoreElements());
//}
}


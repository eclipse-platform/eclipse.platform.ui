package org.eclipse.core.tests.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.localstore.HistoryStore;
import org.eclipse.core.internal.resources.FileState;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.utils.UniversalUniqueIdentifier;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

/**
 * This class defines all tests for the HistoryStore Class.
 */
public class HistoryStoreTest extends EclipseWorkspaceTest {
public HistoryStoreTest() {
	super();
}
public HistoryStoreTest(String name) {
	super(name);
}
public static Test suite() {
//	TestSuite suite = new TestSuite();
//	suite.addTest(new HistoryStoreTest("testMove"));
//	return suite;
	return new TestSuite(HistoryStoreTest.class);
}
protected void tearDown() throws Exception {
	IProject[] projects = getWorkspace().getRoot().getProjects();
	getWorkspace().delete(projects, true, null);
}
public void testAddStateAndPolicies() {

	/* Create common objects. */
	IProject project = getWorkspace().getRoot().getProject("Project");
	IFile file = project.getFile("file.txt");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
		file.create(getRandomContents(), true, getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}

	/* set local history policies */
	IWorkspaceDescription originalDescription = getWorkspace().getDescription(); // keep orignal
	IWorkspaceDescription description = getWorkspace().getDescription(); // get another copy for changes
	description.setFileStateLongevity(1000 * 3600 * 24); // 1 day
	description.setMaxFileStates(5);
	description.setMaxFileStateSize(1024 * 1024); // 1 Mb
	try {
		getWorkspace().setDescription(description);
	} catch (CoreException e) {
		fail("0.1", e);
	}

	/* test max file states */
	for (int i = 0; i < 8; i++) {
		try {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			file.setContents(getRandomContents(), true, true, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
	}
	IFileState[] states = null;
	try {
		getWorkspace().save(true, null);
		states = file.getHistory(getMonitor());
	} catch (CoreException e) {
		fail("1.1", e);
	}
	assertEquals("1.2", description.getMaxFileStates(), states.length);

	// assert that states are in the correct order (newer ones first)
	long lastModified = states[0].getModificationTime();
	for (int i = 1; i < states.length; i++) {
		assertTrue("1.3", lastModified > states[i].getModificationTime());
		lastModified = states[i].getModificationTime();
	}

	/* test max file state size */
	description.setMaxFileStates(15);
	description.setMaxFileStateSize(7); // 7 bytes
	try {
		getWorkspace().setDescription(description);
	} catch (CoreException e) {
		fail("2.0.0", e);
	}
	file = project.getFile("file1.txt");
	try {
		file.create(null, true, getMonitor());
	} catch (CoreException e) {
		fail("2.0", e);
	}
	for (int i = 0; i < 10; i++) {
		try {
			file.appendContents(getContents("a"), true, true, getMonitor());
		} catch (CoreException e) {
			fail("2.1", e);
		}
	}
	try {
		states = file.getHistory(getMonitor());
		assertEquals("2.2", description.getMaxFileStateSize(), states.length);
	} catch (CoreException e) {
		fail("2.3", e);
	}

	/* test max file longevity */
	file = project.getFile("file.txt"); // use the file of the first test
	description.setFileStateLongevity(1000 * 3600 * 24); // 1 day
	description.setMaxFileStates(5);
	description.setMaxFileStateSize(1024 * 1024); // 1 Mb
	try { // the description should be the same as the first test
		getWorkspace().setDescription(description);
	} catch (CoreException e) {
		fail("3.0", e);
	}
	try {
		states = file.getHistory(getMonitor());
		assertEquals("3.3", description.getMaxFileStates(), states.length);
	} catch (CoreException e) {
		fail("3.2", e);
	}
	// change policies
	description.setFileStateLongevity(1000 * 10); // 10 seconds
	description.setMaxFileStateSize(1024 * 1024); // 1 Mb
	try {
		getWorkspace().setDescription(description);
	} catch (CoreException e) {
		fail("3.3", e);
	}
	try {
		Thread.sleep(1000 * 10); // sleep for 10 seconds
	} catch (InterruptedException e) {
	}
	try {
		getWorkspace().save(true, null);
		states = file.getHistory(getMonitor());
		assertEquals("3.4", 0, states.length);
	} catch (CoreException e) {
		fail("3.5", e);
	}

	/* remove garbage */
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("20.0", e);
	}
	try {
		getWorkspace().setDescription(originalDescription);
	} catch (CoreException e) {
		fail("20.1", e);
	}
}
public void testClean() {

	/* Create common objects. */
	IProject project = getWorkspace().getRoot().getProject("Project");
	IFile file = project.getFile("file.txt");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
		file.create(getRandomContents(), true, getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}
	HistoryStore store = ((Workspace) getWorkspace()).getFileSystemManager().getHistoryStore();
	IWorkspaceDescription originalDescription = getWorkspace().getDescription(); // keep orignal
	IWorkspaceDescription description = getWorkspace().getDescription(); // get another copy for changes

	/* test max file states */
	description.setFileStateLongevity(1000 * 3600 * 24); // 1 day
	description.setMaxFileStates(500);
	description.setMaxFileStateSize(1024 * 1024); // 1 Mb
	try {
		getWorkspace().setDescription(description);
	} catch (CoreException e) {
		fail("0.1", e);
	}

	for (int i = 0; i < 8; i++) {
		try {
			try {
				Thread.sleep(5000); // necessary because of lastmodified granularity in some file systems
			} catch (InterruptedException e) {
			}
			file.setContents(getRandomContents(), true, true, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
	}
	try {
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("1.1", 8, states.length);
	} catch (CoreException e) {
		fail("1.2", e);
	}

	description.setMaxFileStates(3);
	try {
		getWorkspace().setDescription(description);
	} catch (CoreException e) {
		fail("2.0", e);
	}
	store.clean();
	description.setMaxFileStates(500);
	try {
		getWorkspace().setDescription(description);
	} catch (CoreException e) {
		fail("2.2", e);
	}

	try {
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("2.3", 3, states.length);
		// assert that states are in the correct order (newer ones first)
		long lastModified = states[0].getModificationTime();
		for (int i = 1; i < states.length; i++) {
			assertTrue("2.4." + i, lastModified > states[i].getModificationTime());
			lastModified = states[i].getModificationTime();
		}
	} catch (CoreException e) {
		fail("2.5", e);
	}

	/* test max file longevity */
	file = project.getFile("file.txt"); // use the file of the first test
	description.setFileStateLongevity(1000 * 3600 * 24); // 1 day
	description.setMaxFileStates(500);
	description.setMaxFileStateSize(1024 * 1024); // 1 Mb
	try { // the description should be the same as the first test
		getWorkspace().setDescription(description);
	} catch (CoreException e) {
		fail("3.0", e);
	}
	try {
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("3.1", 3, states.length);
	} catch (CoreException e) {
		fail("3.2", e);
	}
	// change policies
	description.setFileStateLongevity(1000 * 10); // 10 seconds
	description.setMaxFileStateSize(1024 * 1024); // 1 Mb
	try {
		getWorkspace().setDescription(description);
	} catch (CoreException e) {
		fail("4.0", e);
	}
	try {
		Thread.sleep(1000 * 10); // sleep for 10 seconds
	} catch (InterruptedException e) {
	}
	store.clean();
	// change policies
	description.setFileStateLongevity(1000 * 3600 * 24); // 1 day
	description.setMaxFileStateSize(1024 * 1024); // 1 Mb
	try {
		getWorkspace().setDescription(description);
	} catch (CoreException e) {
		fail("5.0", e);
	}
	try {
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("5.1", 0, states.length);
	} catch (CoreException e) {
		fail("5.2", e);
	}

	/* remove garbage */
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("20.0", e);
	}
	try {
		getWorkspace().setDescription(originalDescription);
	} catch (CoreException e) {
		fail("20.1", e);
	}
}
/**
 * Test for existence of file states in the HistoryStore.
 */
public void testExists() throws Throwable {

	/* Create common objects. */
	IProject project = getWorkspace().getRoot().getProject("Project");
	IFile file = project.getFile("removeAllStatesFile.txt");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
		file.create(getRandomContents(), true, getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}
	final int ITERATIONS = 20;

	/* Add multiple states for one file location. */
	for (int i = 0; i < ITERATIONS; i++) {
		try {
			file.setContents(getRandomContents(), true, true, getMonitor());
		} catch (CoreException e) {
			fail("3.0." + i, e);
		}
	}

	/* Valid Case: Test retrieved values. */
	IFileState[] states = null;
	try {
		states = file.getHistory(getMonitor());
	} catch (CoreException e) {
		fail("5.0", e);
	}
	assertTrue("5.1", states.length == ITERATIONS);
	for (int i = 0; i < states.length; i++)
		assertTrue("5.2." + i, states[i].exists());

	/* remove garbage */
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("20.0", e);
	}
}
/**
 * Test for retrieving contents of files with states logged in the HistoryStore.
 */
public void testGetContents() throws Throwable {

	final int ITERATIONS = 20;

	/* Create common objects. */
	IProject project = getWorkspace().getRoot().getProject("Project");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}

	/* Create files. */
	IFile file = project.getFile("getContentsFile.txt");
	String contents = "This file has some contents in testGetContents.";
	ensureExistsInWorkspace(file, contents);

	IFile secondValidFile = project.getFile("secondGetContentsFile.txt");
	contents = "A file with some other contents in testGetContents.";
	ensureExistsInWorkspace(secondValidFile, contents);

	HistoryStore historyStore = ((Workspace) getWorkspace()).getFileSystemManager().getHistoryStore();

	/* Simulated date -- Increment once for each edition added. */
	long myLong = 0L;

	/* Add multiple editions for one file location. */
	for (int i = 0; i < ITERATIONS; i++, myLong++) {
		historyStore.addState(file.getFullPath(), file.getLocation(), myLong, true);
		try {
			contents = "This file has some contents in testGetContents.";
			InputStream is = new ByteArrayInputStream(contents.getBytes());
			createFileInFileSystem(file.getLocation(), is);
			file.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			fail("1.1." + i, e);
		} catch (IOException e) {
			fail("1.2." + i, e);
		}
	}

	/* Add multiple editions for second file location. */
	for (int i = 0; i < ITERATIONS; i++, myLong++) {
		historyStore.addState(secondValidFile.getFullPath(), secondValidFile.getLocation(), myLong, true);
		try {
			contents = "A file with some other contents in testGetContents.";
			InputStream is = new ByteArrayInputStream(contents.getBytes());
			createFileInFileSystem(secondValidFile.getLocation(), is);
			secondValidFile.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			fail("2.1." + i, e);
		} catch (IOException e) {
			fail("2.2." + i, e);
		}
	}

	/* Ensure contents of file and retrieved resource are identical.
	   Does not check timestamps. Timestamp checks are performed in a separate test. */
	DataInputStream inFile = null;
	DataInputStream inContents = null;
	IFileState[] stateArray = null;
	stateArray = historyStore.getStates(file.getFullPath());
	for (int i = 0; i < stateArray.length; i++, myLong++) {
		inFile = new DataInputStream(file.getContents(false));
		try {
			inContents = new DataInputStream(historyStore.getContents(stateArray[i]));
		} catch (CoreException e) {
			fail("3.1." + i, e);
		}
		if (!compareContent(inFile, inContents))
			fail("3.2." + i + " No match, files are not identical.");
	}

	stateArray = historyStore.getStates(secondValidFile.getFullPath());
	for (int i = 0; i < stateArray.length; i++, myLong++) {
		inFile = new DataInputStream(secondValidFile.getContents(false));
		try {
			inContents = new DataInputStream(historyStore.getContents(stateArray[i]));
		} catch (CoreException e) {
			fail("4.1." + i, e);
		}
		if (!compareContent(inFile, inContents))
			fail("4.2." + i + " No match, files are not identical.");
	}

	/* Test getting an invalid file state. */
	for (int i = 0; i < ITERATIONS; i++) {
		// Create bogus FileState using invalid uuid.
		try {
			InputStream in = historyStore.getContents(new FileState(historyStore, Path.ROOT, myLong, new UniversalUniqueIdentifier()));
			in.close();
			fail("6." + i + " Edition should be invalid.");
		} catch (CoreException e) {
		}
	}

	/* Test verification using null file state. */
	for (int i = 0; i < ITERATIONS; i++) {
		try {
			historyStore.getContents(null);
			fail("7." + i + " Null edition should be invalid.");
		} catch (RuntimeException e) {
		}
	}

	/* remove garbage */
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("20.0", e);
	}
}
public void testRemoveAll() {

	/* Create common objects. */
	IProject project = getWorkspace().getRoot().getProject("Project");
	IFile file = project.getFile("removeAllStatesFile.txt");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
		file.create(getRandomContents(), true, getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}
	final int ITERATIONS = 20;

	/* test remove in a file */
	for (int i = 0; i < ITERATIONS; i++) {
		try {
			file.setContents(getRandomContents(), true, true, getMonitor());
		} catch (CoreException e) {
			fail("3.0." + i, e);
		}
	}

	/* Valid Case: Ensure correct number of states available. */
	IFileState[] states = null;
	try {
		states = file.getHistory(getMonitor());
	} catch (CoreException e) {
		fail("4.0", e);
	}
	assertTrue("4.1", states.length == ITERATIONS);

	/* Remove all states, and verify that no states remain. */
	try {
		file.clearHistory(getMonitor());
		states = file.getHistory(getMonitor());
		assertEquals("5.0", 0, states.length);
	} catch (CoreException e) {
		fail("5.1", e);
	}

	/* test remove in a folder -- make sure it does not affect other resources' states*/
	IFolder folder = project.getFolder("folder");
	IFile anotherOne = folder.getFile("anotherOne");
	try {
		folder.create(true, true, getMonitor());
		anotherOne.create(getRandomContents(), true, getMonitor());
	} catch (CoreException e) {
		fail("6.0", e);
	}
	for (int i = 0; i < ITERATIONS; i++) {
		try {
			file.setContents(getRandomContents(), true, true, getMonitor());
			anotherOne.setContents(getRandomContents(), true, true, getMonitor());
		} catch (CoreException e) {
			fail("6.1." + i, e);
		}
	}

	try {
		states = file.getHistory(getMonitor());
		assertEquals("6.2", ITERATIONS, states.length);
		states = anotherOne.getHistory(getMonitor());
		assertEquals("6.3", ITERATIONS, states.length);
	} catch (CoreException e) {
		fail("6.4", e);
	}

	/* Remove all states, and verify that no states remain. */
	try {
		project.clearHistory(getMonitor());
		states = file.getHistory(getMonitor());
		assertEquals("7.0", 0, states.length);
		states = anotherOne.getHistory(getMonitor());
		assertEquals("7.1", 0, states.length);
	} catch (CoreException e) {
		fail("7.2", e);
	}

	/* test remove in a folder -- make sure it does not affect other resources' states*/
	IFile aaa = project.getFile("aaa");
	IFolder bbb = project.getFolder("bbb");
	anotherOne = bbb.getFile("anotherOne");
	IFile ccc = project.getFile("ccc");
	try {
		bbb.create(true, true, getMonitor());
		anotherOne.create(getRandomContents(), true, getMonitor());
		aaa.create(getRandomContents(), true, getMonitor());
		ccc.create(getRandomContents(), true, getMonitor());
	} catch (CoreException e) {
		fail("8.0", e);
	}
	for (int i = 0; i < ITERATIONS; i++) {
		try {
			anotherOne.setContents(getRandomContents(), true, true, getMonitor());
			aaa.setContents(getRandomContents(), true, true, getMonitor());
			ccc.setContents(getRandomContents(), true, true, getMonitor());
		} catch (CoreException e) {
			fail("8.1." + i, e);
		}
	}

	try {
		states = anotherOne.getHistory(getMonitor());
		assertEquals("8.3", ITERATIONS, states.length);
		states = aaa.getHistory(getMonitor());
		assertEquals("8.4", ITERATIONS, states.length);
		states = ccc.getHistory(getMonitor());
		assertEquals("8.5", ITERATIONS, states.length);
	} catch (CoreException e) {
		fail("8.6", e);
	}

	/* Remove all states, and verify that no states remain. aaa and ccc should not be affected. */
	try {
		bbb.clearHistory(getMonitor());
		states = anotherOne.getHistory(getMonitor());
		assertEquals("9.1", 0, states.length);
		states = aaa.getHistory(getMonitor());
		assertEquals("9.2", ITERATIONS, states.length);
		states = ccc.getHistory(getMonitor());
		assertEquals("9.3", ITERATIONS, states.length);
	} catch (CoreException e) {
		fail("9.4", e);
	}

	/* remove garbage */
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("20.0", e);
	}
}
/**
 * Simple copy case for History Store.
 *
 * Scenario:
 *   1. Create file					"content 1"
 *   2. Set new content				"content 2"
 *   3. Set new content				"content 3"
 *   4. Copy file
 *   2. Set new content	to copy		"content 1"
 *   3. Set new content to copy		"content 2"
 *
 * The original file and the copied file should each have two states available.
 */
public void testSimpleCopy() {
	
	/* Initialize common objects. */
	IProject project = getWorkspace().getRoot().getProject("Project");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}
	String[] contents = {"content1", "content2", "content3"};
	IFile file = project.getFile("simpleCopyFile");
	IFile copyFile = project.getFile("copyOfSimpleCopyFile");
	
	/* Create first file. */
	try {
		file.create(getContents(contents[0]), true, null);
	} catch (CoreException e) {
		fail("1.2", e);
	}

	/* Set new contents on first file. Should add two entries to the history store. */
	try {
		file.setContents(getContents(contents[1]), true, true, null);
		file.setContents(getContents(contents[2]), true, true, null);
	} catch (CoreException e) {
		fail("2.0", e);
	}
	
	/* Copy first file to the second. Second file should have no history. */
	try {
		file.copy(copyFile.getFullPath(), true, null);
	} catch (CoreException e) {
		fail("3.0", e);
	}

	/* Check history for both files. */
	try {
		IFileState[] states = file.getHistory(null);
		assertTrue("4.0", states.length == 2);
		states= copyFile.getHistory(null);
		assertTrue("4.1", states.length == 0);
	} catch (CoreException e) {
		fail("4.2", e);
	}

	/* Set new contents on second file. Should add two entries to the history store. */
	try {
		copyFile.setContents(getContents(contents[0]), true, true, null);
		copyFile.setContents(getContents(contents[1]), true, true, null);
	} catch (CoreException e) {
		fail("5.0", e);
	}

	/* Check history for both files. */
	try {
		// Check log for original file.
		IFileState[] states = file.getHistory(null);
		assertTrue("6.0", states.length == 2);
		assertTrue("6.1", compareContent(getContents(contents[1]), states[0].getContents()));
		assertTrue("6.2", compareContent(getContents(contents[0]), states[1].getContents()));

		// Check log for copy.
		states = copyFile.getHistory(null);
		assertTrue("6.3", states.length == 2);
		assertTrue("6.4", compareContent(getContents(contents[0]), states[0].getContents()));
		assertTrue("6.5", compareContent(getContents(contents[2]), states[1].getContents()));
		
	} catch (CoreException e) {
		fail("6.6", e);
	}
	

	/* remove garbage */
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("20.0", e);
	}
}
/**
 * Simple move case for History Store.
 *
 * Scenario:
 *   1. Create file						"content 1"
 *   2. Set new content					"content 2"
 *   3. Set new content					"content 3"
 *   4. Move file
 *   5. Set new content	to moved file	"content 1"
 *   6. Set new content to moved file	"content 2"
 *
 * The original file and the moved file should each have two states available.
 */
public void testSimpleMove() {

	/* Initialize common objects. */
	IProject project = getWorkspace().getRoot().getProject("Project");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}
	String[] contents = { "content1", "content2", "content3" };
	IFile file = project.getFile("simpleMoveFile");
	IFile moveFile = project.getFile("copyOfSimpleMoveFile");

	/* Create first file. */
	try {
		file.create(getContents(contents[0]), true, null);
	} catch (CoreException e) {
		fail("1.2", e);
	}

	/* Set new contents on source file. Should add two entries to the history store. */
	try {
		file.setContents(getContents(contents[1]), true, true, null);
		file.setContents(getContents(contents[2]), true, true, null);
	} catch (CoreException e) {
		fail("2.0", e);
	}

	/* Move source file to second location. Moved files should have no history. */
	try {
		file.move(moveFile.getFullPath(), true, null);
	} catch (CoreException e) {
		fail("3.0", e);
	}

	/* Check history for both files. */
	try {
		IFileState[] states = file.getHistory(null);
		assertTrue("4.0", states.length == 2);
		states = moveFile.getHistory(null);
		assertTrue("4.1", states.length == 0);
	} catch (CoreException e) {
		fail("4.2", e);
	}

	/* Set new contents on moved file. Should add two entries to the history store. */
	try {
		moveFile.setContents(getContents(contents[0]), true, true, null);
		moveFile.setContents(getContents(contents[1]), true, true, null);
	} catch (CoreException e) {
		fail("5.0", e);
	}

	/* Check history for both files. */
	try {
		// Check log for original file.
		IFileState[] states = file.getHistory(null);
		assertTrue("6.0", states.length == 2);
		assertTrue("6.1", compareContent(getContents(contents[1]), states[0].getContents()));
		assertTrue("6.2", compareContent(getContents(contents[0]), states[1].getContents()));

		// Check log for moved file.
		states = moveFile.getHistory(null);
		assertTrue("6.3", states.length == 2);
		assertTrue("6.4", compareContent(getContents(contents[0]), states[0].getContents()));
		assertTrue("6.5", compareContent(getContents(contents[2]), states[1].getContents()));

	} catch (CoreException e) {
		fail("6.6", e);
	}

	/* remove garbage */
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("20.0", e);
	}
}
/**
 * Simple use case for History Store.
 *
 * Scenario:									   # Editions
 *   1. Create file					"content 1"			0		
 *   2. Set new content				"content 2"			1	
 *   3. Set new content				"content 3"			2
 *   4. Delete file										3
 *   5. Roll back to first version  "content 1"			3
 *   6. Set new content				"content 2"			4
 *   7. Roll back to third version  "content 3"			5
 */
public void testSimpleUse() {

	/* Initialize common objects. */
	IProject project = getWorkspace().getRoot().getProject("Project");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}
	String[] contents = { "content1", "content2", "content3" };
	IFile file = project.getFile("file");

	/* Create the file. */
	try {
		file.create(getContents(contents[0]), true, getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}

	/* Set new contents on the file. Should add two entries to the store. */
	try {
		for (int i = 0; i < 2; i++)
			file.setContents(getContents(contents[i + 1]), true, true, getMonitor());
	} catch (CoreException e) {
		fail("2.0", e);
	}

	/* Ensure two entries are available for the file, and that content matches. */
	try {
		IFileState[] states = file.getHistory(getMonitor());
		assertTrue("3.0", states.length == 2);
		assertTrue("3.1.1", compareContent(getContents(contents[1]), states[0].getContents()));
		assertTrue("3.1.2", compareContent(getContents(contents[0]), states[1].getContents()));
	} catch (CoreException e) {
		fail("3.2", e);
	}

	/* Delete the file. Should add an entry to the store. */
	try {
		file.delete(true, true, getMonitor());
	} catch (CoreException e) {
		fail("4.0", e);
	}

	/* Ensure three entries are available for the file, and that content matches. */
	try {
		IFileState[] states = file.getHistory(getMonitor());
		assertTrue("5.0", states.length == 3);
		assertTrue("5.1.1", compareContent(getContents(contents[2]), states[0].getContents()));
		assertTrue("5.1.2", compareContent(getContents(contents[1]), states[1].getContents()));
		assertTrue("5.1.3", compareContent(getContents(contents[0]), states[2].getContents()));
	} catch (CoreException e) {
		fail("5.2", e);
	}

	/* Roll file back to first version, and ensure that content matches. */
	try {
		IFileState[] states = file.getHistory(getMonitor());
		// Create the file with the contents from one of the states. 
		// Won't add another entry to the store.
		file.create(states[0].getContents(), false, getMonitor());

		// Check history store.
		states = file.getHistory(getMonitor());
		assertTrue("6.0", states.length == 3);
		assertTrue("6.1.1", compareContent(getContents(contents[2]), states[0].getContents()));
		assertTrue("6.1.2", compareContent(getContents(contents[1]), states[1].getContents()));
		assertTrue("6.1.3", compareContent(getContents(contents[0]), states[2].getContents()));

		// Check file contents.
		assertTrue("6.2", compareContent(getContents(contents[2]), file.getContents(false)));

	} catch (CoreException e) {
		fail("6.3", e);
	}

	/* Set new contents on the file. Should add an entry to the history store. */
	try {
		file.setContents(getContents(contents[1]), true, true, null);
	} catch (CoreException e) {
		fail("7.0", e);
	}

	/* Ensure four entries are available for the file, and that entries match. */
	try {
		IFileState[] states = file.getHistory(getMonitor());
		assertTrue("8.0", states.length == 4);
		assertTrue("8.1.1", compareContent(getContents(contents[2]), states[0].getContents()));
		assertTrue("8.1.2", compareContent(getContents(contents[2]), states[1].getContents()));
		assertTrue("8.1.3", compareContent(getContents(contents[1]), states[2].getContents()));
		assertTrue("8.1.4", compareContent(getContents(contents[0]), states[3].getContents()));
	} catch (CoreException e) {
		fail("8.2", e);
	}

	/* Roll file back to third version, and ensure that content matches. */
	try {
		IFileState[] states = file.getHistory(getMonitor());
		// Will add another entry to log.
		file.setContents(states[2], true, true, getMonitor());

		// Check history log.
		states = file.getHistory(getMonitor());
		assertTrue("9.0", states.length == 5);
		assertTrue("9.1.1", compareContent(getContents(contents[1]), states[0].getContents()));
		assertTrue("9.1.2", compareContent(getContents(contents[2]), states[1].getContents()));
		assertTrue("9.1.3", compareContent(getContents(contents[2]), states[2].getContents()));
		assertTrue("9.1.4", compareContent(getContents(contents[1]), states[3].getContents()));
		assertTrue("9.1.5", compareContent(getContents(contents[0]), states[4].getContents()));

		// Check file contents.
		assertTrue("9.2", compareContent(getContents(contents[1]), file.getContents(false)));

	} catch (CoreException e) {
		fail("9.3", e);
	}

	/* remove garbage */
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("20.0", e);
	}
}
public void testDelete() {
	// create common objects
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}
	
	// test file
	IFile file = project.getFile("file.txt");
	try {
		file.create(getRandomContents(), true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		file.delete(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("1.0", 3, states.length);
	} catch (CoreException e) {
		fail("1.20", e);
	}

	// test folder
	IFolder folder = project.getFolder("folder");
	file = folder.getFile("file.txt");
	try {
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		folder.delete(true, true, getMonitor());
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("2.0", 3, states.length);
	} catch (CoreException e) {
		fail("2.20", e);
	}
	
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("20.0", e);
	}
}

public void testFindDeleted() {
	// create common objects
	IWorkspaceRoot root = getWorkspace().getRoot();
	IProject project = root.getProject("MyProject");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
		
		IFile[] df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("0.1", 0, df.length);
	} catch (CoreException e) {
		fail("0.0", e);
	}
	
	// test that a deleted file can be found
	IFile pfile = project.getFile("file.txt");
	try {
		// create and delete a file
		pfile.create(getRandomContents(), true, getMonitor());
		pfile.delete(true, true, getMonitor());
		
		// the deleted file should show up as a deleted member of project
		IFile[] df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("0.1", 1, df.length);
		assertEquals("0.2", pfile, df[0]);
		
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("0.3", 1, df.length);
		assertEquals("0.4", pfile, df[0]);
		
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("0.5", 0, df.length);
		
		// the deleted file should show up as a deleted member of workspace root
		df = root.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("0.5.1", 0, df.length);
		
		df = root.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("0.5.2", 1, df.length);
		assertEquals("0.5.3", pfile, df[0]);
		
		df = root.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("0.5.4", 0, df.length);
		
		// recreate the file
		pfile.create(getRandomContents(), true, getMonitor());
		
		// the deleted file should no longer show up as a deleted member of project
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("0.6", 0, df.length);
		
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("0.7", 0, df.length);
		
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("0.8", 0, df.length);
		
		// the deleted file should no longer show up as a deleted member of ws root
		df = root.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("0.8.1", 0, df.length);
		
		df = root.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("0.8.2", 0, df.length);
		
		df = root.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("0.8.3", 0, df.length);
		
	} catch (CoreException e) {
		fail("0.00", e);
	}

	// scrub the project
	try {
		project.delete(true, getMonitor());
		project.create(getMonitor());
		project.open(getMonitor());
		
		IFile[] df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("0.9", 0, df.length);
	} catch (CoreException e) {
		fail("0.10", e);
	}
	
	// test folder
	IFolder folder = project.getFolder("folder");
	IFile file = folder.getFile("file.txt");
	IFile folderAsFile = project.getFile(folder.getProjectRelativePath());
	try {
		// create and delete a file in a folder
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		file.delete(true, true, getMonitor());

		// the deleted file should show up as a deleted member
		IFile[] df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("1.1", 0, df.length);
		
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("1.2", 1, df.length);
		assertEquals("1.3", file, df[0]);
		
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("1.4", 0, df.length);
		
		// recreate the file
		file.create(getRandomContents(), true, getMonitor());
		
		// the deleted file should no longer show up as a deleted member
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("1.5", 0, df.length);
		
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("1.6", 0, df.length);
		
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("1.7", 0, df.length);
		
		// deleting the folder should bring it back
		folder.delete(true, true, getMonitor());
		
		// the deleted file should show up as a deleted member of project
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("1.8", 0, df.length);
		
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("1.9", 1, df.length);
		assertEquals("1.10", file, df[0]);
		
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("1.11", 0, df.length);
		
		// create and delete a file where the folder was
		folderAsFile.create(getRandomContents(), true, getMonitor());
		folderAsFile.delete(true, true, getMonitor());
		folder.create(true, true, getMonitor());

		// the deleted file should show up as a deleted member of folder
		df = folder.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("1.12", 1, df.length);
		assertEquals("1.13", folderAsFile, df[0]);

		df = folder.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("1.14", 2, df.length);
		List dfList = Arrays.asList(df);
		assertTrue("1.15", dfList.contains(file));
		assertTrue("1.16", dfList.contains(folderAsFile));
		
		df = folder.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("1.17", 2, df.length);
		dfList = Arrays.asList(df);
		assertTrue("1.18", dfList.contains(file));
		assertTrue("1.19", dfList.contains(folderAsFile));
		

	} catch (CoreException e) {
		fail("1.00", e);
	}
	
	// scrub the project
	try {
		project.delete(true, getMonitor());
		project.create(getMonitor());
		project.open(getMonitor());
		
		IFile[] df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("1.50", 0, df.length);
	} catch (CoreException e) {
		fail("1.51", e);
	}
	
	// test a bunch of deletes
	folder = project.getFolder("folder");
	IFile file1 = folder.getFile("file1.txt");
	IFile file2 = folder.getFile("file2.txt");
	IFolder folder2 = folder.getFolder("folder2");
	IFile file3 = folder2.getFile("file3.txt");
	try {
		// create and delete a file in a folder
		folder.create(true, true, getMonitor());
		folder2.create(true, true, getMonitor());
		file1.create(getRandomContents(), true, getMonitor());
		file2.create(getRandomContents(), true, getMonitor());
		file3.create(getRandomContents(), true, getMonitor());
		folder.delete(true, true, getMonitor());

		// under root
		IFile[] df = root.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("3.1", 0, df.length);

		df = root.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("3.2", 0, df.length);
		
		df = root.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("3.3", 3, df.length);
		List dfList = Arrays.asList(df);
		assertTrue("3.3.1", dfList.contains(file1));
		assertTrue("3.3.2", dfList.contains(file2));
		assertTrue("3.3.3", dfList.contains(file3));
		
		// under project
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("3.4", 0, df.length);
		
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("3.5", 0, df.length);
		
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("3.6", 3, df.length);
		dfList = Arrays.asList(df);
		assertTrue("3.6.1", dfList.contains(file1));
		assertTrue("3.6.2", dfList.contains(file2));
		assertTrue("3.6.3", dfList.contains(file3));
		
		// under folder
		df = folder.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("3.7", 0, df.length);

		df = folder.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("3.8", 2, df.length);
		
		df = folder.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("3.9", 3, df.length);
		
		// under folder2
		df = folder2.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("3.10", 0, df.length);
		
		df = folder2.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("3.11", 1, df.length);
		
		df = folder2.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("3.12", 1, df.length);
		
	} catch (CoreException e) {
		fail("3.00", e);
	}
		
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("3.5", e);
	}
	
	// once the project is gone, so is all the history for that project	
	try {
		// under root
		IFile[] df = root.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("4.1", 0, df.length);

		df = root.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("4.2", 0, df.length);
		
		df = root.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("4.3", 0, df.length);
		

		// under project
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("4.4", 0, df.length);
		
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("4.5", 0, df.length);
		
		df = project.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("4.6", 0, df.length);
		
		// under folder
		df = folder.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("4.7", 0, df.length);

		df = folder.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("4.8", 0, df.length);
		
		df = folder.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("4.9", 0, df.length);
		
		// under folder2
		df = folder2.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
		assertEquals("4.10", 0, df.length);
		
		df = folder2.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
		assertEquals("4.11", 0, df.length);
		
		df = folder2.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
		assertEquals("4.12", 0, df.length);
		
	} catch (CoreException e) {
		fail("4.00", e);
	}
}

public void testMove() {
	// create common objects
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}
	
	// test file
	IFile file = project.getFile("file.txt");
	IFile file2 = project.getFile("moved file.txt");
	try {
		file.create(getRandomContents(), true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		file.move(file2.getFullPath(), true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("1.0", 3, states.length);
	} catch (CoreException e) {
		fail("1.20", e);
	}

	// test folder
	IFolder folder = project.getFolder("folder");
	IFolder folder2 = project.getFolder("moved folder");
	file = folder.getFile("file.txt");
	try {
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		folder.move(folder2.getFullPath(), true, true, getMonitor());
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("2.0", 3, states.length);
	} catch (CoreException e) {
		fail("2.20", e);
	}
	
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("20.0", e);
	}
}
}
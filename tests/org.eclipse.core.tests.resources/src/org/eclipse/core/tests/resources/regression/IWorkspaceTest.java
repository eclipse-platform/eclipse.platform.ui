package org.eclipse.core.tests.resources.regression;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import junit.framework.*;

public class IWorkspaceTest extends EclipseWorkspaceTest {
public IWorkspaceTest() {
}
public IWorkspaceTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(IWorkspaceTest.class);
}
protected void tearDown() throws Exception {
	super.tearDown();
	getWorkspace().getRoot().delete(true, null);
}
/**
 * 1GDKIHD: ITPCORE:WINNT - API - IWorkspace.move needs to keep history
 */
public void testMultiMove_1GDKIHD() {
	// create common objects
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}
	
	// test file (force = true)
	IFile file = project.getFile("file.txt");
	IFolder folder = project.getFolder("folder");
	try {
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		getWorkspace().move(new IFile[] {file}, folder.getFullPath(), true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("1.0", 3, states.length);
		getWorkspace().delete(project.members(), true, getMonitor());
		project.clearHistory(getMonitor());
	} catch (CoreException e) {
		fail("1.20", e);
	}

	// test file (force = false)
	try {
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		getWorkspace().move(new IFile[] {file}, folder.getFullPath(), false, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("2.0", 3, states.length);
		getWorkspace().delete(project.members(), true, getMonitor());
		project.clearHistory(getMonitor());
	} catch (CoreException e) {
		fail("2.20", e);
	}

	// test folder (force = true)
	IFolder folder2 = project.getFolder("second folder");
	file = folder.getFile("file.txt");
	try {
		folder.create(true, true, getMonitor());
		folder2.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		getWorkspace().move(new IResource[] {folder}, folder2.getFullPath(), true, getMonitor());
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("3.0", 3, states.length);
		getWorkspace().delete(project.members(), true, getMonitor());
		project.clearHistory(getMonitor());
	} catch (CoreException e) {
		fail("3.20", e);
	}

	// test folder (force = false)
	try {
		folder.create(true, true, getMonitor());
		folder2.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		getWorkspace().move(new IResource[] {folder}, folder2.getFullPath(), false, getMonitor());
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("4.0", 3, states.length);
		getWorkspace().delete(project.members(), true, getMonitor());
		project.clearHistory(getMonitor());
	} catch (CoreException e) {
		fail("4.20", e);
	}
	
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("20.0", e);
	}
}
/**
 * 1GDGRIZ: ITPCORE:WINNT - API - IWorkspace.delete needs to keep history
 */
public void testMultiDelete_1GDGRIZ() {
	// create common objects
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}
	
	// test file (force = true)
	IFile file = project.getFile("file.txt");
	try {
		file.create(getRandomContents(), true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		getWorkspace().delete(new IFile[] {file}, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("1.0", 3, states.length);
		getWorkspace().delete(project.members(), true, getMonitor());
		project.clearHistory(getMonitor());
	} catch (CoreException e) {
		fail("1.20", e);
	}

	// test file (force = false)
	try {
		file.create(getRandomContents(), true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		getWorkspace().delete(new IFile[] {file}, false, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("2.0", 3, states.length);
		getWorkspace().delete(project.members(), true, getMonitor());
		project.clearHistory(getMonitor());
	} catch (CoreException e) {
		fail("2.20", e);
	}

	// test folder (force = true)
	IFolder folder = project.getFolder("folder");
	file = folder.getFile("file.txt");
	try {
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		getWorkspace().delete(new IResource[] {folder}, true, getMonitor());
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("3.0", 3, states.length);
		getWorkspace().delete(project.members(), true, getMonitor());
		project.clearHistory(getMonitor());
	} catch (CoreException e) {
		fail("3.20", e);
	}

	// test folder (force = false)
	try {
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		file.setContents(getRandomContents(), true, true, getMonitor());
		getWorkspace().delete(new IResource[] {folder}, false, getMonitor());
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		IFileState[] states = file.getHistory(getMonitor());
		assertEquals("4.0", 3, states.length);
		getWorkspace().delete(project.members(), true, getMonitor());
		project.clearHistory(getMonitor());
	} catch (CoreException e) {
		fail("4.20", e);
	}
	
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("20.0", e);
	}
}
}

package org.eclipse.core.tests.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.properties.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Policy;
import java.io.*;
import java.util.*;
import junit.framework.*;
//
public class FileSystemResourceManagerTest extends LocalStoreTest implements ICoreConstants {
public FileSystemResourceManagerTest() {
	super();
}
public FileSystemResourceManagerTest(String name) {
	super(name);
}
public String[] defineHierarchy() {
	return new String[] {
		"/Folder1/",
		"/Folder1/File1",
		"/Folder1/Folder2/",
		"/Folder1/Folder2/File2",
		"/Folder1/Folder2/Folder3/"
	};
}
public static Test suite() {
	return new TestSuite(FileSystemResourceManagerTest.class);
}
/**
 * Scenario:
 *		1 solution
 * 		3 projects
 */
public void testContainerFor() throws Throwable {

	/* test null parameter */
	try {
		getLocalManager().containerFor(null);
		fail("1.1");
	} catch (RuntimeException e) {
	}

	/* test normal conditions under default mapping */

	// project/target
	Path path = new Path("target");
	IFolder folder = projects[0].getFolder(path);
	IPath location = projects[0].getLocation().append(path);
	IFolder testFolder = (IFolder) getLocalManager().containerFor(location);
	assertTrue("2.1", folder.equals(testFolder));

	// project/folder/target
	path = new Path("folder/target");
	folder = projects[0].getFolder(path);
	location = projects[0].getLocation().append(path);
	testFolder = (IFolder) getLocalManager().containerFor(location);
	assertTrue("2.2", folder.equals(testFolder));

	// project/folder/folder/target
	path = new Path("folder/folder/target");
	folder = projects[0].getFolder(path);
	location = projects[0].getLocation().append(path);
	testFolder = (IFolder) getLocalManager().containerFor(location);
	assertTrue("2.3", folder.equals(testFolder));

	/* inexisting location */
	testFolder = (IFolder) getLocalManager().containerFor(new Path("../this/path/must/not/exist"));
	assertTrue("3.1", testFolder == null);
}
/**
 * this test should move to FileTest
 */
public void testCreateFile() throws Throwable {
	/* initialize common objects */
	IProject project = projects[0];
	IFile file = project.getFile("foo");
	/* common contents */
	String originalContent = "this string should not be equal the other";

	/* create file with flag false */
	try {
		file.create(getContents(originalContent), false, null);
	} catch (CoreException e) {
		fail("1.1", e);
	}
	assertTrue("1.2", file.exists());
	assertTrue("1.3", file.isLocal(IResource.DEPTH_ZERO));
	assertEquals("1.4", file.getLocation().toFile().lastModified(), ((Resource) file).getResourceInfo(false, false).getLocalSyncInfo());
	try {
		assertTrue("1.5", compareContent(getContents(originalContent), getLocalManager().read((org.eclipse.core.internal.resources.File) file, true, null)));
	} catch (CoreException e) {
		fail("1.6", e);
	}
}
/**
 * Scenario:
 *		1 solution
 * 		3 projects
 */
public void testFileFor() throws Throwable {

	/* test null parameter */
	try {
		getLocalManager().fileFor(null);
		fail("1.1");
	} catch (RuntimeException e) {
	}

	/* test normal conditions under default mapping */

	// project/file
	Path path = new Path("file");
	IFile file = projects[0].getFile(path);
	IPath location = projects[0].getLocation().append(path);
	IFile testFile = getLocalManager().fileFor(location);
	assertTrue("2.1", file.equals(testFile));

	// project/folder/file
	path = new Path("folder/file");
	file = projects[0].getFile(path);
	location = projects[0].getLocation().append(path);
	testFile = getLocalManager().fileFor(location);
	assertTrue("2.2", file.equals(testFile));

	// project/folder/folder/file
	path = new Path("folder/folder/file");
	file = projects[0].getFile(path);
	location = projects[0].getLocation().append(path);
	testFile = getLocalManager().fileFor(location);
	assertTrue("2.3", file.equals(testFile));

	/* inexisting location */
	testFile = getLocalManager().fileFor(new Path("../this/path/must/not/exist"));
	assertTrue("7.1", testFile == null);
}
public void testIsLocal() throws Throwable {
	/* initialize common objects */
	final IProject project = projects[0];

	// create resources
	IResource[] resources = buildResources(project, defineHierarchy());
	ensureExistsInWorkspace(resources, true);
	ensureDoesNotExistInFileSystem(resources);

	// exists
	assertTrue("1.0", project.isLocal(IResource.DEPTH_INFINITE)); // test

	// test the depth parameter
	final IFolder folder = project.getFolder("Folder1");
	IWorkspaceRunnable operation = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {

			IResource[] members = folder.members();

			for (int i = 0; i < members.length; i++) {
				((Resource) members[i]).getResourceInfo(false, true).clear(M_LOCAL_EXISTS);
			}
		}
	};
	getWorkspace().run(operation, null);
	assertTrue("2.0", project.isLocal(IResource.DEPTH_ONE));
	assertTrue("3.0", folder.isLocal(IResource.DEPTH_ZERO));
	assertTrue("4.0", !folder.isLocal(IResource.DEPTH_INFINITE));

	// remove the trash
	ensureDoesNotExistInWorkspace(project);
}
/**
 * Scenario:
 *		1 solution
 * 		3 projects
 */
public void testLocationFor() throws Throwable {

	/* test project */
	IPath location = projects[0].getLocation();
	assertTrue("2.1", location.equals(getLocalManager().locationFor((Project) projects[0])));
	assertTrue("2.2", location.equals(Platform.getLocation().append(projects[0].getName())));
}
/**
 * Scenario:
 *		1 solution
 * 		3 projects
 */
public void testResourceFor() throws Throwable {

	/* test null parameter */
	try {
		getLocalManager().resourceFor(null);
		fail("1.1");
	} catch (RuntimeException e) {
	} catch (CoreException e) {
		fail("1.2", e);
	}

	/* test normal conditions under default mapping */

	// project/target (file)
	IPath path = new Path("target");
	IFile file = projects[0].getFile(path);
	ensureExistsInFileSystem(file);
	IPath location = projects[0].getLocation().append(path);
	IResource testResource = getLocalManager().resourceFor(location);
	assertTrue("2.0", file.equals(testResource));
	ensureDoesNotExistInFileSystem(file);

	// project/target (folder)
	path = new Path("target");
	IFolder folder = projects[0].getFolder(path);
	ensureExistsInFileSystem(folder);
	location = projects[0].getLocation().append(path);
	testResource = getLocalManager().resourceFor(location);
	assertTrue("2.1", folder.equals(testResource));

	// project/folder/target
	path = new Path("folder/target");
	folder = projects[0].getFolder(path);
	ensureExistsInWorkspace(folder.getParent(), true);
	ensureExistsInWorkspace(folder, true);
	location = projects[0].getLocation().append(path);
	testResource = getLocalManager().resourceFor(location);
	assertTrue("2.2", folder.equals(testResource));

	// project/folder/folder/target
	path = new Path("folder/folder/target");
	folder = projects[0].getFolder(path);
	ensureExistsInFileSystem(folder);
	location = projects[0].getLocation().append(path);
	testResource = getLocalManager().resourceFor(location);
	assertTrue("2.3", folder.equals(testResource));

	/* give a location of a project */
	location = projects[0].getLocation();
	testResource = getLocalManager().resourceFor(location);
	assertTrue("5.1", projects[0].equals(testResource));

	/* test resource that does not exist neither in the workspace or file system */
	location = new Path("../this/path/must/not/exist/");
	assertTrue("6.1", getLocalManager().resourceFor(location) == null);

	/* test inexisting resource with a valid path */
	location = projects[0].getLocation().append("resource/must/not/exist");
	testResource = getLocalManager().resourceFor(location);
	assertTrue("7.1", testResource == null);
}
/**
 * Scenario:
 *		1 solution
 * 		3 projects
 */
public void testResourcePathFor() throws Throwable {

	///* test null parameter */
	//try {
		//getLocalManager().resourcePathFor(null);
		//fail("1.1");
	//} catch (RuntimeException e) {
	//} catch (CoreException e) {
		//fail("1.2", e);
	//}

	///* test normal conditions under default mapping */

	//// project/file
	//Path path = new Path("file");
	//IFile file = projects[0].getFile(path);
	//IPath location = projects[0].getLocation().append(path);
	//IPath testPath = getLocalManager().resourcePathFor(location);
	//assert("2.1", file.getFullPath().equals(testPath));

	//// project/folder/file
	//path = new Path("folder/file");
	//file = projects[0].getFile(path);
	//location = projects[0].getLocation().append(path);
	//testPath = getLocalManager().resourcePathFor(location);
	//assert("2.2", file.getFullPath().equals(testPath));

	//// project/folder/folder/file
	//path = new Path("folder/folder/file");
	//file = projects[0].getFile(path);
	//location = projects[0].getLocation().append(path);
	//testPath = getLocalManager().resourcePathFor(location);
	//assert("2.3", file.getFullPath().equals(testPath));
}
public void testSynchronizeProject() throws Throwable {
	/* test DEPTH parameter */
	/* DEPTH_ZERO */
	IFile file = projects[0].getFile("file");
	ensureExistsInFileSystem(file);
	projects[0].refreshLocal(IResource.DEPTH_ZERO, null);
	assertTrue("1.1", !file.exists());
	/* DEPTH_ONE */
	IFolder folder = projects[0].getFolder("folder");
	IFolder subfolder = folder.getFolder("subfolder");
	IFile subfile = folder.getFile("subfile");
	ensureExistsInFileSystem(folder);
	ensureExistsInFileSystem(subfolder);
	ensureExistsInFileSystem(subfile);
	projects[0].refreshLocal(IResource.DEPTH_ONE, null);
	assertTrue("2.1", file.exists());
	assertTrue("2.2", folder.exists());
	assertTrue("2.3", !subfolder.exists());
	assertTrue("2.4", !subfile.exists());
	/* DEPTH_INFINITE */
	projects[0].refreshLocal(IResource.DEPTH_INFINITE, null);
	assertTrue("3.1", file.exists());
	assertTrue("3.2", folder.exists());
	assertTrue("3.3", subfolder.exists());
	assertTrue("3.4", subfile.exists());

	/* closed project */
	/* #open synchronizes project */
	file = projects[0].getFile("closed");
	projects[0].close(null);
	ensureExistsInFileSystem(file);
	projects[0].open(null);
	assertTrue("4.1", file.exists());
	projects[0].refreshLocal(IResource.DEPTH_INFINITE, null);
	assertTrue("4.2", file.exists());

	/* refreshing an inexisting project should do nothing */
	getWorkspace().getRoot().getProject("inexistingProject").refreshLocal(IResource.DEPTH_INFINITE, null);
}
public void testWriteFile() {
	/* initialize common objects */
	IProject project = projects[0];
	IFile file = project.getFile("foo");
	ensureExistsInWorkspace(file, true);
	/* common contents */
	String originalContent = "this string should not be equal the other";
	String anotherContent = "and this string should not... well, you know...";
	InputStream original;
	InputStream another;

	/* write file for the first time */
	original = getContents(originalContent);
	try {
		write(file, original, true, null);
	} catch (CoreException e) {
		fail("1.0", e);
	}
	original = getContents(originalContent);
	try {
		assertTrue("1.1", compareContent(original, getLocalManager().read((org.eclipse.core.internal.resources.File) file, true, null)));
	} catch (CoreException e) {
		fail("1.2", e);
	}

	/* test the overwrite parameter (false) */
	another = getContents(anotherContent);
	try {
		write(file, another, false, null);
	} catch (CoreException e) {
		fail("2.0", e);
	}
	another = getContents(anotherContent);
	try {
		assertTrue("2.1", compareContent(another, getLocalManager().read((org.eclipse.core.internal.resources.File) file, true, null)));
	} catch (CoreException e) {
		fail("2.2", e);
	}

	/* test the overwrite parameter (true) */
	original = getContents(originalContent);
	try {
		write(file, original, true, null);
	} catch (CoreException e) {
		fail("3.0", e);
	}
	original = getContents(originalContent);
	try {
		assertTrue("3.1", compareContent(original, getLocalManager().read((org.eclipse.core.internal.resources.File) file, true, null)));
	} catch (CoreException e) {
		fail("3.2", e);
	}

	/* test the overwrite parameter (false) */
	try {
		Thread.sleep(sleepTime); // it's a shame doing this but the timestamp resolution is not good enough
	} catch (InterruptedException e) {}
	ensureExistsInFileSystem(file);
	another = getContents(anotherContent);
	try {
		write(file, another, false, null);
		fail("4.0", null);
	} catch (CoreException e) {
	}
	try {
		file.setContents(another, false, false, null);
		fail("4.3");
	} catch (CoreException e) {
	}
	try {
		file.setContents(another, true, false, null);
	} catch (CoreException e) {
		fail("4.4", e);
	}

	/* test the overwrite parameter (false) */
	try {
		Thread.sleep(sleepTime); // it's a shame doing this but the timestamp resolution is not good enough
	} catch (InterruptedException e) {}
	ensureDoesNotExistInFileSystem(file);
	another = getContents(anotherContent);
	try {
		write(file, another, false, null);
		fail("5.0");
	} catch (CoreException e) {
	}

	/* remove trash */
	ensureDoesNotExistInWorkspace(project);
}
public void testWriteFolder() throws Throwable {
	/* initialize common objects */
	IProject project = projects[0];
	IFolder folder = project.getFolder("foo");
	ensureExistsInWorkspace(folder, true);
	boolean ok;

	/* existing file on destination */
	ensureDoesNotExistInFileSystem(folder);
	IFile file = project.getFile("foo");
	ensureExistsInFileSystem(file);
	/* force = true */
	ok = false;
	try {
		write(folder, true, null);
	} catch (CoreException e) {
		ok = true;
	}
	assertTrue("1.1", ok);
	/* force = false */
	ok = false;
	try {
		write(folder, false, null);
	} catch (CoreException e) {
		ok = true;
	}
	assertTrue("1.2", ok);
	ensureDoesNotExistInFileSystem(file);

	/* existing folder on destination */
	ensureExistsInFileSystem(folder);
	/* force = true */
	write(folder, true, null);
	assertTrue("2.1", folder.getLocation().toFile().isDirectory());
	/* force = false */
	ok = false;
	try {
		write(folder, false, null);
	} catch (CoreException e) {
		ok = true;
	}
	assertTrue("2.2", ok);
	ensureDoesNotExistInFileSystem(folder);

	/* inexisting resource on destination */
	/* force = true */
	write(folder, true, null);
	assertTrue("3.1", folder.getLocation().toFile().isDirectory());
	ensureDoesNotExistInFileSystem(folder);
	/* force = false */
	write(folder, false, null);
	assertTrue("3.1", folder.getLocation().toFile().isDirectory());
}
/**
 * 1.2 - write a project with default local location
 * 2.1 - delete project's local location
 */
public void testWriteProject() throws Throwable {
	/* initialize common objects */
	final IProject project = projects[0];

	/* remove project location and write */
	IPath location = getLocalManager().locationFor((Project) project);
	ensureDoesNotExistInFileSystem(project);
	assertTrue("2.1", !location.toFile().isDirectory());
	//write project in a runnable, otherwise tree will be locked
	getWorkspace().run(new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			getLocalManager().write((Project) project, IResource.FORCE);
		}
	}, null);
	assertTrue("2.2", location.toFile().isDirectory());
	long lastModified = getLocalManager().getDescriptionLocationFor(project).toFile().lastModified();
	assertEquals("2.3", lastModified, ((Resource) project).getResourceInfo(false, false).getLocalSyncInfo());
}
protected void write(final IFile file, final InputStream contents, final boolean force, IProgressMonitor monitor) throws CoreException {
	IWorkspaceRunnable operation = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			getLocalManager().write(file, getLocalManager().locationFor(file), contents, force, false, false, null);
		}
	};
	getWorkspace().run(operation, null);
}
protected void write(final IFolder folder, final boolean force, IProgressMonitor monitor) throws CoreException {
	IWorkspaceRunnable operation = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			getLocalManager().write((Folder) folder, force, null);
		}
	};
	getWorkspace().run(operation, null);
}
}

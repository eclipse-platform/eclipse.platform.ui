/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.localstore;

import java.io.File;
import java.util.Date;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
import org.eclipse.core.internal.localstore.FileSystemStore;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;

public class FileSystemStoreTest extends LocalStoreTest {
	public FileSystemStoreTest() {
		super();
	}

	public FileSystemStoreTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(FileSystemStoreTest.class);
	}

	/**
	 * Basically this is a test for the Windows Platform.
	 */
	public void testCopyAcrossVolumes() throws Throwable {

		/* test if we are in the adequate environment */
		if (!new File("c:\\").exists() || !new File("d:\\").exists())
			return;

		/* initialize commom objetcs */
		FileSystemStore store = new FileSystemStore();

		/* build scenario */
		// create source root folder
		File tempC = new File("c:\\temp");
		tempC.mkdirs();
		assertTrue("1.1", tempC.isDirectory());
		// create destination root folder
		File tempD = new File("d:\\temp");
		tempD.mkdirs();
		assertTrue("1.2", tempD.isDirectory());
		// create tree
		File target = new File(tempC, "target");
		Workspace.clear(target);
		target.mkdirs();
		createTree(getTree(target));

		/* c:\temp\target -> d:\temp\target */
		File destination = new File(tempD, "target");
		store.copy(target, destination, IResource.DEPTH_INFINITE, null);
		assertTrue("3.1", verifyTree(getTree(destination)));
		Workspace.clear(destination);

		/* c:\temp\target -> d:\temp\copy of target */
		destination = new File(tempD, "copy of target");
		store.copy(target, destination, IResource.DEPTH_INFINITE, null);
		assertTrue("4.1", verifyTree(getTree(destination)));
		Workspace.clear(destination);

		/* c:\temp\target -> d:\temp\target (but the destination is already a file */
		destination = new File(tempD, "target");
		String anotherContent = "nothing..................gnihton";
		createFile(destination, anotherContent);
		assertTrue("5.1", destination.isFile());
		boolean ok = false;
		try {
			store.copy(target, destination, IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			ok = true;
		}
		assertTrue("5.2", ok);
		assertTrue("5.3", !verifyTree(getTree(destination)));
		Workspace.clear(destination);

		/* c:\temp\target -> d:\temp\target (but the destination is already a folder */
		destination = new File(tempD, "target");
		destination.mkdirs();
		assertTrue("6.1", destination.isDirectory());
		store.copy(target, destination, IResource.DEPTH_INFINITE, null);
		assertTrue("6.2", verifyTree(getTree(destination)));
		Workspace.clear(destination);

		/* remove trash */
		Workspace.clear(target);
	}

	public void testCopyDirectory() throws Throwable {
		/* initialize commom objetcs */
		FileSystemStore store = new FileSystemStore();

		/* build scenario */
		File temp = getWorkspace().getRoot().getLocation().append("temp").toFile();
		temp.mkdirs();
		assertTrue("1.1", temp.isDirectory());
		// create tree
		File target = new File(temp, "target");
		Workspace.clear(target);
		target.mkdirs();
		createTree(getTree(target));

		/* temp\target -> temp\copy of target */
		File copyOfTarget = new File(temp, "copy of target");
		store.copy(target, copyOfTarget, IResource.DEPTH_INFINITE, null);
		assertTrue("2.1", verifyTree(getTree(copyOfTarget)));
		Workspace.clear(copyOfTarget);

		/* remove trash */
		Workspace.clear(target);
	}

	public void testCopyFile() throws Throwable {
		/* initialize commom objetcs */
		FileSystemStore store = new FileSystemStore();

		/* build scenario */
		File temp = getWorkspace().getRoot().getLocation().append("temp").toFile();
		temp.mkdirs();
		assertTrue("1.1", temp.isDirectory());
		// create target
		String content = "this is just a simple content \n to a simple file \n to test a 'simple' copy";
		File target = new File(temp, "target");
		Workspace.clear(target);
		createFile(target, content);
		assertTrue("1.3", target.exists());
		assertTrue("1.4", compareContent(getContents(content), store.read(target)));

		/* temp\target -> temp\copy of target */
		File copyOfTarget = new File(temp, "copy of target");
		store.copy(target, copyOfTarget, IResource.DEPTH_INFINITE, null);
		assertTrue("2.1", compareContent(getContents(content), store.read(copyOfTarget)));
		copyOfTarget.delete();

		// We need to know whether or not we can unset the read-only flag
		// in order to perform this part of the test.
		if (CoreFileSystemLibrary.usingNatives()) {
			/* make source read-only and try the copy temp\target -> temp\copy of target */
			copyOfTarget = new File(temp, "copy of target");
			CoreFileSystemLibrary.setReadOnly(target.getAbsolutePath(), true);
			store.copy(target, copyOfTarget, IResource.DEPTH_INFINITE, null);
			assertTrue("3.1", compareContent(getContents(content), store.read(copyOfTarget)));
			// reset readonly flag for cleanup
			CoreFileSystemLibrary.setReadOnly(copyOfTarget.getAbsolutePath(), false);
			copyOfTarget.delete();
			// reset the readonly flag for cleanup
			CoreFileSystemLibrary.setReadOnly(target.getAbsolutePath(), false);
			Workspace.clear(target);
		}

		/* copy a big file to test progress monitor */
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 1000; i++)
			sb.append("asdjhasldhaslkfjhasldkfjhasdlkfjhasdlfkjhasdflkjhsdaf");
		File bigFile = new File(temp, "bigFile");
		createFile(bigFile, sb.toString());
		assertTrue("7.1", bigFile.exists());
		assertTrue("7.2", compareContent(getContents(sb.toString()), store.read(bigFile)));
		File destination = new File(temp, "copy of bigFile");
		//IProgressMonitor monitor = new LoggingProgressMonitor(System.out);
		IProgressMonitor monitor = getMonitor();
		store.copy(bigFile, destination, IResource.DEPTH_INFINITE, monitor);
		assertTrue("7.3", compareContent(getContents(sb.toString()), store.read(destination)));
		destination.delete();

		/* remove trash */
		Workspace.clear(temp);
	}

	/**
	 * Basically this is a test for the Windows Platform.
	 */
	public void testCopyFileAcrossVolumes() throws Throwable {
		/* test if we are in the adequate environment */
		if (!new File("c:\\").exists() || !new File("d:\\").exists())
			return;

		/* initialize commom objetcs */
		FileSystemStore store = new FileSystemStore();

		/* build scenario */
		// create source
		File tempC = new File("c:\\temp");
		tempC.mkdirs();
		assertTrue("1.1", tempC.isDirectory());
		// create destination
		File tempD = new File("d:\\temp");
		tempD.mkdirs();
		assertTrue("1.2", tempD.isDirectory());
		// create target
		String content = "this is just a simple content \n to a simple file \n to test a 'simple' copy";
		File target = new File(tempC, "target");
		Workspace.clear(target);
		createFile(target, content);
		assertTrue("1.3", target.exists());
		assertTrue("1.4", compareContent(getContents(content), store.read(target)));

		/* c:\temp\target -> d:\temp\target */
		File destination = new File(tempD, "target");
		store.copy(target, destination, IResource.DEPTH_INFINITE, null);
		assertTrue("3.1", compareContent(getContents(content), store.read(destination)));
		destination.delete();

		/* c:\temp\target -> d:\temp\copy of target */
		destination = new File(tempD, "copy of target");
		store.copy(target, destination, IResource.DEPTH_INFINITE, null);
		assertTrue("4.1", compareContent(getContents(content), store.read(destination)));
		destination.delete();

		/* c:\temp\target -> d:\temp\target (but the destination is already a file */
		destination = new File(tempD, "target");
		String anotherContent = "nothing..................gnihton";
		createFile(destination, anotherContent);
		assertTrue("5.1", destination.isFile());
		store.copy(target, destination, IResource.DEPTH_INFINITE, null);
		assertTrue("5.2", compareContent(getContents(content), store.read(destination)));
		destination.delete();

		/* c:\temp\target -> d:\temp\target (but the destination is already a folder */
		destination = new File(tempD, "target");
		destination.delete();
		destination.mkdirs();
		assertTrue("6.1", destination.isDirectory());
		boolean ok = false;
		try {
			store.copy(target, destination, IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			/* test if the input stream inside the copy method was closed */
			assertTrue("6.1.1", target.delete());
			createFile(target, content);
			ok = true;
		}
		assertTrue("6.2", ok);
		assertTrue("6.3", destination.isDirectory());
		destination.delete();

		/* remove trash */
		target.delete();
	}

	public void testGetStat() {
		/* evaluate test environment */
		IPath root = getWorkspace().getRoot().getLocation().append("" + new Date().getTime());
		File temp = root.toFile();
		if (!temp.exists()) {
			temp.mkdirs();
			if (!temp.exists())
				return;
		}

		/* create common objects */
		File target = new File(temp, "target");
		long stat;

		/* test stat with an inexisting file */
		stat = CoreFileSystemLibrary.getStat(target.getAbsolutePath());
		assertTrue("1.0", stat == 0);

		/* test stat with an existing folder */
		target.mkdirs();
		stat = CoreFileSystemLibrary.getStat(target.getAbsolutePath());
		assertTrue("2.0", stat != 0);

		/* remove trash */
		target.delete();
		temp.delete();
	}

	public void testMove() throws Throwable {
		/* initialize commom objetcs */
		FileSystemStore store = new FileSystemStore();

		/* build scenario */
		File tempC = getWorkspace().getRoot().getLocation().append("temp").toFile();
		tempC.mkdirs();
		assertTrue("1.1", tempC.isDirectory());
		// create target file
		File target = new File(tempC, "target");
		String content = "just a content.....tnetnoc a tsuj";
		createFile(target, content);
		assertTrue("1.3", target.exists());
		// create target tree
		File tree = new File(tempC, "tree");
		Workspace.clear(tree);
		tree.mkdirs();
		createTree(getTree(tree));

		/* rename file */
		File destination = new File(tempC, "destination");
		store.move(target, destination, true, null);
		assertTrue("2.1", destination.isFile());
		assertTrue("2.2", !target.exists());
		store.move(destination, target, true, null);
		assertTrue("2.3", target.isFile());
		assertTrue("2.4", !destination.exists());

		/* rename file (but destination is already a file) */
		String anotherContent = "another content";
		createFile(destination, anotherContent);
		boolean ok = false;
		try {
			store.move(target, destination, false, null);
		} catch (CoreException e) {
			ok = true;
		}
		assertTrue("3.1", ok);
		assertTrue("3.2", target.isFile());
		destination.delete();
		assertTrue("3.3", !destination.exists());

		/* rename file (but destination is already a folder) */
		store.writeFolder(destination);
		try {
			store.move(target, destination, false, null);
		} catch (CoreException e) {
			ok = true;
		}
		assertTrue("4.1", ok);
		assertTrue("4.2", target.isFile());
		destination.delete();
		assertTrue("4.3", !destination.exists());

		/* rename folder */
		destination = new File(tempC, "destination");
		store.move(tree, destination, true, null);
		assertTrue("6.1", verifyTree(getTree(destination)));
		assertTrue("6.2", !tree.exists());
		store.move(destination, tree, true, null);
		assertTrue("6.3", verifyTree(getTree(tree)));
		assertTrue("6.4", !destination.exists());

		/* remove trash */
		Workspace.clear(target);
		Workspace.clear(tree);
	}

	public void testMoveAcrossVolumes() throws Throwable {
		/* test if we are in the adequate environment */
		if (!new File("c:\\").exists() || !new File("d:\\").exists())
			return;

		/* initialize commom objetcs */
		FileSystemStore store = new FileSystemStore();

		/* build scenario */
		// create source
		File tempC = new File("c:\\temp");
		tempC.mkdirs();
		assertTrue("1.1", tempC.isDirectory());
		// create destination
		File tempD = new File("d:\\temp");
		tempD.mkdirs();
		assertTrue("1.2", tempD.isDirectory());
		// create target file
		File target = new File(tempC, "target");
		String content = "just a content.....tnetnoc a tsuj";
		createFile(target, content);
		assertTrue("1.3", target.exists());
		// create target tree
		File tree = new File(tempC, "tree");
		Workspace.clear(tree);
		tree.mkdirs();
		createTree(getTree(tree));

		/* move file across volumes */
		File destination = new File(tempD, "target");
		store.move(target, destination, true, null);
		assertTrue("5.1", destination.isFile());
		assertTrue("5.2", !target.exists());
		store.move(destination, target, true, null);
		assertTrue("5.3", target.isFile());
		assertTrue("5.4", !destination.exists());

		/* move folder across volumes */
		destination = new File(tempD, "target");
		store.move(tree, destination, true, null);
		assertTrue("9.1", verifyTree(getTree(destination)));
		assertTrue("9.2", !tree.exists());
		store.move(destination, tree, true, null);
		assertTrue("9.3", verifyTree(getTree(tree)));
		assertTrue("9.4", !destination.exists());

		/* remove trash */
		Workspace.clear(target);
		Workspace.clear(tree);
	}

	public void testReadOnly() {
		// We need to know whether or not we can unset the read-only flag
		// in order to perform this test.
		if (!CoreFileSystemLibrary.usingNatives())
			return;

		/* evaluate test environment */
		IPath root = getWorkspace().getRoot().getLocation().append("" + new Date().getTime());
		IPath target = root.append("target");
		File temp = root.toFile();
		if (!temp.exists()) {
			temp.mkdirs();
			if (!temp.exists())
				return;
		}
		try {
			createFileInFileSystem(target);
		} catch (CoreException e) {
			fail("0.0", e);
		}

		// file
		String fileName = target.toOSString();
		assertTrue("1.0", !CoreFileSystemLibrary.isReadOnly(fileName));
		assertTrue("1.1", CoreFileSystemLibrary.setReadOnly(fileName, true));
		assertTrue("1.2", CoreFileSystemLibrary.isReadOnly(fileName));
		assertTrue("1.3", CoreFileSystemLibrary.setReadOnly(fileName, false));
		assertTrue("1.4", !CoreFileSystemLibrary.isReadOnly(fileName));

		// folder
		fileName = root.toOSString();
		assertTrue("2.0", !CoreFileSystemLibrary.isReadOnly(fileName));
		assertTrue("2.1", CoreFileSystemLibrary.setReadOnly(fileName, true));
		assertTrue("2.2", CoreFileSystemLibrary.isReadOnly(fileName));
		assertTrue("2.3", CoreFileSystemLibrary.setReadOnly(fileName, false));
		assertTrue("2.4", !CoreFileSystemLibrary.isReadOnly(fileName));

		/* remove trash */
		assertTrue("3.98", target.toFile().delete());
		assertTrue("3.99", temp.delete());
	}
}
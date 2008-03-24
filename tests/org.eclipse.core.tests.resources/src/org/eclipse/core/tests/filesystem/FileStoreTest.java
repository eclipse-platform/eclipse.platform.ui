/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.filesystem;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.internal.localstore.LocalStoreTest;

/**
 * Basic tests for the IFileStore API
 */
public class FileStoreTest extends LocalStoreTest {
	public static Test suite() {
		return new TestSuite(FileStoreTest.class);
	}

	public FileStoreTest() {
		super();
	}

	public FileStoreTest(String name) {
		super(name);
	}

	private IFileStore createDir(IFileStore store, boolean clear) throws CoreException {
		if (clear && store.fetchInfo().exists())
			store.delete(EFS.NONE, null);
		store.mkdir(EFS.NONE, null);
		IFileInfo info = store.fetchInfo();
		assertTrue("createDir.1", info.exists());
		assertTrue("createDir.1", info.isDirectory());
		return store;
	}

	private IFileStore createDir(String string, boolean clear) throws CoreException {
		return createDir(EFS.getFileSystem(EFS.SCHEME_FILE).getStore(new Path(string)), clear);
	}
	
	/**
	 * Tests behaviour of IFileStore#fetchInfo when underlying file system
	 * throws exceptions.
	 */
	public void testBrokenFetchInfo() {
		IFileStore broken = null;
		try {
			broken = EFS.getStore(new URI("broken://a/b/c"));
		} catch (CoreException e) {
			fail("0.98", e);
		} catch (URISyntaxException e) {
			fail("0.99", e);
		}
		//no-arg fetch info should return non-existent file
		IFileInfo info = broken.fetchInfo();
		assertTrue("1.0", !info.exists());
		
		//two-arg fetchInfo should throw exception
		try {
			info = broken.fetchInfo(EFS.NONE, getMonitor());
			fail("2.0");
		} catch (CoreException e) {
			//expected
		}
	}

	/**
	 * Basically this is a test for the Windows Platform.
	 */
	public void testCopyAcrossVolumes() throws Throwable {

		/* test if we are in the adequate environment */
		if (!new File("c:\\").exists() || !new File("d:\\").exists())
			return;

		/* build scenario */
		// create source root folder
		IFileStore tempC = createDir("c:/temp", false);
		// create destination root folder
		IFileStore tempD = createDir("d:/temp", false);
		// create tree
		IFileStore target = tempC.getChild("target");
		createDir(target, true);
		createTree(getTree(target));

		/* c:\temp\target -> d:\temp\target */
		IFileStore destination = tempD.getChild("target");
		target.copy(destination, EFS.NONE, null);
		assertTrue("3.1", verifyTree(getTree(destination)));
		destination.delete(EFS.NONE, null);

		/* c:\temp\target -> d:\temp\copy of target */
		destination = tempD.getChild("copy of target");
		target.copy(destination, EFS.NONE, null);
		assertTrue("4.1", verifyTree(getTree(destination)));
		destination.delete(EFS.NONE, null);

		/* c:\temp\target -> d:\temp\target (but the destination is already a file) */
		destination = tempD.getChild("target");
		String anotherContent = "nothing..................gnihton";
		createFile(destination, anotherContent);
		assertTrue("5.1", !destination.fetchInfo().isDirectory());
		try {
			target.copy(destination, EFS.NONE, null);
			fail("5.2");
		} catch (CoreException e) {
			//should fail
		}
		assertTrue("5.3", !verifyTree(getTree(destination)));
		destination.delete(EFS.NONE, null);

		/* c:\temp\target -> d:\temp\target (but the destination is already a folder */
		destination = tempD.getChild("target");
		createDir(destination, true);
		target.copy(destination, EFS.NONE, null);
		assertTrue("6.2", verifyTree(getTree(destination)));
		destination.delete(EFS.NONE, null);

		/* remove trash */
		target.delete(EFS.NONE, null);
	}

	public void testCopyDirectory() throws Throwable {
		/* build scenario */
		IFileStore temp = EFS.getFileSystem(EFS.SCHEME_FILE).getStore(getWorkspace().getRoot().getLocation().append("temp"));
		temp.mkdir(EFS.NONE, null);
		assertTrue("1.1", temp.fetchInfo().isDirectory());
		// create tree
		IFileStore target = temp.getChild("target");
		target.delete(EFS.NONE, null);
		createTree(getTree(target));

		/* temp\target -> temp\copy of target */
		IFileStore copyOfTarget = temp.getChild("copy of target");
		target.copy(copyOfTarget, EFS.NONE, null);
		assertTrue("2.1", verifyTree(getTree(copyOfTarget)));

		/* remove trash */
		target.delete(EFS.NONE, null);
		copyOfTarget.delete(EFS.NONE, null);
	}

	public void testCopyDirectoryParentMissing() throws Throwable {
		IFileStore parent = getTempStore();
		IFileStore child = parent.getChild("child");
		IFileStore existing = getTempStore();
		createFile(existing, getRandomString());
		//try to copy when parent of destination does not exist
		try {
			existing.copy(child, EFS.NONE, getMonitor());
			fail("1.0");
		} catch (CoreException e) {
			//should fail
		}
		//destination should not exist
		assertTrue("1.1", !child.fetchInfo().exists());
	}

	public void testCopyFile() throws Throwable {
		/* build scenario */
		IFileStore temp = createDir(getWorkspace().getRoot().getLocation().append("temp").toString(), true);
		// create target
		String content = "this is just a simple content \n to a simple file \n to test a 'simple' copy";
		IFileStore target = temp.getChild("target");
		target.delete(EFS.NONE, null);
		createFile(target, content);
		assertTrue("1.3", target.fetchInfo().exists());
		assertTrue("1.4", compareContent(getContents(content), target.openInputStream(EFS.NONE, null)));

		/* temp\target -> temp\copy of target */
		IFileStore copyOfTarget = temp.getChild("copy of target");
		target.copy(copyOfTarget, IResource.DEPTH_INFINITE, null);
		assertTrue("2.1", compareContent(getContents(content), copyOfTarget.openInputStream(EFS.NONE, null)));
		copyOfTarget.delete(EFS.NONE, null);

		// We need to know whether or not we can unset the read-only flag
		// in order to perform this part of the test.
		if (isReadOnlySupported()) {
			/* make source read-only and try the copy temp\target -> temp\copy of target */
			copyOfTarget = temp.getChild("copy of target");
			setReadOnly(target, true);

			target.copy(copyOfTarget, IResource.DEPTH_INFINITE, null);
			assertTrue("3.1", compareContent(getContents(content), copyOfTarget.openInputStream(EFS.NONE, null)));
			// reset read only flag for cleanup
			setReadOnly(copyOfTarget, false);
			copyOfTarget.delete(EFS.NONE, null);
			// reset the read only flag for cleanup
			setReadOnly(target, false);
			target.delete(EFS.NONE, null);
		}

		/* copy a big file to test progress monitor */
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 1000; i++)
			sb.append("asdjhasldhaslkfjhasldkfjhasdlkfjhasdlfkjhasdflkjhsdaf");
		IFileStore bigFile = temp.getChild("bigFile");
		createFile(bigFile, sb.toString());
		assertTrue("7.1", bigFile.fetchInfo().exists());
		assertTrue("7.2", compareContent(getContents(sb.toString()), bigFile.openInputStream(EFS.NONE, null)));
		IFileStore destination = temp.getChild("copy of bigFile");
		//IProgressMonitor monitor = new LoggingProgressMonitor(System.out);
		IProgressMonitor monitor = getMonitor();
		bigFile.copy(destination, EFS.NONE, monitor);
		assertTrue("7.3", compareContent(getContents(sb.toString()), destination.openInputStream(EFS.NONE, null)));
		destination.delete(EFS.NONE, null);

		/* take out the trash */
		temp.delete(EFS.NONE, null);
	}

	/**
	 * Basically this is a test for the Windows Platform.
	 */
	public void testCopyFileAcrossVolumes() throws Throwable {
		/* test if we are in the adequate environment */
		if (!new File("c:\\").exists() || !new File("d:\\").exists())
			return;

		/* build scenario */
		// create source
		IFileStore tempC = createDir("c:\\temp", false);
		// create destination
		IFileStore tempD = createDir("d:\\temp", false);
		// create target
		String content = "this is just a simple content \n to a simple file \n to test a 'simple' copy";
		IFileStore target = tempC.getChild("target");
		target.delete(EFS.NONE, null);
		createFile(target, content);
		assertTrue("1.3", target.fetchInfo().exists());
		assertTrue("1.4", compareContent(getContents(content), target.openInputStream(EFS.NONE, null)));

		/* c:\temp\target -> d:\temp\target */
		IFileStore destination = tempD.getChild("target");
		target.copy(destination, IResource.DEPTH_INFINITE, null);
		assertTrue("3.1", compareContent(getContents(content), destination.openInputStream(EFS.NONE, null)));
		destination.delete(EFS.NONE, null);

		/* c:\temp\target -> d:\temp\copy of target */
		destination = tempD.getChild("copy of target");
		target.copy(destination, IResource.DEPTH_INFINITE, null);
		assertTrue("4.1", compareContent(getContents(content), destination.openInputStream(EFS.NONE, null)));
		destination.delete(EFS.NONE, null);

		/* c:\temp\target -> d:\temp\target (but the destination is already a file */
		destination = tempD.getChild("target");
		String anotherContent = "nothing..................gnihton";
		createFile(destination, anotherContent);
		assertTrue("5.1", !destination.fetchInfo().isDirectory());
		target.copy(destination, IResource.DEPTH_INFINITE, null);
		assertTrue("5.2", compareContent(getContents(content), destination.openInputStream(EFS.NONE, null)));
		destination.delete(EFS.NONE, null);

		/* c:\temp\target -> d:\temp\target (but the destination is already a folder */
		destination = tempD.getChild("target");
		createDir(destination, true);
		assertTrue("6.1", destination.fetchInfo().isDirectory());
		boolean ok = false;
		try {
			target.copy(destination, EFS.NONE, null);
		} catch (CoreException e) {
			/* test if the input stream inside the copy method was closed */
			target.delete(EFS.NONE, null);
			createFile(target, content);
			ok = true;
		}
		assertTrue("6.2", ok);
		assertTrue("6.3", destination.fetchInfo().isDirectory());
		destination.delete(EFS.NONE, null);

		/* remove trash */
		target.delete(EFS.NONE, null);
	}

	public void testGetLength() throws Exception {
		// evaluate test environment 
		IPath root = getWorkspace().getRoot().getLocation().append("" + new Date().getTime());
		IFileStore temp = createDir(root.toString(), true);
		try {
			// create common objects
			IFileStore target = temp.getChild("target");

			// test non-existent file 
			assertEquals("1.0", EFS.NONE, target.fetchInfo().getLength());

			// create empty file
			target.openOutputStream(EFS.NONE, null).close();
			assertEquals("1.0", 0, target.fetchInfo().getLength());

			// add a byte
			OutputStream out = target.openOutputStream(EFS.NONE, null);
			out.write(5);
			out.close();
			assertEquals("1.0", 1, target.fetchInfo().getLength());
		} finally {
			/* remove trash */
			temp.delete(EFS.NONE, null);
		}

	}

	public void testGetStat() throws CoreException {
		/* evaluate test environment */
		IPath root = getWorkspace().getRoot().getLocation().append("" + new Date().getTime());
		IFileStore temp = createDir(root.toString(), true);

		/* create common objects */
		IFileStore target = temp.getChild("target");
		long stat;

		/* test stat with an non-existing file */
		stat = target.fetchInfo().getLastModified();
		assertEquals("1.0", EFS.NONE, stat);

		/* test stat with an existing folder */
		createDir(target, true);
		stat = target.fetchInfo().getLastModified();
		assertTrue("2.0", EFS.NONE != stat);

		/* remove trash */
		temp.delete(EFS.NONE, null);
	}

	public void testMove() throws Throwable {
		/* build scenario */
		IFileStore tempC = createDir(getWorkspace().getRoot().getLocation().append("temp").toString(), true);
		// create target file
		IFileStore target = tempC.getChild("target");
		String content = "just a content.....tnetnoc a tsuj";
		createFile(target, content);
		assertTrue("1.3", target.fetchInfo().exists());
		// create target tree
		IFileStore tree = tempC.getChild("tree");
		createDir(tree, true);
		createTree(getTree(tree));

		/* rename file */
		IFileStore destination = tempC.getChild("destination");
		target.move(destination, EFS.NONE, null);
		assertTrue("2.1", !destination.fetchInfo().isDirectory());
		assertTrue("2.2", !target.fetchInfo().exists());
		destination.move(target, EFS.NONE, null);
		assertTrue("2.3", !target.fetchInfo().isDirectory());
		assertTrue("2.4", !destination.fetchInfo().exists());

		/* rename file (but destination is already a file) */
		String anotherContent = "another content";
		createFile(destination, anotherContent);
		boolean ok = false;
		try {
			target.move(destination, EFS.NONE, null);
		} catch (CoreException e) {
			ok = true;
		}
		assertTrue("3.1", ok);
		assertTrue("3.2", !target.fetchInfo().isDirectory());
		destination.delete(EFS.NONE, null);
		assertTrue("3.3", !destination.fetchInfo().exists());

		/* rename file (but destination is already a folder) */
		createDir(destination, true);
		try {
			target.move(destination, EFS.NONE, null);
		} catch (CoreException e) {
			ok = true;
		}
		assertTrue("4.1", ok);
		assertTrue("4.2", !target.fetchInfo().isDirectory());
		destination.delete(EFS.NONE, null);
		assertTrue("4.3", !destination.fetchInfo().exists());

		/* rename folder */
		destination = tempC.getChild("destination");
		tree.move(destination, EFS.NONE, null);
		assertTrue("6.1", verifyTree(getTree(destination)));
		assertTrue("6.2", !tree.fetchInfo().exists());
		destination.move(tree, EFS.NONE, null);
		assertTrue("6.3", verifyTree(getTree(tree)));
		assertTrue("6.4", !destination.fetchInfo().exists());

		/* remove trash */
		target.delete(EFS.NONE, null);
		tree.delete(EFS.NONE, null);
	}

	public void testMoveAcrossVolumes() throws Throwable {
		/* test if we are in the adequate environment */
		if (!new File("c:\\").exists() || !new File("d:\\").exists())
			return;

		/* build scenario */
		// create source
		IFileStore tempC = createDir("c:\\temp", false);
		// create destination
		IFileStore tempD = createDir("d:\\temp", false);
		// create target file
		IFileStore target = tempC.getChild("target");
		String content = "just a content.....tnetnoc a tsuj";
		createFile(target, content);
		assertTrue("1.3", target.fetchInfo().exists());
		// create target tree
		IFileStore tree = tempC.getChild("tree");
		createDir(tree, true);
		createTree(getTree(tree));

		/* move file across volumes */
		IFileStore destination = tempD.getChild("target");
		target.move(destination, EFS.NONE, null);
		assertTrue("5.1", !destination.fetchInfo().isDirectory());
		assertTrue("5.2", !target.fetchInfo().exists());
		destination.move(target, EFS.NONE, null);
		assertTrue("5.3", !target.fetchInfo().isDirectory());
		assertTrue("5.4", !destination.fetchInfo().exists());

		/* move folder across volumes */
		destination = tempD.getChild("target");
		tree.move(destination, EFS.NONE, null);
		assertTrue("9.1", verifyTree(getTree(destination)));
		assertTrue("9.2", !tree.fetchInfo().exists());
		destination.move(tree, EFS.NONE, null);
		assertTrue("9.3", verifyTree(getTree(tree)));
		assertTrue("9.4", !destination.fetchInfo().exists());

		/* remove trash */
		target.delete(EFS.NONE, null);
		tree.delete(EFS.NONE, null);
	}

	public void testMoveDirectoryParentMissing() throws Throwable {
		IFileStore parent = getTempStore();
		IFileStore child = parent.getChild("child");
		IFileStore existing = getTempStore();
		createFile(existing, getRandomString());
		//try to move when parent of destination does not exist
		try {
			existing.move(child, EFS.NONE, getMonitor());
			fail("1.0");
		} catch (CoreException e) {
			//should fail
		}
		//destination should not exist
		assertTrue("1.1", !child.fetchInfo().exists());
	}

	/**
	 * Tests public API method {@link IFileStore#putInfo(IFileInfo, int, IProgressMonitor)}.
	 */
	public void testPutInfo() {
		IFileStore nonExisting = getTempStore();

		//assert that modifying a non-existing store fails
		IFileInfo info = nonExisting.fetchInfo();
		info.setLastModified(System.currentTimeMillis());
		try {
			nonExisting.putInfo(info, EFS.SET_LAST_MODIFIED, getMonitor());
			fail("1.0");
		} catch (CoreException e) {
			//expected
		}
		info = nonExisting.fetchInfo();
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, false);
		try {
			nonExisting.putInfo(info, EFS.SET_ATTRIBUTES, getMonitor());
			fail("1.1");
		} catch (CoreException e) {
			//expected
		}

	}

	public void testReadOnly() throws CoreException {
		// We need to know whether or not we can unset the read-only flag
		// in order to perform this test.
		if (!isReadOnlySupported())
			return;

		IPath root = getWorkspace().getRoot().getLocation().append("" + new Date().getTime());
		IFileStore targetFolder = createDir(root.toString(), true);
		IFileStore targetFile = targetFolder.getChild("targetFile");
		createFileInFileSystem(targetFile);

		// file
		assertTrue("1.0", !targetFile.fetchInfo().getAttribute(EFS.ATTRIBUTE_READ_ONLY));
		setReadOnly(targetFile, true);
		assertTrue("1.2", targetFile.fetchInfo().getAttribute(EFS.ATTRIBUTE_READ_ONLY));
		setReadOnly(targetFile, false);
		assertTrue("1.4", !targetFile.fetchInfo().getAttribute(EFS.ATTRIBUTE_READ_ONLY));

		// folder
		assertTrue("2.0", !targetFolder.fetchInfo().getAttribute(EFS.ATTRIBUTE_READ_ONLY));
		setReadOnly(targetFolder, true);
		assertTrue("2.2", targetFolder.fetchInfo().getAttribute(EFS.ATTRIBUTE_READ_ONLY));
		setReadOnly(targetFolder, false);
		assertTrue("2.4", !targetFolder.fetchInfo().getAttribute(EFS.ATTRIBUTE_READ_ONLY));

		/* remove trash */
		targetFolder.delete(EFS.NONE, null);
	}
	
	public void testGetFileStore() throws Exception {
		// create files
		File file = getTempDir().append("test.txt").toFile();
		file.createNewFile();
		assertTrue("1.0", file.exists());
		
		IFileStore tempStore = createDir(getTempDir().append("temp").toString(), true);
		createDir(getTempDir().append("temp/temp2").toString(), true);
		
		file = getTempDir().append("temp/temp2/test.txt").toFile();
		file.createNewFile();
		assertTrue("2.0", file.exists());

		// check the parent reference
		IPath relativePath = new Path("../test.txt");

		IFileStore relativeStore = tempStore.getFileStore(relativePath);
		assertNotNull("3.0", relativeStore);
		IFileInfo info = relativeStore.fetchInfo();
		assertNotNull("4.0", info);
		assertTrue("5.0", info.exists());
		
		// check the parent and self reference
		relativePath = new Path(".././test.txt");
		
		relativeStore = tempStore.getFileStore(relativePath);
		assertNotNull("6.0", relativeStore);
		info = relativeStore.fetchInfo();
		assertNotNull("7.0", info);
		assertTrue("8.0", info.exists());
		
		// check the a path with no parent and self references
		relativePath = new Path("temp2/test.txt");
		
		relativeStore = tempStore.getFileStore(relativePath);
		assertNotNull("9.0", relativeStore);
		info = relativeStore.fetchInfo();
		assertNotNull("10.0", info);
		assertTrue("11.0", info.exists());
	}
}

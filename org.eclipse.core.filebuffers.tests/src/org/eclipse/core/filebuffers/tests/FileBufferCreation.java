/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;



public class FileBufferCreation {

	private final static String CONTENT1= "This is the content of the workspace file.";
	private final static String CONTENT2= "This is the content of the link target.";
	private final static String CONTENT3= "This is the content of the external file.";
	private final static String CONTENT4= "This is the content of a file in a linked folder.";


	private IProject fProject;


	@Before
	public void setUp() throws Exception {
		fProject= ResourceHelper.createProject("project");
	}

	@After
	public void tearDown() throws Exception {
		ResourceHelper.deleteProject("project");
	}

	private IPath createLinkedFile(String linkedFileName, String linkedFileTarget) throws CoreException {
		IFile linkedFile= ResourceHelper.createLinkedFile(fProject, new Path(linkedFileName), FileBuffersTestPlugin.getDefault(), new Path(linkedFileTarget));
		return linkedFile != null ? linkedFile.getFullPath() : null;
	}

	private IPath createLinkedFolder(String linkedFolderName, String linkedFolderTarget) throws CoreException {
		IFolder linkedFolder= ResourceHelper.createLinkedFolder(fProject, new Path(linkedFolderName), FileBuffersTestPlugin.getDefault(), new Path(linkedFolderTarget));
		return linkedFolder != null ? linkedFolder.getFullPath() : null;
	}

	/*
	 * Tests the creation of file buffer for an existing file.
	 */
	@Test
	public void test1() throws Exception {
		IFolder folder= ResourceHelper.createFolder("project/folderA/folderB/");
		IFile file= ResourceHelper.createFile(folder, "file", CONTENT1);
		IPath path= file.getFullPath();
		assertNotNull(path);

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, LocationKind.NORMALIZE, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path, LocationKind.NORMALIZE);
		assertNotNull(buffer);

		IDocument document= buffer.getDocument();
		assertNotNull(document);
		assertEquals(CONTENT1, document.get());

		assertSame(buffer, manager.getTextFileBuffer(document));

		manager.disconnect(path, LocationKind.NORMALIZE, null);
		assertNull(manager.getTextFileBuffer(path, LocationKind.NORMALIZE));
	}

	/*
	 * Tests the creation of file buffer for an existing file.
	 */
	@Test
	public void test1_IFileStore() throws Exception {
		IFolder folder= ResourceHelper.createFolder("project/folderA/folderB/");
		IFile file= ResourceHelper.createFile(folder, "file", CONTENT1);
		IPath path= file.getFullPath();
		assertNotNull(path);
		IFileStore fileStore= EFS.getLocalFileSystem().getStore(file.getLocation());

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connectFileStore(fileStore, null);
		ITextFileBuffer buffer= manager.getFileStoreTextFileBuffer(fileStore);
		assertNotNull(buffer);

		IDocument document= buffer.getDocument();
		assertNotNull(document);
		assertEquals(CONTENT1, document.get());

		assertSame(buffer, manager.getTextFileBuffer(document));

		manager.disconnectFileStore(fileStore, null);
		assertNull(manager.getFileStoreTextFileBuffer(fileStore));
	}

	/*
	 * Tests that two different paths pointing to the same physical resource
	 * result in the same shared file buffer.
	 */
	@Test
	public void test2() throws Exception {

		IFolder folder= ResourceHelper.createFolder("project/folderA/folderB/");
		IFile file= ResourceHelper.createFile(folder, "file", CONTENT1);
		IPath path1= file.getFullPath();
		assertNotNull(path1);

		IPath path2= ResourcesPlugin.getWorkspace().getRoot().getLocation();
		path2= path2.append(path1.makeAbsolute());

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path1, LocationKind.NORMALIZE, null);
		ITextFileBuffer buffer1= manager.getTextFileBuffer(path1, LocationKind.NORMALIZE);
		assertNotNull(buffer1);

		ITextFileBuffer buffer2= manager.getTextFileBuffer(path2, LocationKind.NORMALIZE);
		assertNotNull(buffer2);

		manager.connect(path2, LocationKind.NORMALIZE, null);
		buffer2= manager.getTextFileBuffer(path2, LocationKind.NORMALIZE);
		assertNotNull(buffer2);

		IDocument document1= buffer1.getDocument();
		assertNotNull(document1);
		assertEquals(CONTENT1, document1.get());
		assertSame(buffer1, manager.getTextFileBuffer(document1));

		IDocument document2= buffer2.getDocument();
		assertNotNull(document2);
		assertEquals(CONTENT1, document2.get());
		assertSame(buffer2, manager.getTextFileBuffer(document2));

		try {
			document1.replace(0, document1.getLength(), CONTENT3);
		} catch (BadLocationException x) {
			assertTrue(false);
		}

		assertEquals(CONTENT3, document2.get());

		manager.disconnect(path1, LocationKind.NORMALIZE, null);
		assertNotNull(manager.getTextFileBuffer(path1, LocationKind.NORMALIZE));
		assertNotNull(manager.getTextFileBuffer(path2, LocationKind.NORMALIZE));

		manager.disconnect(path2, LocationKind.NORMALIZE, null);
		assertNull(manager.getTextFileBuffer(path1, LocationKind.NORMALIZE));
		assertNull(manager.getTextFileBuffer(path2, LocationKind.NORMALIZE));
	}

	/*
	 * Tests the creation of a file buffer for a linked file.
	 */
	@Test
	public void test3_1() throws Exception {
		IPath path= createLinkedFile("file", "testResources/LinkedFileTarget");
		assertNotNull(path);

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, LocationKind.NORMALIZE, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path, LocationKind.NORMALIZE);
		Assert.assertNotNull(buffer);

		IDocument document= buffer.getDocument();
		Assert.assertNotNull(document);
		Assert.assertTrue(CONTENT2.equals(document.get()));
		assertSame(buffer, manager.getTextFileBuffer(document));

		manager.disconnect(path, LocationKind.NORMALIZE, null);
		assertNull(manager.getTextFileBuffer(path, LocationKind.NORMALIZE));
	}

	/*
	 * Tests the creation of a file buffer for a file in a linked folder.
	 */
	@Test
	public void test3_2() throws Exception {
		IPath path= createLinkedFolder("linkedFolder", "testResources/linkedFolderTarget");
		assertNotNull(path);
		path= path.append("FileInLinkedFolder");

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, LocationKind.NORMALIZE, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path, LocationKind.NORMALIZE);
		Assert.assertNotNull(buffer);

		IDocument document= buffer.getDocument();
		Assert.assertNotNull(document);
		Assert.assertTrue(CONTENT4.equals(document.get()));
		assertSame(buffer, manager.getTextFileBuffer(document));

		manager.disconnect(path, LocationKind.NORMALIZE, null);
		assertNull(manager.getTextFileBuffer(path, LocationKind.NORMALIZE));
	}

	/*
	 * Tests that two different files linked to the same target file result
	 * in two different, independent file buffers.
	 */
	@Test
	public void test4() throws Exception {

		IPath path1= createLinkedFile("file1", "testResources/LinkedFileTarget");
		assertNotNull(path1);
		IPath path2= createLinkedFile("file2", "testResources/LinkedFileTarget");
		assertNotNull(path2);

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path1, LocationKind.NORMALIZE, null);
		ITextFileBuffer buffer1= manager.getTextFileBuffer(path1, LocationKind.NORMALIZE);
		assertNotNull(buffer1);
		manager.connect(path2, LocationKind.NORMALIZE, null);
		ITextFileBuffer buffer2= manager.getTextFileBuffer(path2, LocationKind.NORMALIZE);
		assertNotNull(buffer2);

		IDocument document1= buffer1.getDocument();
		assertNotNull(document1);
		assertSame(buffer1, manager.getTextFileBuffer(document1));

		IDocument document2= buffer2.getDocument();
		assertNotNull(document2);
		assertSame(buffer2, manager.getTextFileBuffer(document2));

		assertEquals(document1.get(), document2.get());
		assertEquals(CONTENT2, document1.get());

		try {
			document1.replace(0, document1.getLength(), CONTENT1);
		} catch (BadLocationException x) {
			Assert.assertFalse(false);
		}

		assertFalse(document1.get().equals(document2.get()));

		manager.disconnect(path1, LocationKind.NORMALIZE, null);
		assertNull(manager.getTextFileBuffer(path1, LocationKind.NORMALIZE));
		assertNotNull(manager.getTextFileBuffer(path2, LocationKind.NORMALIZE));
		manager.disconnect(path2, LocationKind.NORMALIZE, null);
		assertNull(manager.getTextFileBuffer(path2, LocationKind.NORMALIZE));
	}

	/*
	 * Tests the creation of a file buffer for an external file.
	 */
	@Test
	public void test5() throws Exception {
		File externalFile= FileTool.getFileInPlugin(FileBuffersTestPlugin.getDefault(), new Path("testResources/ExternalFile"));
		assertNotNull(externalFile);
		IPath path= new Path(externalFile.getAbsolutePath());

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, LocationKind.NORMALIZE, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path, LocationKind.NORMALIZE);
		assertNotNull(buffer);

		IDocument document= buffer.getDocument();
		assertNotNull(document);
		assertTrue(CONTENT3.equals(document.get()));
		assertSame(buffer, manager.getTextFileBuffer(document));

		manager.disconnect(path, LocationKind.NORMALIZE, null);
		assertNull(manager.getTextFileBuffer(path, LocationKind.NORMALIZE));
	}

	/*
	 * Tests that a workspace file linked to an external file and the external file result
	 * in two different, independent file buffers.
	 */
	@Test
	public void test6() throws Exception {

		IPath path1= createLinkedFile("file1", "testResources/ExternalFile");
		assertNotNull(path1);

		File externalFile= FileTool.getFileInPlugin(FileBuffersTestPlugin.getDefault(), new Path("testResources/ExternalFile"));
		assertNotNull(externalFile);
		IPath path2= new Path(externalFile.getAbsolutePath());

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path1, LocationKind.NORMALIZE, null);
		ITextFileBuffer buffer1= manager.getTextFileBuffer(path1, LocationKind.NORMALIZE);
		assertNotNull(buffer1);
		manager.connect(path2, LocationKind.NORMALIZE, null);
		ITextFileBuffer buffer2= manager.getTextFileBuffer(path2, LocationKind.NORMALIZE);
		assertNotNull(buffer2);

		IDocument document1= buffer1.getDocument();
		assertNotNull(document1);
		assertSame(buffer1, manager.getTextFileBuffer(document1));

		IDocument document2= buffer2.getDocument();
		assertNotNull(document2);
		assertSame(buffer2, manager.getTextFileBuffer(document2));

		assertEquals(document1.get(), document2.get());
		assertEquals(CONTENT3, document1.get());

		try {
			document1.replace(0, document1.getLength(), CONTENT1);
		} catch (BadLocationException x) {
			Assert.assertFalse(false);
		}

		assertFalse(document1.get().equals(document2.get()));

		manager.disconnect(path1, LocationKind.NORMALIZE, null);
		assertNull(manager.getTextFileBuffer(path1, LocationKind.NORMALIZE));
		manager.disconnect(path2, LocationKind.NORMALIZE, null);
		assertNull(manager.getTextFileBuffer(path2, LocationKind.NORMALIZE));
	}

	/*
	 * Tests the creation of a file buffer for a non-existing file.
	 */
	@Test
	public void test7() throws Exception {
		IPath path= FileBuffersTestPlugin.getDefault().getStateLocation();
		path= path.append("NonExistingFile");

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, LocationKind.NORMALIZE, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path, LocationKind.NORMALIZE);
		Assert.assertNotNull(buffer);

		IDocument document= buffer.getDocument();
		Assert.assertNotNull(document);
		Assert.assertTrue("".equals(document.get()));
		assertSame(buffer, manager.getTextFileBuffer(document));

		manager.disconnect(path, LocationKind.NORMALIZE, null);
		assertNull(manager.getTextFileBuffer(path, LocationKind.NORMALIZE));
	}

	/*
	 * Tests the creation of file buffer for an existing file.
	 */
	@Test
	public void test1_IFILE() throws Exception {
		IFolder folder= ResourceHelper.createFolder("project/folderA/folderB/");
		IFile file= ResourceHelper.createFile(folder, "file", CONTENT1);
		IPath path= file.getFullPath();
		assertNotNull(path);

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, LocationKind.IFILE, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path, LocationKind.IFILE);
		assertNotNull(buffer);

		IDocument document= buffer.getDocument();
		assertNotNull(document);
		assertEquals(CONTENT1, document.get());

		assertSame(buffer, manager.getTextFileBuffer(document));

		manager.disconnect(path, LocationKind.IFILE, null);
		assertNull(manager.getTextFileBuffer(path, LocationKind.IFILE));
	}

	/*
	 * Tests that two different paths pointing to the same physical resource
	 * result in the same shared file buffer.
	 */
	@Test
	public void test2_new() throws Exception {

		IFolder folder= ResourceHelper.createFolder("project/folderA/folderB/");
		IFile file= ResourceHelper.createFile(folder, "file", CONTENT1);
		IPath path1= file.getFullPath();
		assertNotNull(path1);

		IPath path2= ResourcesPlugin.getWorkspace().getRoot().getLocation();
		path2= path2.append(path1.makeAbsolute());

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path1, LocationKind.IFILE, null);
		ITextFileBuffer buffer1= manager.getTextFileBuffer(path1, LocationKind.IFILE);
		assertNotNull(buffer1);

		ITextFileBuffer buffer2= manager.getTextFileBuffer(path2, LocationKind.NORMALIZE);
		assertNotNull(buffer2);

		manager.connect(path2, LocationKind.NORMALIZE, null);
		buffer2= manager.getTextFileBuffer(path2, LocationKind.NORMALIZE);
		assertNotNull(buffer2);

		IDocument document1= buffer1.getDocument();
		assertNotNull(document1);
		assertEquals(CONTENT1, document1.get());
		assertSame(buffer1, manager.getTextFileBuffer(document1));

		IDocument document2= buffer2.getDocument();
		assertNotNull(document2);
		assertEquals(CONTENT1, document2.get());
		assertSame(buffer2, manager.getTextFileBuffer(document2));

		try {
			document1.replace(0, document1.getLength(), CONTENT3);
		} catch (BadLocationException x) {
			assertTrue(false);
		}

		assertEquals(CONTENT3, document2.get());

		manager.disconnect(path1, LocationKind.IFILE, null);
		assertNotNull(manager.getTextFileBuffer(path1, LocationKind.IFILE));
		assertNotNull(manager.getTextFileBuffer(path2, LocationKind.NORMALIZE));

		manager.disconnect(path2, LocationKind.NORMALIZE, null);
		assertNull(manager.getTextFileBuffer(path1, LocationKind.IFILE));
		assertNull(manager.getTextFileBuffer(path2, LocationKind.NORMALIZE));
	}

	/*
	 * Tests the creation of a file buffer for a linked file.
	 */
	@Test
	public void test3_1_IFILE() throws Exception {
		IPath path= createLinkedFile("file", "testResources/LinkedFileTarget");
		assertNotNull(path);

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, LocationKind.IFILE, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path, LocationKind.IFILE);
		Assert.assertNotNull(buffer);

		IDocument document= buffer.getDocument();
		Assert.assertNotNull(document);
		Assert.assertTrue(CONTENT2.equals(document.get()));
		assertSame(buffer, manager.getTextFileBuffer(document));

		manager.disconnect(path, LocationKind.IFILE, null);
		assertNull(manager.getTextFileBuffer(path, LocationKind.IFILE));
	}

	/*
	 * Tests the creation of a file buffer for a file in a linked folder.
	 */
	@Test
	public void test3_2_new() throws Exception {
		IPath path= createLinkedFolder("linkedFolder", "testResources/linkedFolderTarget");
		assertNotNull(path);
		path= path.append("FileInLinkedFolder");

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, LocationKind.IFILE, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path, LocationKind.IFILE);
		Assert.assertNotNull(buffer);

		IDocument document= buffer.getDocument();
		Assert.assertNotNull(document);
		Assert.assertTrue(CONTENT4.equals(document.get()));
		assertSame(buffer, manager.getTextFileBuffer(document));

		manager.disconnect(path, LocationKind.IFILE, null);
		assertNull(manager.getTextFileBuffer(path, LocationKind.IFILE));
	}

	/*
	 * Tests that two different files linked to the same target file result
	 * in two different, independent file buffers.
	 */
	@Test
	public void test4_IFILE() throws Exception {

		IPath path1= createLinkedFile("file1", "testResources/LinkedFileTarget");
		assertNotNull(path1);
		IPath path2= createLinkedFile("file2", "testResources/LinkedFileTarget");
		assertNotNull(path2);

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path1, LocationKind.IFILE, null);
		ITextFileBuffer buffer1= manager.getTextFileBuffer(path1, LocationKind.IFILE);
		assertNotNull(buffer1);
		manager.connect(path2, LocationKind.IFILE, null);
		ITextFileBuffer buffer2= manager.getTextFileBuffer(path2, LocationKind.IFILE);
		assertNotNull(buffer2);

		IDocument document1= buffer1.getDocument();
		assertNotNull(document1);
		assertSame(buffer1, manager.getTextFileBuffer(document1));

		IDocument document2= buffer2.getDocument();
		assertNotNull(document2);
		assertSame(buffer2, manager.getTextFileBuffer(document2));

		assertEquals(document1.get(), document2.get());
		assertEquals(CONTENT2, document1.get());

		try {
			document1.replace(0, document1.getLength(), CONTENT1);
		} catch (BadLocationException x) {
			Assert.assertFalse(false);
		}

		assertFalse(document1.get().equals(document2.get()));

		manager.disconnect(path1, LocationKind.IFILE, null);
		assertNull(manager.getTextFileBuffer(path1, LocationKind.IFILE));
		assertNotNull(manager.getTextFileBuffer(path2, LocationKind.IFILE));
		manager.disconnect(path2, LocationKind.IFILE, null);
		assertNull(manager.getTextFileBuffer(path2, LocationKind.IFILE));
	}

	/*
	 * Tests the creation of a file buffer for an external file.
	 */
	@Test
	public void test5_location() throws Exception {
		File externalFile= FileTool.getFileInPlugin(FileBuffersTestPlugin.getDefault(), new Path("testResources/ExternalFile"));
		assertNotNull(externalFile);
		IPath path= new Path(externalFile.getAbsolutePath());

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, LocationKind.LOCATION, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path, LocationKind.LOCATION);
		assertNotNull(buffer);

		IDocument document= buffer.getDocument();
		assertNotNull(document);
		assertTrue(CONTENT3.equals(document.get()));
		assertSame(buffer, manager.getTextFileBuffer(document));

		manager.disconnect(path, LocationKind.LOCATION, null);
		assertNull(manager.getTextFileBuffer(path, LocationKind.LOCATION));
	}

	/*
	 * Tests the creation of a file buffer for a non-existing file.
	 */
	@Test
	public void test7_location() throws Exception {
		IPath path= FileBuffersTestPlugin.getDefault().getStateLocation();
		path= path.append("NonExistingFile");

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, LocationKind.LOCATION, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path, LocationKind.LOCATION);
		Assert.assertNotNull(buffer);

		IDocument document= buffer.getDocument();
		Assert.assertNotNull(document);
		Assert.assertTrue("".equals(document.get()));
		assertSame(buffer, manager.getTextFileBuffer(document));

		manager.disconnect(path, LocationKind.LOCATION, null);
		assertNull(manager.getTextFileBuffer(path, LocationKind.LOCATION));
	}

	/*
	 * Tests the creation of a file buffer for a non-existing file.
	 */
	@Test
	public void test7_IFileStore() throws Exception {
		IPath path= FileBuffersTestPlugin.getDefault().getStateLocation();
		path= path.append("NonExistingFile");
		IFileStore fileStore= EFS.getLocalFileSystem().getStore(path);

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connectFileStore(fileStore, null);
		ITextFileBuffer buffer= manager.getFileStoreTextFileBuffer(fileStore);
		Assert.assertNotNull(buffer);

		IDocument document= buffer.getDocument();
		Assert.assertNotNull(document);
		Assert.assertTrue("".equals(document.get()));
		assertSame(buffer, manager.getTextFileBuffer(document));

		manager.disconnectFileStore(fileStore, null);
		assertNull(manager.getFileStoreTextFileBuffer(fileStore));
	}
}

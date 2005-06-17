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
package org.eclipse.core.filebuffers.tests;

import java.io.File;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;



public class FileBufferCreation extends TestCase {
	
	private final static String CONTENT1= "This is the content of the workspace file.";
	private final static String CONTENT2= "This is the content of the link target.";
	private final static String CONTENT3= "This is the content of the external file.";
	private final static String CONTENT4= "This is the content of a file in a linked folder.";
	
	
	private IProject fProject;
	
		
	public FileBufferCreation(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		fProject= ResourceHelper.createProject("project");
	}
	
	protected void tearDown() throws Exception {
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
	public void test1() throws Exception {
		IFolder folder= ResourceHelper.createFolder("project/folderA/folderB/");
		IFile file= ResourceHelper.createFile(folder, "file", CONTENT1);
		IPath path= file.getFullPath();
		assertNotNull(path);
		
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path);
		assertNotNull(buffer);
		
		IDocument document= buffer.getDocument();
		assertNotNull(document);
		assertEquals(CONTENT1, document.get());
		
		manager.disconnect(path, null);
		assertNull(manager.getTextFileBuffer(path));
	}
	
	/*
	 * Tests that two different paths pointing to the same physical resource
	 * result in the same shared file buffer.
	 */
	public void test2() throws Exception {
		
		IFolder folder= ResourceHelper.createFolder("project/folderA/folderB/");
		IFile file= ResourceHelper.createFile(folder, "file", CONTENT1);
		IPath path1= file.getFullPath();
		assertNotNull(path1);
		
		IPath path2= ResourcesPlugin.getWorkspace().getRoot().getLocation();
		path2= path2.append(path1.makeAbsolute());
		
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path1, null);
		ITextFileBuffer buffer1= manager.getTextFileBuffer(path1);
		assertNotNull(buffer1);
		
		ITextFileBuffer buffer2= manager.getTextFileBuffer(path2);
		assertNotNull(buffer2);
		
		manager.connect(path2, null);
		buffer2= manager.getTextFileBuffer(path2);
		assertNotNull(buffer2);
		
		IDocument document1= buffer1.getDocument();
		assertNotNull(document1);
		assertEquals(CONTENT1, document1.get());
		
		IDocument document2= buffer2.getDocument();
		assertNotNull(document2);
		assertEquals(CONTENT1, document2.get());
		
		try {
			document1.replace(0, document1.getLength(), CONTENT3);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
		
		assertEquals(CONTENT3, document2.get());
		
		manager.disconnect(path1, null);
		assertNotNull(manager.getTextFileBuffer(path1));
		assertNotNull(manager.getTextFileBuffer(path2));
		
		manager.disconnect(path2, null);
		assertNull(manager.getTextFileBuffer(path1));
		assertNull(manager.getTextFileBuffer(path2));
	}
	
	/*
	 * Tests the creation of a file buffer for a linked file.
	 */
	public void test3_1() throws Exception {
		IPath path= createLinkedFile("file", "testResources/LinkedFileTarget");
		assertNotNull(path);
		
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path);
		Assert.assertNotNull(buffer);
		
		IDocument document= buffer.getDocument();
		Assert.assertNotNull(document);
		Assert.assertTrue(CONTENT2.equals(document.get()));
		
		manager.disconnect(path, null);
		assertNull(manager.getTextFileBuffer(path));
	}
	
	/*
	 * Tests the creation of a file buffer for a file in a linked folder.
	 */
	public void test3_2() throws Exception {
		IPath path= createLinkedFolder("linkedFolder", "testResources/linkedFolderTarget");
		assertNotNull(path);
		path= path.append("FileInLinkedFolder");
		
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path);
		Assert.assertNotNull(buffer);
		
		IDocument document= buffer.getDocument();
		Assert.assertNotNull(document);
		Assert.assertTrue(CONTENT4.equals(document.get()));
		
		manager.disconnect(path, null);
		assertNull(manager.getTextFileBuffer(path));
	}
	
	/*
	 * Tests that two different files linked to the same target file result
	 * in two different, independent file buffers.
	 */
	public void test4() throws Exception {
		
		IPath path1= createLinkedFile("file1", "testResources/LinkedFileTarget");
		assertNotNull(path1);
		IPath path2= createLinkedFile("file2", "testResources/LinkedFileTarget");
		assertNotNull(path2);

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path1, null);
		ITextFileBuffer buffer1= manager.getTextFileBuffer(path1);
		assertNotNull(buffer1);
		manager.connect(path2, null);
		ITextFileBuffer buffer2= manager.getTextFileBuffer(path2);
		assertNotNull(buffer2);
		
		IDocument document1= buffer1.getDocument();
		assertNotNull(document1);
		IDocument document2= buffer2.getDocument();
		assertNotNull(document2);
		
		assertEquals(document1.get(), document2.get());
		assertEquals(CONTENT2, document1.get());
		
		try {
			document1.replace(0, document1.getLength(), CONTENT1);
		} catch (BadLocationException x) {
			Assert.assertFalse(false);
		}
		
		assertFalse(document1.get().equals(document2.get()));
		
		manager.disconnect(path1, null);
		assertNull(manager.getTextFileBuffer(path1));
		assertNotNull(manager.getTextFileBuffer(path2));
		manager.disconnect(path2, null);
		assertNull(manager.getTextFileBuffer(path2));
	}
	
	/*
	 * Tests the creation of a file buffer for an external file.
	 */
	public void test5() throws Exception {
		File externalFile= FileTool.getFileInPlugin(FileBuffersTestPlugin.getDefault(), new Path("testResources/ExternalFile"));
		assertNotNull(externalFile);
		IPath path= new Path(externalFile.getAbsolutePath());
		
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path);
		assertNotNull(buffer);
		
		IDocument document= buffer.getDocument();
		assertNotNull(document);
		assertTrue(CONTENT3.equals(document.get()));
		
		manager.disconnect(path, null);
		assertNull(manager.getTextFileBuffer(path));
	}
	
	/*
	 * Tests that a workspace file linked to an external file and the external file result
	 * in two different, independent file buffers.
	 */
	public void test6() throws Exception {
		
		IPath path1= createLinkedFile("file1", "testResources/ExternalFile");
		assertNotNull(path1);
		
		File externalFile= FileTool.getFileInPlugin(FileBuffersTestPlugin.getDefault(), new Path("testResources/ExternalFile"));
		assertNotNull(externalFile);
		IPath path2= new Path(externalFile.getAbsolutePath());
		
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path1, null);
		ITextFileBuffer buffer1= manager.getTextFileBuffer(path1);
		assertNotNull(buffer1);
		manager.connect(path2, null);
		ITextFileBuffer buffer2= manager.getTextFileBuffer(path2);
		assertNotNull(buffer2);
		
		IDocument document1= buffer1.getDocument();
		assertNotNull(document1);
		IDocument document2= buffer2.getDocument();
		assertNotNull(document2);
		
		assertEquals(document1.get(), document2.get());
		assertEquals(CONTENT3, document1.get());
		
		try {
			document1.replace(0, document1.getLength(), CONTENT1);
		} catch (BadLocationException x) {
			Assert.assertFalse(false);
		}
		
		assertFalse(document1.get().equals(document2.get()));
		
		manager.disconnect(path1, null);
		assertNull(manager.getTextFileBuffer(path1));
		manager.disconnect(path2, null);
		assertNull(manager.getTextFileBuffer(path2));
	}
	
	/*
	 * Tests the creation of a file buffer for a non-existing file. 
	 */
	public void test7() throws Exception {
		IPath path= FileBuffersTestPlugin.getDefault().getStateLocation();
		path= path.append("NonExistingFile");
				
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(path, null);
		ITextFileBuffer buffer= manager.getTextFileBuffer(path);
		Assert.assertNotNull(buffer);
		
		IDocument document= buffer.getDocument();
		Assert.assertNotNull(document);
		Assert.assertTrue("".equals(document.get()));
		
		manager.disconnect(path, null);
		assertNull(manager.getTextFileBuffer(path));
	}
}

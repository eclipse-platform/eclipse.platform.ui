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
package org.eclipse.core.filebuffers.tests;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.jface.text.IDocument;

/**
 * FileBufferFunctions
 */
public abstract class FileBufferFunctions extends TestCase {
	
	private IProject fProject;
	private ITextFileBufferManager fManager;
	private IPath fPath;
	
	
	protected abstract IPath createPath(IProject project) throws Exception;
	
	protected abstract void markReadOnly() throws Exception;
	
	protected abstract boolean isStateValidationSupported();
	

	protected void setUp() throws Exception {
		fManager= FileBuffers.getTextFileBufferManager();
		fProject= ResourceHelper.createProject("project");
		fPath= createPath(fProject);
	}
	
	protected void tearDown() throws Exception {
		ResourceHelper.deleteProject("project");
	}
	
	protected IPath getPath() {
		return fPath;
	}
		
	/*
	 * Tests getLocation.
	 */
	public void test1() throws Exception {
		fManager.connect(fPath, null);
		try {
			ITextFileBuffer buffer= fManager.getTextFileBuffer(fPath);
			assertEquals(fPath, buffer.getLocation());
			
		} finally {
			fManager.disconnect(fPath, null);
		}
	}
		
	/*
	 * Tests isSynchronized.
	 */
	public void test2() throws Exception {
		fManager.connect(fPath, null);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(fileBuffer.isSynchronized());
			File file= FileBuffers.getSystemFileAtLocation(fPath);
			boolean modified= file.setLastModified(1000);
			assertEquals(modified, !fileBuffer.isSynchronized());
			
		} finally {
			fManager.disconnect(fPath, null);
		}
	}
	
	/*
	 * Tests isDirty.
	 */
	public void test3() throws Exception {
		fManager.connect(fPath, null);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertFalse(fileBuffer.isDirty());
			IDocument document= fileBuffer.getDocument();
			document.replace(document.getLength(), 0, "appendix");
			assertTrue(fileBuffer.isDirty());
			
		} finally {
			fManager.disconnect(fPath, null);
		}
	}
	
	/*
	 * Tests isShared.
	 */
	public void test4() throws Exception {
		fManager.connect(fPath, null);
		try {
			ITextFileBuffer fileBuffer1= fManager.getTextFileBuffer(fPath);
			assertFalse(fileBuffer1.isShared());
			
			fManager.connect(fPath, null);
			try {
				ITextFileBuffer fileBuffer2= fManager.getTextFileBuffer(fPath);
				assertTrue(fileBuffer1.isShared());
				assertTrue(fileBuffer2.isShared());
			} finally {
				fManager.disconnect(fPath, null);
			}
			
			assertFalse(fileBuffer1.isShared());
			
		} finally {
			fManager.disconnect(fPath, null);
		}
	}
	
	/*
	 * Tests getModificationStamp.
	 */
	public void test5() throws Exception {
		fManager.connect(fPath, null);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			File file= FileBuffers.getSystemFileAtLocation(fPath);
			
			long modificationStamp= fileBuffer.getModificationStamp();
			assertEquals(modificationStamp != IResource.NULL_STAMP, file.exists());
			boolean modified= file.setLastModified(1000);
			assertEquals(modified, modificationStamp != fileBuffer.getModificationStamp());
			
		} finally {
			fManager.disconnect(fPath, null);
		}		
	}
	
	/*
	 * Test revert.
	 */
	public void test6() throws Exception {
		fManager.connect(fPath, null);
		try {
			
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			// set dirty bit
			IDocument document= fileBuffer.getDocument();
			String originalContent= document.get();
			document.replace(document.getLength(), 0, "appendix");
			// invalidate synchronization state
			File file= FileBuffers.getSystemFileAtLocation(fPath);
			file.setLastModified(1000);
			//revert
			fileBuffer.revert(null);
			// check assertions
			assertEquals(originalContent, document.get());
			assertFalse(fileBuffer.isDirty());
			assertTrue(fileBuffer.isSynchronized());

		} finally {
			fManager.disconnect(fPath, null);
		}
	}
	
	/*
	 * Test commit.
	 */
	public void test7() throws Exception {
		fManager.connect(fPath, null);
		try {
			
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			// set dirty bit
			IDocument document= fileBuffer.getDocument();
			document.replace(document.getLength(), 0, "appendix");
			String originalContent= document.get();
			// commit
			fileBuffer.commit(null, true);
			// check assertions
			assertEquals(originalContent, document.get());
			assertFalse(fileBuffer.isDirty());
			assertTrue(fileBuffer.isSynchronized());
			// revert to see effect is persistent
			fileBuffer.revert(null);
			// check assertions
			assertEquals(originalContent, document.get());
			assertFalse(fileBuffer.isDirty());
			assertTrue(fileBuffer.isSynchronized());
			// disconnect
			fManager.disconnect(fPath, null);
			// reconnect
			fManager.connect(fPath, null);
			try {
				fileBuffer= fManager.getTextFileBuffer(fPath);
				document= fileBuffer.getDocument();
				// check assertions
				assertEquals(originalContent, document.get());
				assertFalse(fileBuffer.isDirty());
				assertTrue(fileBuffer.isSynchronized());				
			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.disconnect(fPath, null);
		}
	}
	
	/*
	 * Test validateState.
	 */
	public void test8_1() throws Exception {
		fManager.connect(fPath, null);
		try {
			
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			fileBuffer.validateState(null, null);
			assertTrue(fileBuffer.isStateValidated());
			
		} finally {
			fManager.disconnect(fPath, null);
		}
	}
	
	/*
	 * Test validateState.
	 */
	public void test8_2() throws Exception {
		fManager.connect(fPath, null);
		try {
			
			markReadOnly();
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			fileBuffer.validateState(null, null);
			assertTrue(fileBuffer.isStateValidated());
			
		} finally {
			fManager.disconnect(fPath, null);
		}
	}
	
	/*
	 * Test resetStateValidation.
	 */
	public void test9_1() throws Exception {
		fManager.connect(fPath, null);
		try {
			
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			fileBuffer.validateState(null, null);
			fileBuffer.resetStateValidation();
			if (isStateValidationSupported())
				assertFalse(fileBuffer.isStateValidated());
			
		} finally {
			fManager.disconnect(fPath, null);
		}
	}
	
	/*
	 * Test resetStateValidation.
	 */
	public void test9_2() throws Exception {
		fManager.connect(fPath, null);
		try {
			
			markReadOnly();
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			fileBuffer.validateState(null, null);
			fileBuffer.resetStateValidation();
			if (isStateValidationSupported())
				assertFalse(fileBuffer.isStateValidated());
			
		} finally {
			fManager.disconnect(fPath, null);
		}
	}
	
}

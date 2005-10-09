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

import java.io.PrintStream;
import java.io.PrintWriter;

import junit.framework.TestCase;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * FileBufferFunctions
 */
public abstract class FileBufferFunctions extends TestCase {
	
	private IProject fProject;
	private ITextFileBufferManager fManager;
	private IPath fPath;
	
	
	protected abstract IPath createPath(IProject project) throws Exception;
	
	protected abstract void setReadOnly(boolean state) throws Exception;
	
	protected abstract boolean modifyUnderlyingFile() throws Exception;
	
	protected abstract boolean deleteUnderlyingFile() throws Exception;
	
	protected abstract IPath moveUnderlyingFile() throws Exception;
	
	protected abstract boolean isStateValidationSupported();
	
	protected abstract Class getAnnotationModelClass() throws Exception;
	

	protected void setUp() throws Exception {
		fManager= FileBuffers.getTextFileBufferManager();
		fProject= ResourceHelper.createProject("project");
		fPath= createPath(fProject);
	}
	
	protected IProject getProject() {
		return fProject;
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
			IFileStore fileStore= FileBuffers.getFileStoreAtLocation(fPath);
			IFileInfo fileInfo= fileStore.fetchInfo();
			fileInfo.setLastModified(1000);
			fileStore.putInfo(fileInfo, EFS.SET_LAST_MODIFIED, null);
			long lastModified= fileStore.fetchInfo().getLastModified();
			assertTrue(lastModified == EFS.NONE || !fileBuffer.isSynchronized());
			
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
			long modificationStamp= fileBuffer.getModificationStamp();
			IFileStore fileStore= FileBuffers.getFileStoreAtLocation(fPath);
			IFileInfo fileInfo= fileStore.fetchInfo();
			assertEquals(modificationStamp != IResource.NULL_STAMP, fileInfo.exists());
			fileInfo.setLastModified(1000);
			fileStore.putInfo(fileInfo, EFS.SET_LAST_MODIFIED, null);
			long lastModified= fileStore.fetchInfo().getLastModified();
			assertTrue(lastModified == EFS.NONE || modificationStamp != fileBuffer.getModificationStamp());
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
			IFileStore fileStore= FileBuffers.getFileStoreAtLocation(fPath);
			IFileInfo fileInfo= fileStore.fetchInfo();
			fileInfo.setLastModified(1000);
			fileStore.putInfo(fileInfo, EFS.SET_LAST_MODIFIED, null);
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
			
			setReadOnly(true);
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			fileBuffer.validateState(null, null);
			assertTrue(fileBuffer.isStateValidated());
			
		} finally {
			setReadOnly(false);
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
			
			setReadOnly(true);
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			fileBuffer.validateState(null, null);
			fileBuffer.resetStateValidation();
			if (isStateValidationSupported())
				assertFalse(fileBuffer.isStateValidated());
			
		} finally {
			setReadOnly(false);
			fManager.disconnect(fPath, null);
		}
	}
	
	/*
	 * Test IFileBufferListener#bufferCreated and IFileBufferListener#bufferDisposed
	 */
	public void test10() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			
			public void bufferCreated(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
			
			public void bufferDisposed(IFileBuffer buf) {
				--count;
				this.buffer= buf;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			
			fManager.connect(fPath, null);
			
			assertTrue(listener.count == 1);
			assertNotNull(listener.buffer);
			IFileBuffer fileBuffer= fManager.getFileBuffer(fPath);
			assertTrue(listener.buffer == fileBuffer);
			
			fManager.disconnect(fPath, null);
			assertTrue(listener.count == 0);
			assertTrue(listener.buffer == fileBuffer);
			
		} finally {
			try {
				fManager.disconnect(fPath, null);
			} finally {
				fManager.removeFileBufferListener(listener);
			}
		}
	}
	
	/*
	 * Test IFileBufferListener#dirtyStateChanged
	 */
	public void test11_1() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			public boolean isDirty;
			
			public void dirtyStateChanged(IFileBuffer buf, boolean state) {
				++count;
				this.buffer= buf;
				this.isDirty= state;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.count == 0 && listener.buffer == null);
			
			fManager.connect(fPath, null);
			try {
				
				fileBuffer= fManager.getTextFileBuffer(fPath);
				IDocument document= fileBuffer.getDocument();
				document.replace(0, 0, "prefix");
				
				assertTrue(listener.count == 1);
				assertTrue(listener.buffer == fileBuffer);
				assertTrue(listener.isDirty);
				
				fileBuffer.commit(null, true);
				
				assertTrue(listener.count == 2);
				assertTrue(listener.buffer == fileBuffer);
				assertFalse(listener.isDirty);
				
			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	
	/*
	 * Test IFileBufferListener#dirtyStateChanged
	 */
	public void test11_2() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			public boolean isDirty;
			
			public void dirtyStateChanged(IFileBuffer buf, boolean state) {
				++count;
				this.buffer= buf;
				this.isDirty= state;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.count == 0 && listener.buffer == null);
			
			fManager.connect(fPath, null);
			try {
				
				fileBuffer= fManager.getTextFileBuffer(fPath);
				IDocument document= fileBuffer.getDocument();
				document.replace(0, 0, "prefix");
				
				assertTrue(listener.count == 1);
				assertTrue(listener.buffer == fileBuffer);
				assertTrue(listener.isDirty);
				
				fileBuffer.revert(null);
				
				assertTrue(listener.count == 2);
				assertTrue(listener.buffer == fileBuffer);
				assertFalse(listener.isDirty);
				
			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	
	/*
	 * Test IFileBufferListener#bufferContentAboutToBeReplaced/replaced
	 */
	public void test12_1() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer preBuffer, postBuffer;
			public int preCount, postCount;
			
			public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
				++preCount;
				preBuffer= buffer;
			}
			
			public void bufferContentReplaced(IFileBuffer buffer) {
				++postCount;
				postBuffer= buffer;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.preCount == 0 && listener.postCount == 0 && listener.preBuffer == null && listener.postBuffer == null);
			
			fManager.connect(fPath, null);
			try {
				
				fileBuffer= fManager.getTextFileBuffer(fPath);
				IDocument document= fileBuffer.getDocument();
				document.replace(0, 0, "prefix");
							
				fileBuffer.revert(null);
				
				assertTrue(listener.preCount == 1);
				assertTrue(listener.preBuffer == fileBuffer);
				assertTrue(listener.postCount == 1);
				assertTrue(listener.postBuffer == fileBuffer);
				
			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	
	/*
	 * Test IFileBufferListener#bufferContentAboutToBeReplaced/replaced
	 */
	public void test12_2() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer preBuffer, postBuffer;
			public int preCount, postCount;
			
			public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
				++preCount;
				preBuffer= buffer;
			}
			
			public void bufferContentReplaced(IFileBuffer buffer) {
				++postCount;
				postBuffer= buffer;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.preCount == 0 && listener.postCount == 0 && listener.preBuffer == null && listener.postBuffer == null);
			
			fManager.connect(fPath, null);
			try {
				
				fileBuffer= fManager.getTextFileBuffer(fPath);
							
				if (modifyUnderlyingFile()) {
					assertTrue(listener.preCount == 1);
					assertTrue(listener.preBuffer == fileBuffer);
					assertTrue(listener.postCount == 1);
					assertTrue(listener.postBuffer == fileBuffer);
				}
				
			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	
	/*
	 * Test IFileBufferListener#stateValidationChanged
	 */
	public void test13_1() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			public boolean isStateValidated;
			
			public void stateValidationChanged(IFileBuffer buf, boolean state) {
				++count;
				this.buffer= buf;
				this.isStateValidated= state;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.count == 0 && listener.buffer == null);
			
			fManager.connect(fPath, null);
			try {
				
				fileBuffer= fManager.getTextFileBuffer(fPath);
				fileBuffer.validateState(null, null);
				
				if (isStateValidationSupported()) {
					assertTrue(listener.count == 1);
					assertTrue(listener.buffer == fileBuffer);
					assertTrue(listener.isStateValidated);
				}
				
			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	
	/*
	 * Test IFileBufferListener#stateValidationChanged
	 */
	public void test13_2() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			public boolean isStateValidated;
			
			public void stateValidationChanged(IFileBuffer buf, boolean state) {
				++count;
				this.buffer= buf;
				this.isStateValidated= state;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, null);
			try {

				setReadOnly(true);
				fileBuffer= fManager.getTextFileBuffer(fPath);
				fileBuffer.validateState(null, null);

				if (isStateValidationSupported()) {
					assertTrue(listener.count == 1);
					assertTrue(listener.buffer == fileBuffer);
					assertTrue(listener.isStateValidated);
				}

			} finally {
				setReadOnly(false);
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	/*
	 * Test IFileBufferListener#stateValidationChanged
	 */
	public void test13_3() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			public boolean isStateValidated;
			
			public void stateValidationChanged(IFileBuffer buf, boolean state) {
				++count;
				this.buffer= buf;
				this.isStateValidated= state;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath);
				fileBuffer.validateState(null, null);
				fileBuffer.resetStateValidation();
				
				if (isStateValidationSupported()) {
					assertTrue(listener.count == 2);
					assertTrue(listener.buffer == fileBuffer);
					assertFalse(listener.isStateValidated);
				}

			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	
	/*
	 * Test IFileBufferListener#stateValidationChanged
	 */
	public void test13_4() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			public boolean isStateValidated;
			
			public void stateValidationChanged(IFileBuffer buf, boolean state) {
				++count;
				this.buffer= buf;
				this.isStateValidated= state;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, null);
			try {

				setReadOnly(true);
				fileBuffer= fManager.getTextFileBuffer(fPath);
				fileBuffer.validateState(null, null);
				fileBuffer.resetStateValidation();

				if (isStateValidationSupported()) {
					assertTrue(listener.count == 2);
					assertTrue(listener.buffer == fileBuffer);
					assertFalse(listener.isStateValidated);
				}

			} finally {
				setReadOnly(false);
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	
	/*
	 * Test IFileBufferListener#underlyingFileDeleted
	 */
	public void test14() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			
			public void underlyingFileDeleted(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath);
				if (deleteUnderlyingFile()) {
					assertTrue(listener.count == 1);
					assertTrue(listener.buffer == fileBuffer);
				}

			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	
	/*
	 * Test IFileBufferListener#underlyingFileMoved
	 */
	public void test15() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			public IPath newLocation;
			
			public void underlyingFileMoved(IFileBuffer buf, IPath location) {
				++count;
				this.buffer= buf;
				this.newLocation= location;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath);
				IPath newLocation= moveUnderlyingFile();
				if (newLocation != null) {
					assertTrue(listener.count == 1);
					assertTrue(listener.buffer == fileBuffer);
					assertEquals(listener.newLocation, newLocation);
				}

			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#stateChanging for external changes
	 */
	public void test16_1() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			
			public void stateChanging(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath);
				if (modifyUnderlyingFile()) {
					assertSame(listener.buffer, fileBuffer);
					assertEquals(1, listener.count);
				}

			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	
	/*
	 * Test IFileBufferListener#stateChanging for external changes
	 */
	public void test16_2() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			
			public void stateChanging(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath);
				if (deleteUnderlyingFile()) {
					assertTrue(listener.count == 1);
					assertTrue(listener.buffer == fileBuffer);
				}

			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#stateChanging for external changes
	 */
	public void test16_3() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			
			public void stateChanging(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath);
				if (moveUnderlyingFile() != null) {
					assertTrue(listener.count == 1);
					assertTrue(listener.buffer == fileBuffer);
				}

			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	
	/*
	 * Test IFileBufferListener#stateChanging for internal changes
	 */
	public void test17_1() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			
			public void stateChanging(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath);
				fileBuffer.validateState(null, null);
				
				if (isStateValidationSupported()) {
					assertTrue(listener.count == 1);
					assertTrue(listener.buffer == fileBuffer);
				}

			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	/*
	 * Test IFileBufferListener#stateChanging for internal changes
	 */
	public void test17_2() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			
			public void stateChanging(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath);
				IDocument document= fileBuffer.getDocument();
				document.replace(0, 0, "prefix");				
				fileBuffer.revert(null);
				
				assertTrue(listener.count == 1);
				assertTrue(listener.buffer == fileBuffer);

			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	
	/*
	 * Test IFileBufferListener#stateChanging for internal changes
	 */
	public void test17_3() throws Exception {
		class Listener extends FileBufferListener {
			
			public IFileBuffer buffer;
			public int count;
			
			public void stateChanging(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
		}
		
		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath);
				IDocument document= fileBuffer.getDocument();
				document.replace(0, 0, "prefix");				
				fileBuffer.commit(null, true);
				
				assertTrue(listener.count == 1);
				assertTrue(listener.buffer == fileBuffer);

			} finally {
				fManager.disconnect(fPath, null);
			}
			
		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	
	/*
	 * Test annotation model existence. 
	 * ATTENTION: This test is only effective in a workspace that contains the "org.eclipse.ui.editors" bundle.
	 */
	public void test18() throws Exception {
		fManager.connect(fPath, null);
		try {
			ITextFileBuffer buffer= fManager.getTextFileBuffer(fPath);
			assertNotNull(buffer);
			
			Class clazz= getAnnotationModelClass();
			if (clazz != null) {
				IAnnotationModel model= buffer.getAnnotationModel();
				assertTrue(clazz.isInstance(model));
			}
			
		} finally {
			fManager.disconnect(fPath, null);
		}
	}
	
	/*
	 * Test notification in case of failing listener. 
	 */
	public void test19() throws Exception {
		
		class NotifiedListener extends FileBufferListener {
			
			int notifyCount= 0;
			
			public void bufferCreated(IFileBuffer buffer) {
				notifyCount++;
			}
			public void bufferDisposed(IFileBuffer buffer) {
				notifyCount++;
			}
		}
		
		class ForcedException extends RuntimeException {
			private static final long serialVersionUID= 1L;

			public void printStackTrace(PrintStream s) {
				s.println("!FORCED BY TEST: this entry is intentional");
			}
			
			public void printStackTrace(PrintWriter s) {
				s.println("!FORCED BY TEST: this entry is intentional");
			}
		}
		
		NotifiedListener notifyCounter1= new NotifiedListener();
		NotifiedListener notifyCounter2= new NotifiedListener();
		
		FileBufferListener failingListener= new FileBufferListener() {
			public void bufferCreated(IFileBuffer buffer) {
				throw new ForcedException();
			}
			public void bufferDisposed(IFileBuffer buffer) {
				throw new ForcedException();
			}
		};
		
		fManager.addFileBufferListener(notifyCounter1);
		fManager.addFileBufferListener(failingListener);
		fManager.addFileBufferListener(notifyCounter2);
		
		fManager.connect(fPath, null);
		try {
			ITextFileBuffer buffer= fManager.getTextFileBuffer(fPath);
			assertNotNull(buffer);
			
			Class clazz= getAnnotationModelClass();
			if (clazz != null) {
				IAnnotationModel model= buffer.getAnnotationModel();
				assertTrue(clazz.isInstance(model));
			}
			
		} finally {
			fManager.disconnect(fPath, null);
			fManager.removeFileBufferListener(notifyCounter1);
			fManager.removeFileBufferListener(failingListener);
			fManager.removeFileBufferListener(notifyCounter2);
		}
		
		assertEquals(2, notifyCounter1.notifyCount);
		assertEquals(2, notifyCounter2.notifyCount);
	}
}

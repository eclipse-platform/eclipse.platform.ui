/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alex Blewitt <alex.blewitt@gmail.com> - Replace new Boolean with Boolean.valueOf - https://bugs.eclipse.org/470244
 *******************************************************************************/
package org.eclipse.core.filebuffers.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * FileBufferFunctions
 */
public abstract class FileBufferFunctions {

	private IProject fProject;
	protected ITextFileBufferManager fManager;
	private IPath fPath;


	protected abstract IPath createPath(IProject project) throws Exception;

	protected abstract void setReadOnly(boolean state) throws Exception;

	protected abstract boolean modifyUnderlyingFile() throws Exception;

	protected abstract boolean deleteUnderlyingFile() throws Exception;

	protected abstract IPath moveUnderlyingFile() throws Exception;

	protected abstract boolean isStateValidationSupported();

	protected abstract Class<IAnnotationModel> getAnnotationModelClass() throws Exception;


	@Before
	public void setUp() throws Exception {
		fManager= FileBuffers.getTextFileBufferManager();
		fProject= ResourceHelper.createProject("project");
		fPath= createPath(fProject);
		ITextFileBuffer buffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
		assertTrue(buffer == null);
	}

	protected IProject getProject() {
		return fProject;
	}

	@After
	public void tearDown() {
		ITextFileBuffer buffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
		assertTrue(buffer == null);
		ResourceHelper.deleteProject("project");
	}

	protected IPath getPath() {
		return fPath;
	}

	/*
	 * Tests getLocation.
	 */
	@Test
	public void test1() throws Exception {
		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {
			ITextFileBuffer buffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertEquals(fPath, buffer.getLocation());

		} finally {
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
		}
	}

	/*
	 * Tests isSynchronized.
	 */
	@Test
	public void test2() throws Exception {
		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(fileBuffer.isSynchronized());
			IFileStore fileStore= FileBuffers.getFileStoreAtLocation(fPath);
			IFileInfo fileInfo= fileStore.fetchInfo();
			fileInfo.setLastModified(1000);
			if (fileInfo.exists())
				fileStore.putInfo(fileInfo, EFS.SET_LAST_MODIFIED, null);
			long lastModified= fileStore.fetchInfo().getLastModified();
			assertTrue(lastModified == EFS.NONE || !fileBuffer.isSynchronized());

		} finally {
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
		}
	}

	/*
	 * Tests isDirty.
	 */
	@Test
	public void test3() throws Exception {
		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertFalse(fileBuffer.isDirty());
			IDocument document= fileBuffer.getDocument();
			document.replace(document.getLength(), 0, "appendix");
			assertTrue(fileBuffer.isDirty());

		} finally {
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
		}
	}

	/*
	 * Tests isShared.
	 */
	@Test
	public void test4() throws Exception {
		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {
			ITextFileBuffer fileBuffer1= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertFalse(fileBuffer1.isShared());

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {
				ITextFileBuffer fileBuffer2= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				assertTrue(fileBuffer1.isShared());
				assertTrue(fileBuffer2.isShared());
			} finally {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

			assertFalse(fileBuffer1.isShared());

		} finally {
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
		}
	}

	/*
	 * Tests getModificationStamp.
	 */
	@Test
	public void test5() throws Exception {
		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			long modificationStamp= fileBuffer.getModificationStamp();
			IFileStore fileStore= FileBuffers.getFileStoreAtLocation(fPath);
			IFileInfo fileInfo= fileStore.fetchInfo();
			assertEquals(modificationStamp != IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP, fileInfo.exists());
			fileInfo.setLastModified(1000);
			if (fileInfo.exists())
				fileStore.putInfo(fileInfo, EFS.SET_LAST_MODIFIED, null);
			long lastModified= fileStore.fetchInfo().getLastModified();
			assertTrue(lastModified == EFS.NONE || modificationStamp != fileBuffer.getModificationStamp());
		} finally {
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
		}
	}

	/*
	 * Test revert.
	 */
	@Test
	public void test6() throws Exception {
		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {

			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			// set dirty bit
			IDocument document= fileBuffer.getDocument();
			String originalContent= document.get();
			document.replace(document.getLength(), 0, "appendix");
			// invalidate synchronization state
			IFileStore fileStore= FileBuffers.getFileStoreAtLocation(fPath);
			IFileInfo fileInfo= fileStore.fetchInfo();
			fileInfo.setLastModified(1000);
			if (fileInfo.exists())
				fileStore.putInfo(fileInfo, EFS.SET_LAST_MODIFIED, null);
			//revert
			fileBuffer.revert(null);
			// check assertions
			assertEquals(originalContent, document.get());
			assertFalse(fileBuffer.isDirty());
			assertTrue(fileBuffer.isSynchronized());

		} finally {
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
		}
	}

	/*
	 * Test commit.
	 */
	@Test
	public void test7() throws Exception {
		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {

			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
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
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			// reconnect
			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {
				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				document= fileBuffer.getDocument();
				// check assertions
				assertEquals(originalContent, document.get());
				assertFalse(fileBuffer.isDirty());
				assertTrue(fileBuffer.isSynchronized());
			} finally {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
		}
	}

	/*
	 * Test validateState.
	 */
	@Test
	public void test8_1() throws Exception {
		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {

			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			fileBuffer.validateState(null, null);
			assertTrue(fileBuffer.isStateValidated());

		} finally {
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
		}
	}

	/*
	 * Test validateState.
	 */
	@Test
	public void test8_2() throws Exception {
		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {

			setReadOnly(true);
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			fileBuffer.validateState(null, null);
			assertTrue(fileBuffer.isStateValidated());

		} finally {
			setReadOnly(false);
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
		}
	}

	/*
	 * Test resetStateValidation.
	 */
	@Test
	public void test9_1() throws Exception {
		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {

			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			fileBuffer.validateState(null, null);
			fileBuffer.resetStateValidation();
			if (isStateValidationSupported())
				assertFalse(fileBuffer.isStateValidated());

		} finally {
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
		}
	}

	/*
	 * Test resetStateValidation.
	 */
	@Test
	public void test9_2() throws Exception {
		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {

			setReadOnly(true);
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			fileBuffer.validateState(null, null);
			fileBuffer.resetStateValidation();
			if (isStateValidationSupported())
				assertFalse(fileBuffer.isStateValidated());

		} finally {
			setReadOnly(false);
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
		}
	}

	/*
	 * Test IFileBufferListener#bufferCreated and IFileBufferListener#bufferDisposed
	 */
	@Test
	public void test10() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;

			@Override
			public void bufferCreated(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}

			@Override
			public void bufferDisposed(IFileBuffer buf) {
				--count;
				this.buffer= buf;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {

			fManager.connect(fPath, LocationKind.NORMALIZE, null);

			assertTrue(listener.count == 1);
			assertNotNull(listener.buffer);
			IFileBuffer fileBuffer= fManager.getFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.buffer == fileBuffer);

			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			assertTrue(listener.count == 0);
			assertTrue(listener.buffer == fileBuffer);

		} finally {
			try {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			} finally {
				fManager.removeFileBufferListener(listener);
			}
		}
	}

	/*
	 * Test IFileBufferListener#dirtyStateChanged
	 */
	@Test
	public void test11_1() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;
			public boolean isDirty;

			@Override
			public void dirtyStateChanged(IFileBuffer buf, boolean state) {
				++count;
				this.buffer= buf;
				this.isDirty= state;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {

			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
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
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#dirtyStateChanged
	 */
	@Test
	public void test11_2() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;
			public boolean isDirty;

			@Override
			public void dirtyStateChanged(IFileBuffer buf, boolean state) {
				++count;
				this.buffer= buf;
				this.isDirty= state;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
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
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#bufferContentAboutToBeReplaced/replaced
	 */
	@Test
	public void test12_1() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer preBuffer, postBuffer;
			public int preCount, postCount;

			@Override
			public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
				++preCount;
				preBuffer= buffer;
			}

			@Override
			public void bufferContentReplaced(IFileBuffer buffer) {
				++postCount;
				postBuffer= buffer;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.preCount == 0 && listener.postCount == 0 && listener.preBuffer == null && listener.postBuffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				IDocument document= fileBuffer.getDocument();
				document.replace(0, 0, "prefix");

				fileBuffer.revert(null);

				assertTrue(listener.preCount == 1);
				assertTrue(listener.preBuffer == fileBuffer);
				assertTrue(listener.postCount == 1);
				assertTrue(listener.postBuffer == fileBuffer);

			} finally {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#bufferContentAboutToBeReplaced/replaced
	 */
	@Test
	public void test12_2() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer preBuffer, postBuffer;
			public int preCount, postCount;

			@Override
			public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
				++preCount;
				preBuffer= buffer;
			}

			@Override
			public void bufferContentReplaced(IFileBuffer buffer) {
				++postCount;
				postBuffer= buffer;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.preCount == 0 && listener.postCount == 0 && listener.preBuffer == null && listener.postBuffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);

				if (modifyUnderlyingFile()) {
					assertTrue(listener.preCount == 1);
					assertTrue(listener.preBuffer == fileBuffer);
					assertTrue(listener.postCount == 1);
					assertTrue(listener.postBuffer == fileBuffer);
				}

			} finally {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#stateValidationChanged
	 */
	@Test
	public void test13_1() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;
			public boolean isStateValidated;

			@Override
			public void stateValidationChanged(IFileBuffer buf, boolean state) {
				++count;
				this.buffer= buf;
				this.isStateValidated= state;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				fileBuffer.validateState(null, null);

				if (isStateValidationSupported()) {
					assertTrue(listener.count == 1);
					assertTrue(listener.buffer == fileBuffer);
					assertTrue(listener.isStateValidated);
				}

			} finally {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#stateValidationChanged
	 */
	@Test
	public void test13_2() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;
			public boolean isStateValidated;

			@Override
			public void stateValidationChanged(IFileBuffer buf, boolean state) {
				++count;
				this.buffer= buf;
				this.isStateValidated= state;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				setReadOnly(true);
				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				fileBuffer.validateState(null, null);

				if (isStateValidationSupported()) {
					assertTrue(listener.count == 1);
					assertTrue(listener.buffer == fileBuffer);
					assertTrue(listener.isStateValidated);
				}

			} finally {
				setReadOnly(false);
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	/*
	 * Test IFileBufferListener#stateValidationChanged
	 */
	@Test
	public void test13_3() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;
			public boolean isStateValidated;

			@Override
			public void stateValidationChanged(IFileBuffer buf, boolean state) {
				++count;
				this.buffer= buf;
				this.isStateValidated= state;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				fileBuffer.validateState(null, null);
				fileBuffer.resetStateValidation();

				if (isStateValidationSupported()) {
					assertTrue(listener.count == 2);
					assertTrue(listener.buffer == fileBuffer);
					assertFalse(listener.isStateValidated);
				}

			} finally {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#stateValidationChanged
	 */
	@Test
	public void test13_4() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;
			public boolean isStateValidated;

			@Override
			public void stateValidationChanged(IFileBuffer buf, boolean state) {
				++count;
				this.buffer= buf;
				this.isStateValidated= state;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				setReadOnly(true);
				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				fileBuffer.validateState(null, null);
				fileBuffer.resetStateValidation();

				if (isStateValidationSupported()) {
					assertTrue(listener.count == 2);
					assertTrue(listener.buffer == fileBuffer);
					assertFalse(listener.isStateValidated);
				}

			} finally {
				setReadOnly(false);
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#underlyingFileDeleted
	 */
	@Test
	public void test14() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;

			@Override
			public void underlyingFileDeleted(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				if (deleteUnderlyingFile()) {
					assertTrue(listener.count == 1);
					assertTrue(listener.buffer == fileBuffer);
				}

			} finally {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#underlyingFileMoved
	 */
	@Test
	public void test15() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;
			public IPath newLocation;

			@Override
			public void underlyingFileMoved(IFileBuffer buf, IPath location) {
				++count;
				this.buffer= buf;
				this.newLocation= location;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				IPath newLocation= moveUnderlyingFile();
				if (newLocation != null) {
					if (listener.count != 1 || listener.buffer != fileBuffer || !newLocation.equals(listener.newLocation)) {
						StringBuffer buf= new StringBuffer();
						buf.append("Wrong listener notifcation in " + getClass().getName() + ":\n");
						buf.append("listener.count: " + listener.count + " (expected: 1)\n");
						if (newLocation.equals(listener.newLocation))
							buf.append("newLocation identical: true\n");
						else {
							buf.append("listener.newLocation: " + listener.newLocation + "\n");
							buf.append("newLocation: " + newLocation + "\n");
						}
						buf.append("buffers identical: " + Boolean.valueOf(listener.buffer == fileBuffer));
						fail(buf.toString());
					}
				}

			} finally {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#stateChanging for external changes
	 */
	@Test
	public void test16_1() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;

			@Override
			public void stateChanging(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				if (modifyUnderlyingFile()) {
					assertSame(listener.buffer, fileBuffer);
					assertEquals(1, listener.count);
				}

			} finally {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#stateChanging for external changes
	 */
	@Test
	public void test16_2() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;

			@Override
			public void stateChanging(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				if (deleteUnderlyingFile()) {
					assertTrue(listener.count == 1);
					assertTrue(listener.buffer == fileBuffer);
				}

			} finally {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#stateChanging for external changes
	 */
	@Test
	public void test16_3() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;

			@Override
			public void stateChanging(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				if (moveUnderlyingFile() != null) {
					assertTrue(listener.count == 1);
					assertTrue(listener.buffer == fileBuffer);
				}

			} finally {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#stateChanging for internal changes
	 */
	@Test
	public void test17_1() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;

			@Override
			public void stateChanging(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				fileBuffer.validateState(null, null);

				if (isStateValidationSupported()) {
					assertTrue(listener.count == 1);
					assertTrue(listener.buffer == fileBuffer);
				}

			} finally {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}
	/*
	 * Test IFileBufferListener#stateChanging for internal changes
	 */
	@Test
	public void test17_2() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;

			@Override
			public void stateChanging(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				IDocument document= fileBuffer.getDocument();
				document.replace(0, 0, "prefix");
				fileBuffer.revert(null);

				assertTrue(listener.count == 1);
				assertTrue(listener.buffer == fileBuffer);

			} finally {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test IFileBufferListener#stateChanging for internal changes
	 */
	@Test
	public void test17_3() throws Exception {
		class Listener extends FileBufferListener {

			public IFileBuffer buffer;
			public int count;

			@Override
			public void stateChanging(IFileBuffer buf) {
				++count;
				this.buffer= buf;
			}
		}

		Listener listener= new Listener();
		fManager.addFileBufferListener(listener);
		try {
			ITextFileBuffer fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertTrue(listener.count == 0 && listener.buffer == null);

			fManager.connect(fPath, LocationKind.NORMALIZE, null);
			try {

				fileBuffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
				IDocument document= fileBuffer.getDocument();
				document.replace(0, 0, "prefix");
				fileBuffer.commit(null, true);

				assertTrue(listener.count == 1);
				assertTrue(listener.buffer == fileBuffer);

			} finally {
				fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			}

		} finally {
			fManager.removeFileBufferListener(listener);
		}
	}

	/*
	 * Test annotation model existence.
	 * ATTENTION: This test is only effective in a workspace that contains the "org.eclipse.ui.editors" bundle.
	 */
	@Test
	public void test18() throws Exception {
		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {
			ITextFileBuffer buffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertNotNull(buffer);

			Class<IAnnotationModel> clazz= getAnnotationModelClass();
			if (clazz != null) {
				IAnnotationModel model= buffer.getAnnotationModel();
				assertTrue(clazz.isInstance(model));
			}

		} finally {
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
		}
	}

	/*
	 * Test notification in case of failing listener.
	 */
	@Test
	public void test19() throws Exception {

		class NotifiedListener extends FileBufferListener {

			int notifyCount= 0;

			@Override
			public void bufferCreated(IFileBuffer buffer) {
				notifyCount++;
			}
			@Override
			public void bufferDisposed(IFileBuffer buffer) {
				notifyCount++;
			}
		}

		class ForcedException extends RuntimeException {
			private static final long serialVersionUID= 1L;

			@Override
			public void printStackTrace(PrintStream s) {
				s.println("!FORCED BY TEST: this entry is intentional");
			}

			@Override
			public void printStackTrace(PrintWriter s) {
				s.println("!FORCED BY TEST: this entry is intentional");
			}
		}

		NotifiedListener notifyCounter1= new NotifiedListener();
		NotifiedListener notifyCounter2= new NotifiedListener();

		FileBufferListener failingListener= new FileBufferListener() {
			@Override
			public void bufferCreated(IFileBuffer buffer) {
				throw new ForcedException();
			}
			@Override
			public void bufferDisposed(IFileBuffer buffer) {
				throw new ForcedException();
			}
		};

		fManager.addFileBufferListener(notifyCounter1);
		fManager.addFileBufferListener(failingListener);
		fManager.addFileBufferListener(notifyCounter2);

		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {
			ITextFileBuffer buffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertNotNull(buffer);

			Class<IAnnotationModel> clazz= getAnnotationModelClass();
			if (clazz != null) {
				IAnnotationModel model= buffer.getAnnotationModel();
				assertTrue(clazz.isInstance(model));
			}

		} finally {
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
			fManager.removeFileBufferListener(notifyCounter1);
			fManager.removeFileBufferListener(failingListener);
			fManager.removeFileBufferListener(notifyCounter2);
		}

		assertEquals(2, notifyCounter1.notifyCount);
		assertEquals(2, notifyCounter2.notifyCount);
	}

	@Test
	public void testGetBufferForDocument() throws Exception {
		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {
			ITextFileBuffer buffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertNotNull(buffer);
			IDocument document= buffer.getDocument();
			assertNotNull(document);
			assertSame(buffer, fManager.getTextFileBuffer(document));
		} finally {
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
		}
	}

	/*
	 * Tests isSynchronized.
	 */
	@Test
	public void testGetFileStoreAnnotationModel() throws Exception {
		IFileStore fileStore= EFS.getNullFileSystem().getStore(new Path("/dev/null"));
		assertNotNull(fileStore);
		fManager.connectFileStore(fileStore, null);
		try {
			ITextFileBuffer fileBuffer= fManager.getFileStoreTextFileBuffer(fileStore);
			IAnnotationModel model= fileBuffer.getAnnotationModel();
			Class<IAnnotationModel> clazz= getAnnotationModelClass();
			if (clazz != null)
				assertTrue(clazz.isInstance(model));
			else
				assertNotNull(model);
		} finally {
			fManager.disconnectFileStore(fileStore, null);
		}
	}

	@Test
	public void testGetFileBuffers() throws Exception {
		fManager.connect(fPath, LocationKind.NORMALIZE, null);
		try {
			ITextFileBuffer buffer= fManager.getTextFileBuffer(fPath, LocationKind.NORMALIZE);
			assertNotNull(buffer);
			IFileBuffer[] fileBuffers= fManager.getFileBuffers();
			assertNotNull(fileBuffers);
			assertEquals(1, fileBuffers.length);
			assertSame(buffer, fileBuffers[0]);
			fileBuffers= fManager.getFileStoreFileBuffers();
			assertNotNull(fileBuffers);
			assertEquals(0, fileBuffers.length);
		} finally {
			fManager.disconnect(fPath, LocationKind.NORMALIZE, null);
		}
	}

}

/*******************************************************************************
 * Copyright (c) 2019 Kichwa Coders Canada Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.ide.api;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.FileSystemHelper;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.ide.IDE;
import org.junit.After;
import org.junit.Test;

/**
 * More tests for the <code>IDE</code> API and behaviour.
 */
public class IDETest2 {
	private final Set<IFileStore> storesToDelete = new HashSet<>();

	@After
	public void doTearDown() throws Exception {
		storesToDelete.forEach(file -> {
			try {
				file.delete(EFS.NONE, null);
			} catch (CoreException e) {
			}
		});
	}

	/**
	 * Test for IDE.getEditorDescriptorForFileStore when there is a content type
	 * match, but no associated editor
	 */
	@Test
	public void testGetEditorDescriptorForFileStoreNoEditor() throws IOException, CoreException {
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(FileSystemHelper
				.getRandomLocation(FileSystemHelper.getTempDir()).addFileExtension("bug516470-noeditor"));
		storesToDelete.add(fileStore);
		fileStore.openOutputStream(EFS.NONE, null).close();
		IEditorDescriptor editorDescriptorForFileStore = IDE.getEditorDescriptorForFileStore(fileStore, false);
		assertEquals("org.eclipse.ui.DefaultTextEditor", editorDescriptorForFileStore.getId());
	}

	/**
	 * Test for IDE.getEditorDescriptorForFileStore when there is a content type
	 * match with an associated editor
	 */
	@Test
	public void testGetEditorDescriptorForFileStoreWithEditor() throws IOException, CoreException {
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(FileSystemHelper
				.getRandomLocation(FileSystemHelper.getTempDir()).addFileExtension("bug516470-witheditor"));
		storesToDelete.add(fileStore);
		fileStore.openOutputStream(EFS.NONE, null).close();
		IEditorDescriptor editorDescriptorForFileStore = IDE.getEditorDescriptorForFileStore(fileStore, false);
		assertEquals("org.eclipse.ui.tests.editor.bug516470", editorDescriptorForFileStore.getId());
	}

	/**
	 * Test for IDE.getEditorDescriptorForFileStore when there is no content type
	 * match
	 */
	@Test
	public void testGetEditorDescriptorForFileStoreNoContentType() throws IOException, CoreException {
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(FileSystemHelper
				.getRandomLocation(FileSystemHelper.getTempDir()).addFileExtension("bug516470-nocontenttype"));
		storesToDelete.add(fileStore);
		fileStore.openOutputStream(EFS.NONE, null).close();
		IEditorDescriptor editorDescriptorForFileStore = IDE.getEditorDescriptorForFileStore(fileStore, false);
		assertEquals("org.eclipse.ui.DefaultTextEditor", editorDescriptorForFileStore.getId());
	}

	/**
	 * Test for IDE.getEditorDescriptorForFileStore when file does not exist which
	 * means no content type is used.
	 */
	@Test
	public void testGetEditorDescriptorForFileStoreDoesNotExist() throws CoreException {
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(FileSystemHelper
				.getRandomLocation(FileSystemHelper.getTempDir()).addFileExtension("bug516470-filedoesnotexist"));
		// don't create the file
		IEditorDescriptor editorDescriptorForFileStore = IDE.getEditorDescriptorForFileStore(fileStore, false);
		assertEquals("org.eclipse.ui.DefaultTextEditor", editorDescriptorForFileStore.getId());
	}
}

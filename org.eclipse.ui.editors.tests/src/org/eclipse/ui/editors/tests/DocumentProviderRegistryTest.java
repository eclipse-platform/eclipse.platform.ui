/*******************************************************************************
 * Copyright (c) 2019 Thomas Wolf <thomas.wolf@paranor.ch>
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import org.eclipse.core.filebuffers.tests.ResourceHelper;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;

import org.eclipse.ui.editors.text.TextFileDocumentProvider;

public class DocumentProviderRegistryTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private IFile file;

	@After
	public void tearDown() throws Exception {
		if (file != null) {
			ResourceHelper.delete(file.getProject());
		}
		TestUtil.cleanUp();
	}

	@Test
	public void testFindByExtensionInWorkspace() throws Exception {
		IFolder folder = ResourceHelper.createFolder("DocumentProviderRegistryTestProject/test");
		file = ResourceHelper.createFile(folder, "file.testfile", "");
		assertTrue("File should exist: " + file.toString(), file.exists());
		IEditorInput editorInput = new FileEditorInput(file);
		IDocumentProvider provider = DocumentProviderRegistry.getDefault().getDocumentProvider(editorInput);
		assertEquals("Unexpected document provider found : " + provider.getClass().getName(),
				TestDocumentProvider.class, provider.getClass());
	}

	@Test
	public void testFindByExtensionNonWorkspace() throws Exception {
		File external = tmp.newFile("external.testfile");
		IFileStore store = EFS.getLocalFileSystem().getStore(new Path(external.getCanonicalPath()));
		IEditorInput editorInput = new FileStoreEditorInput(store);
		IDocumentProvider provider = DocumentProviderRegistry.getDefault().getDocumentProvider(editorInput);
		assertEquals("Unexpected document provider found : " + provider.getClass().getName(),
				TestDocumentProvider.class, provider.getClass());
	}

	public static class TestDocumentProvider extends TextFileDocumentProvider {

		// Nothing; class registered in plugin.xml so that we can test that we
		// found the right one.

	}
}

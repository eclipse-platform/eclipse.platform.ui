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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.IPath;

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

	@TempDir
	Path tmp;

	private IFile file;

	@AfterEach
	void tearDown() throws Exception {
		if (file != null) {
			ResourceHelper.delete(file.getProject());
		}
		TestUtil.cleanUp();
	}

	@Test
	void testFindByExtensionInWorkspace() throws Exception {
		IFolder folder = ResourceHelper.createFolder("DocumentProviderRegistryTestProject/test");
		file = ResourceHelper.createFile(folder, "file.testfile", "");
		assertTrue(file.exists(), "File should exist: " + file.toString());
		IEditorInput editorInput = new FileEditorInput(file);
		IDocumentProvider provider = DocumentProviderRegistry.getDefault().getDocumentProvider(editorInput);
		assertEquals(TestDocumentProvider.class, provider.getClass(),
				"Unexpected document provider found : " + provider.getClass().getName());
	}

	@Test
	void testFindByExtensionNonWorkspace() throws Exception {
		File external = tmp.resolve("external.testfile").toFile();
		IFileStore store = EFS.getLocalFileSystem().getStore(IPath.fromOSString(external.getCanonicalPath()));
		IEditorInput editorInput = new FileStoreEditorInput(store);
		IDocumentProvider provider = DocumentProviderRegistry.getDefault().getDocumentProvider(editorInput);
		assertEquals(TestDocumentProvider.class, provider.getClass(),
				"Unexpected document provider found : " + provider.getClass().getName());
	}

	public static class TestDocumentProvider extends TextFileDocumentProvider {

		// Nothing; class registered in plugin.xml so that we can test that we
		// found the right one.

	}
}

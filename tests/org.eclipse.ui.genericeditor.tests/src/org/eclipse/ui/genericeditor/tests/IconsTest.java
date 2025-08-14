/********************************************************************************
 * Copyright (c) 2019, 2025 Lakshminarayana Nekkanti(narayana.nekkanti@gmail.com)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 3
 *
 * Contributor
 * Lakshminarayana Nekkanti - initial API and implementation
 ********************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.eclipse.ui.tests.harness.util.DisplayHelper.runEventLoop;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextEditor;
import org.eclipse.ui.internal.genericeditor.GenericEditorPlugin;
import org.eclipse.ui.part.FileEditorInput;

public class IconsTest extends AbstratGenericEditorTest {

	private ExtensionBasedTextEditor genericEditor;

	private IFile testFile;

	private IProject testProject;

	@Test
	public void testEditorIconParentSet() throws Exception {
		testProject= ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		testProject.create(null);
		testProject.open(null);

		testFile= testProject.getFile("foobar.txt");
		testFile.create(new ByteArrayInputStream("Testing file".getBytes()), true, null);

		genericEditor= (ExtensionBasedTextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().openEditor(new FileEditorInput(testFile), "org.eclipse.ui.genericeditor.GenericEditor");

		Field field= genericEditor.getClass().getDeclaredField("contentTypeImageDescripter");
		field.setAccessible(true);// Workaround to access descriptor

		ImageDescriptor descriptor= GenericEditorPlugin.getDefault().getContentTypeImagesRegistry()
				.getImageDescriptor(new IContentType[] { Platform.getContentTypeManager().getContentType("org.eclipse.ui.genericeditor.tests.content-type"),
				});
		assertEquals(field.get(genericEditor), descriptor);
	}

	@Test
	public void testEditorIconChildSet() throws Exception {
		testProject= ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		testProject.create(null);
		testProject.open(null);

		testFile= testProject.getFile("foo.txt");
		testFile.create(new ByteArrayInputStream("Testing file".getBytes()), true, null);

		genericEditor= (ExtensionBasedTextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().openEditor(new FileEditorInput(testFile), "org.eclipse.ui.genericeditor.GenericEditor");

		Field field= genericEditor.getClass().getDeclaredField("contentTypeImageDescripter");
		field.setAccessible(true);// Workaround to access descriptor

		ImageDescriptor descriptor= GenericEditorPlugin.getDefault().getContentTypeImagesRegistry()
				.getImageDescriptor(new IContentType[] { Platform.getContentTypeManager().getContentType("org.eclipse.ui.genericeditor.tests.sub-specialized-content-type"),
				});
		assertEquals(field.get(genericEditor), descriptor);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		if (genericEditor != null) {
			genericEditor.close(false);
			genericEditor= null;
			runEventLoop(PlatformUI.getWorkbench().getDisplay(),0);
		}
		if (testFile != null) {
			testFile.delete(true, new NullProgressMonitor());
			testFile= null;
		}
		if (testProject != null) {
			testProject.delete(true, new NullProgressMonitor());
			testProject= null;
		}
		super.tearDown();
	}
}

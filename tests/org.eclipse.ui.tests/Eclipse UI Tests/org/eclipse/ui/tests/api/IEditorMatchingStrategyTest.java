/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.api;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests the <code>IEditorMatchingStrategyTest</code> API and behaviour.
 *
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class IEditorMatchingStrategyTest extends UITestCase {

	private static final String MATCHING_EDITOR_ID = "org.eclipse.ui.tests.api.MockMatchingEditorPart1";

	public IEditorMatchingStrategyTest() {
		super(IEditorMatchingStrategyTest.class.getSimpleName());
	}

	@Test
	public void testOpenMatchingEditor() throws Exception {
		IProject proj = FileUtil.createProject("IEditorMatchingStrategyTest");
		IFile file1 = FileUtil.createFile("plugin.xml", proj);
		IFile file2 = FileUtil.createFile("MANIFEST.MF", proj);
		IFile file3 = FileUtil.createFile("build.properties", proj);
		IFile file4 = FileUtil.createFile("something.txt", proj);

		IWorkbenchPage page = openTestWindow().getActivePage();
		IEditorPart editor1 = page.openEditor(new FileEditorInput(file1), MATCHING_EDITOR_ID);
		IEditorPart editor1b = page.openEditor(new FileEditorInput(file1), MATCHING_EDITOR_ID);
		IEditorPart editor2 = page.openEditor(new FileEditorInput(file2), MATCHING_EDITOR_ID);
		IEditorPart editor3 = page.openEditor(new FileEditorInput(file3), MATCHING_EDITOR_ID);
		IEditorPart editor4 = page.openEditor(new FileEditorInput(file4), MATCHING_EDITOR_ID);
		assertSame(editor1, editor1b);
		assertSame(editor1, editor2);
		assertSame(editor1, editor3);
		assertNotSame(editor1, editor4);
	}

	@Test
	public void testFindMatchingEditor() throws Exception {
		IProject proj = FileUtil.createProject("IEditorMatchingStrategyTest");
		IFile file1 = FileUtil.createFile("plugin.xml", proj);
		IFile file2 = FileUtil.createFile("MANIFEST.MF", proj);
		IFile file3 = FileUtil.createFile("build.properties", proj);
		IFile file4 = FileUtil.createFile("something.txt", proj);

		IWorkbenchPage page = openTestWindow().getActivePage();
		IEditorPart editor = page.openEditor(new FileEditorInput(file1), MATCHING_EDITOR_ID);
		assertEquals(editor, page.findEditor(new FileEditorInput(file1)));
		assertEquals(editor, page.findEditor(new FileEditorInput(file2)));
		assertEquals(editor, page.findEditor(new FileEditorInput(file3)));
		assertEquals(null, page.findEditor(new FileEditorInput(file4)));
	}

}


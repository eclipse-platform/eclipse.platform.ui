/*******************************************************************************
 * Copyright (c) 2013, 2017 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.api;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.junit.Rule;
import org.junit.Test;

/**
 * @since 3.5
 */
public class Bug407422Test {

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	@Test
	public void test() throws CoreException {
		final IWorkbenchWindow window = openTestWindow();
		final IWorkbenchPage page = window.getActivePage();
		final String EDITOR_ID = "org.eclipse.ui.DefaultTextEditor";

		final IProject project = FileUtil.createProject("Bug407422Test");
		final IFile file1 = FileUtil.createFile("file1.txt", project);
		final IFile file2 = FileUtil.createFile("file2.txt", project);
		final IFile file3 = FileUtil.createFile("file3.txt", project);

		final FileEditorInput input1 = new FileEditorInput(file1);
		final FileEditorInput input2 = new FileEditorInput(file2);
		final FileEditorInput input3 = new FileEditorInput(file3);

		final List<IWorkbenchPartReference> openedParts = new ArrayList<>();

		page.addPartListener(new IPartListener2() {

			@Override
			public void partVisible(IWorkbenchPartReference partRef) { }

			@Override
			public void partOpened(IWorkbenchPartReference partRef) {
				openedParts.add(partRef);
			}

			@Override
			public void partInputChanged(IWorkbenchPartReference partRef) {}

			@Override
			public void partHidden(IWorkbenchPartReference partRef) {}

			@Override
			public void partDeactivated(IWorkbenchPartReference partRef) {}

			@Override
			public void partClosed(IWorkbenchPartReference partRef) {
				openedParts.remove(partRef);
			}

			@Override
			public void partBroughtToTop(IWorkbenchPartReference partRef) { }

			@Override
			public void partActivated(IWorkbenchPartReference partRef) {}
		});

		// With nothing open, getEditors should return an empty array
		final IEditorPart[] editorParts0 = page.getEditors();
		assertEquals(0, editorParts0.length);

		// open some editors
		final IEditorReference[] openRefs = page.openEditors(new IEditorInput[] {input1, input2, input3},
				new String[] {EDITOR_ID,EDITOR_ID,EDITOR_ID}, 0);
		assertEquals(3, openRefs.length);

		// openEditors will only 'open' the first editor. The others will lazily
		// opened, if the user clicks on their tabs...
		assertEquals(1, openedParts.size());

		// Close all editors...
		page.closeAllEditors(true);
		// Verify that there are no editors open...
		final IEditorReference[] editorReferences2 = page.getEditorReferences();
		assertEquals(0, editorReferences2.length);
		// ... and that the one we did open was closed...
		assertEquals(0, openedParts.size());

		// getEditors() should return an empty array, again.
		final IEditorPart[] editorParts = page.getEditors();
		// In bug 407422, the actual returned array has length 2,
		// one editor part for each of the lazily loaded editors.
		assertEquals(0, editorParts.length);
	}
}

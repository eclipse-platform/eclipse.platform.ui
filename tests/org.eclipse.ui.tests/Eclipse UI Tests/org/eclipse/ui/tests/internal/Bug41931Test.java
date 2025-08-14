/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

package org.eclipse.ui.tests.internal;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test for Bug 41931.
 *
 * @since 3.0
 */
@Ignore
public class Bug41931Test {

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	/**
	 * Tests that the <code>bringToTop(IWorkbenchPart)</code> correctly
	 * updates the activation list.
	 *
	 * @throws CoreException
	 *             If the test project cannot be created or opened.
	 */
	@Test
	public void testBringToTop() throws CoreException {
		// Open a window.
		IWorkbenchWindow window = openTestWindow();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		// Create a test project.
		IProject testProject = workspace.getRoot().getProject("Bug41931"); //$NON-NLS-1$
		testProject.create(null);
		testProject.open(null);

		// Open three test files.
		InputStream contents = new ByteArrayInputStream(new byte[0]);
		IFile fileA = testProject.getFile("a.txt"); //$NON-NLS-1$
		fileA.create(contents, true, null);
		IFile fileB = testProject.getFile("b.txt"); //$NON-NLS-1$
		fileB.create(contents, true, null);
		IFile fileC = testProject.getFile("c.txt"); //$NON-NLS-1$
		fileC.create(contents, true, null);

		// Open editors on those files.
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();
		IEditorPart editorA = IDE.openEditor(page, fileA, true);
		IEditorPart editorB = IDE.openEditor(page, fileB, true);
		IEditorPart editorC = IDE.openEditor(page, fileC, true);

		// Test that the editors are open in the order: A, B, C
		IEditorPart[] expectedResults = { editorA, editorB, editorC };
		IWorkbenchPartReference[] actualResults = page.getSortedParts();
		for (int i = 0; i < expectedResults.length; i++) {
			assertEquals(
					"Pre-test order is not correct.", expectedResults[i].getTitle(), actualResults[i].getPart(false).getTitle()); //$NON-NLS-1$
		}

		// Bring editor B to the top.
		page.bringToTop(editorB);

		// Test that the editors are open in the order: A, C, B
		expectedResults = new IEditorPart[] { editorA, editorC, editorB };
		actualResults = page.getSortedParts();
		for (int i = 0; i < expectedResults.length; i++) {
			assertEquals(
					"bringToTop() does not change sorted part order.", expectedResults[i].getTitle(), actualResults[i].getPart(false).getTitle()); //$NON-NLS-1$
		}
	}
}

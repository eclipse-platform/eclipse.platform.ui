/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.junit.Test;

/**
 * SessionRestoreTest runs the second half of our session
 * presistance tests.
 */
public class DeprecatedSessionRestoreTest {

	/**
	 * Generates a session state in the workbench.
	 */
	@Test
	public void testRestoreSession() throws Throwable {
		IWorkbenchWindow[] windows;
		IWorkbenchPage[] pages;

		// Get windows.
		windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		assertEquals(windows.length, 3);

		// First window contains empty perspective.
		pages = windows[0].getPages();
		assertEquals(pages.length, 1);
		assertEquals(pages[0].getPerspective().getId(),
				EmptyPerspective.PERSP_ID);

		// Second window contains empty + session.
		pages = windows[1].getPages();
		assertEquals(pages.length, 2);
		assertEquals(pages[0].getPerspective().getId(),
				EmptyPerspective.PERSP_ID);
		assertEquals(pages[1].getPerspective().getId(), SessionPerspective.ID);
		testSessionView(pages[1]);

		// Third window contains 2 sessions.
		pages = windows[2].getPages();
		assertEquals(pages.length, 2);
		assertEquals(pages[0].getPerspective().getId(), SessionPerspective.ID);
		assertEquals(pages[1].getPerspective().getId(), SessionPerspective.ID);
		testSessionView(pages[0]);
		testSessionView(pages[1]);

		// Last page contains 3 editors.
		IEditorPart[] editors = pages[1].getEditors();
		assertEquals(editors.length, 3);
		testSessionEditor(editors[0], SessionCreateTest.TEST_FILE_1);
		testSessionEditor(editors[1], SessionCreateTest.TEST_FILE_2);
		testSessionEditor(editors[2], SessionCreateTest.TEST_FILE_3);
	}

	/**
	 * Tests the session view within a page.
	 */
	private void testSessionView(IWorkbenchPage page) {
		IViewPart view = page.findView(SessionView.VIEW_ID);
		assertNotNull(view);
		SessionView sessionView = (SessionView) view;
		sessionView.testMementoState();
	}

	/**
	 * Tests the state of a session editor.
	 */
	private void testSessionEditor(IEditorPart part, String fileName) {
		IEditorSite site = part.getEditorSite();
		assertEquals(site.getId(), MockEditorPart.ID1);
		IEditorInput input = part.getEditorInput();
		assertTrue(input instanceof IFileEditorInput);
		IFile file = ((IFileEditorInput) input).getFile();
		assertEquals(fileName, file.getName());
	}
}


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
package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * SessionCreateTest runs the first half of our session
 * presistance tests.
 */
@RunWith(JUnit4.class)
public class SessionCreateTest extends UITestCase {

	private IWorkbenchWindow[] oldWindows;

	public static String TEST_PROJ = "sessionTest";

	public static String TEST_FILE_1 = "one.mock1";

	public static String TEST_FILE_2 = "two.mock1";

	public static String TEST_FILE_3 = "three.mock1";

	/**
	 * Construct an instance.
	 */
	public SessionCreateTest() {
		super(SessionCreateTest.class.getSimpleName());
	}

	/**
	 * Generates a session state in the workbench.
	 */
	@Test
	public void testSessionCreation() throws Throwable {
		IWorkbenchWindow window;
		IWorkbenchPage page;

		// Save the original windows.  We close all of
		// these at the end, after the test windows have
		// been created.
		saveOriginalWindows();

		// Create test window with empty perspective.
		window = fWorkbench.openWorkbenchWindow(EmptyPerspective.PERSP_ID, getPageInput());

		// Create test window with empty perspective and
		// session perspective.
		window = fWorkbench.openWorkbenchWindow(EmptyPerspective.PERSP_ID, getPageInput());
		page = window.openPage(SessionPerspective.ID, getPageInput());

		// Create test window with two session perspectives.
		window = fWorkbench.openWorkbenchWindow(SessionPerspective.ID, getPageInput());
		page = window.openPage(SessionPerspective.ID, getPageInput());

		// Open 3 editors in last page.
		IProject proj = FileUtil.createProject(TEST_PROJ);
		IFile file = FileUtil.createFile(TEST_FILE_1, proj);
		page.openEditor(new FileEditorInput(file), MockEditorPart.ID1);
		file = FileUtil.createFile(TEST_FILE_2, proj);
		page.openEditor(new FileEditorInput(file), MockEditorPart.ID1);
		file = FileUtil.createFile(TEST_FILE_3, proj);
		page.openEditor(new FileEditorInput(file), MockEditorPart.ID1);

		// Close the original windows.
		closeOriginalWindows();
	}

	/**
	 * Saves the original window set.
	 */
	private void saveOriginalWindows() {
		oldWindows = fWorkbench.getWorkbenchWindows();
	}

	/**
	 * Closes the original window set.
	 */
	private void closeOriginalWindows() {
		for (IWorkbenchWindow oldWindow : oldWindows) {
			oldWindow.close();
		}
	}

}


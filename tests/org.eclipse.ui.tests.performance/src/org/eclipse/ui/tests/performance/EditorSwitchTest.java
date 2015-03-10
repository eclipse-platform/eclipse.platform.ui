/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;

/**
 * Test editor switching.
 */
public class EditorSwitchTest extends BasicPerformanceTest {

	private String extension1;

	private String extension2;

	/**
	 * Constructor.
	 *
	 * @param testName
	 *            Test's name.
	 */
	public EditorSwitchTest(String[] pair) {
		super("testEditorSwitch:" + pair[0] + "," + pair[1]);
		extension1 = pair[0];
		extension2 = pair[1];
	}

	/**
	 * Test editor opening performance. This test always fails.
	 */
	protected void runTest() throws CoreException {

		// Open both files outside the loop so as not to include
		// the initial time to open, just switching.
		IWorkbenchWindow window = openTestWindow(UIPerformanceTestSetup.PERSPECTIVE1);
		final IWorkbenchPage activePage = window.getActivePage();
		final IFile file1 = getProject().getFile("1." + extension1);
		assertTrue(file1.exists());
		final IFile file2 = getProject().getFile("1." + extension2);
		assertTrue(file2.exists());
		IDE.openEditor(activePage, file1, true);
		IDE.openEditor(activePage, file2, true);
		processEvents();
        EditorTestHelper.calmDown(500, 30000, 500);
        waitForBackgroundJobs();

		for (int j = 0; j < 100; j++) {

			startMeasuring();
			for (int i = 0; i < 12; i++) {
                IDE.openEditor(activePage, file1, true);
                processEvents();
                IDE.openEditor(activePage, file2, true);
                processEvents();
			}
			stopMeasuring();
            EditorTestHelper.calmDown(500, 30000, 100);
		}

		commitMeasurements();
		assertPerformance();
	}
}

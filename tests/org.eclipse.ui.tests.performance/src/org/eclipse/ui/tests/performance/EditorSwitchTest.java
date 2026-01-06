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
package org.eclipse.ui.tests.performance;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.performance.UIPerformanceTestRule.getTestProject;
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.waitForBackgroundJobs;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.test.performance.PerformanceTestCaseJunit4;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test editor switching.
 */
@RunWith(Parameterized.class)
public class EditorSwitchTest extends PerformanceTestCaseJunit4 {

	@ClassRule
	public static final UIPerformanceTestRule uiPerformanceTestRule = new UIPerformanceTestRule();

	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private final String extension1;

	private final String extension2;

	@Parameters(name = "{index}: {0} - {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "perf_outline", "perf_text" }, { "perf_basic", "perf_outline" } });
	}

	/**
	 * Constructor.
	 */
	public EditorSwitchTest(String extension1, String extension2) {
		this.extension1 = extension1;
		this.extension2 = extension2;
	}

	/**
	 * Test editor opening performance. This test always fails.
	 */
	@Test
	public void test() throws CoreException {

		// Open both files outside the loop so as not to include
		// the initial time to open, just switching.
		IWorkbenchWindow window = openTestWindow(UIPerformanceTestRule.PERSPECTIVE1);
		final IWorkbenchPage activePage = window.getActivePage();
		final IFile file1 = getTestProject().getFile("1." + extension1);
		assertTrue(file1.exists());
		final IFile file2 = getTestProject().getFile("1." + extension2);
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

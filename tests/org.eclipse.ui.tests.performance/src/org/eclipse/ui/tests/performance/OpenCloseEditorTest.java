/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.exercise;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.PerformanceTestCaseJunit4;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class OpenCloseEditorTest extends PerformanceTestCaseJunit4 {

	@ClassRule
	public static final UIPerformanceTestRule uiPerformanceTestRule = new UIPerformanceTestRule();

	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private final String extension;

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "perf_basic" }, { "perf_outline" }, { "perf_text" } });
	}


	public OpenCloseEditorTest(String extension) {
		this.extension = extension;
	}

	@Test
	public void test() throws Throwable {
		final IFile file = getTestProject().getFile("1." + extension);
		assertTrue(file.exists());

		IWorkbenchWindow window = openTestWindow(UIPerformanceTestRule.PERSPECTIVE1);
		final IWorkbenchPage activePage = window.getActivePage();

		exercise(() -> {
			startMeasuring();
			for (int j = 0; j < 10; j++) {
				IEditorPart part;
				try {
					part = IDE.openEditor(activePage, file, true);
				} catch (PartInitException e) {
					throw new AssertionError("Can't open editor for " + file.getName());
				}
				processEvents();
				activePage.closeEditor(part, false);
				processEvents();

			}
			stopMeasuring();
		});

		if (extension.equals("perf_text")) {
			tagAsSummary("UI - Open/Close Editor", Dimension.ELAPSED_PROCESS);
		}
		commitMeasurements();
		assertPerformance();
	}
}

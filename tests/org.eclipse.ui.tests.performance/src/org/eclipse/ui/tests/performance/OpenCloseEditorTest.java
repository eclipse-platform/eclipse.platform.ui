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
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.reportTimings;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
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

/**
 * Measures how long it takes to open and to close an editor, and reports the
 * two phases separately.
 */
@RunWith(Parameterized.class)
public class OpenCloseEditorTest {

	/**
	 * Enough pairs to get the editor implementation loaded and compiled. Below
	 * roughly this many, the reported times are dominated by JIT warm-up.
	 */
	private static final int WARMUP_PAIRS = 20;

	private static final int MIN_PAIRS = 50;

	private static final int MAX_PAIRS = 300;

	private static final int MAX_MEASURE_TIME_MS = 10000;

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

		// Class loading and JIT warm-up of the editor implementation would otherwise
		// end up in the reported times.
		for (int i = 0; i < WARMUP_PAIRS; i++) {
			openAndClose(activePage, file, null, null);
		}
		EditorTestHelper.calmDown(500, 30000, 500);

		List<Long> openTimes = new ArrayList<>();
		List<Long> closeTimes = new ArrayList<>();

		exercise(() -> openAndClose(activePage, file, openTimes, closeTimes), MIN_PAIRS, MAX_PAIRS,
				MAX_MEASURE_TIME_MS);

		reportTimings("OpenCloseEditor[" + extension + "] open", openTimes);
		reportTimings("OpenCloseEditor[" + extension + "] close", closeTimes);
	}

	/**
	 * Opens and closes the editor, recording the two phases separately when the
	 * given lists are not {@code null}.
	 */
	private static void openAndClose(IWorkbenchPage page, IFile file, List<Long> openTimes, List<Long> closeTimes) {
		long beforeOpen = System.nanoTime();
		IEditorPart part;
		try {
			part = IDE.openEditor(page, file, true);
		} catch (PartInitException e) {
			throw new AssertionError("Can't open editor for " + file.getName());
		}
		processEvents();
		long afterOpen = System.nanoTime();
		assertNotNull("No editor opened for " + file.getName(), part);

		page.closeEditor(part, false);
		processEvents();
		long afterClose = System.nanoTime();

		if (openTimes != null) {
			openTimes.add(afterOpen - beforeOpen);
			closeTimes.add(afterClose - afterOpen);
		}
	}
}

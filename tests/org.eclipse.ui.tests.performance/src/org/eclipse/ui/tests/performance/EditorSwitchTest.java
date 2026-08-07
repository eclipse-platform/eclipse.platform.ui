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
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.exercise;
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.reportTimings;
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.waitForBackgroundJobs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
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
 * Measures how long it takes to switch to an editor that is already open, and
 * reports each switch direction separately.
 */
@RunWith(Parameterized.class)
public class EditorSwitchTest {

	/**
	 * Enough pairs to get the editor implementation loaded and compiled. Below
	 * roughly this many, the reported times are dominated by JIT warm-up.
	 */
	private static final int WARMUP_PAIRS = 20;

	private static final int MIN_PAIRS = 60;

	private static final int MAX_PAIRS = 400;

	private static final int MAX_MEASURE_TIME_MS = 10000;

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
	 * Measures switching back and forth between two editors that are already open.
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

		// Class loading and JIT warm-up would otherwise end up in the reported times.
		for (int i = 0; i < WARMUP_PAIRS; i++) {
			switchTo(activePage, file1, null);
			switchTo(activePage, file2, null);
		}
		EditorTestHelper.calmDown(500, 30000, 500);

		List<Long> toFirst = new ArrayList<>();
		List<Long> toSecond = new ArrayList<>();

		exercise(() -> {
			switchTo(activePage, file1, toFirst);
			switchTo(activePage, file2, toSecond);
		}, MIN_PAIRS, MAX_PAIRS, MAX_MEASURE_TIME_MS);

		reportTimings("EditorSwitch to [" + extension1 + "]", toFirst);
		reportTimings("EditorSwitch to [" + extension2 + "]", toSecond);
	}

	/**
	 * Reveals the already open editor for the given file, recording the time when
	 * the given list is not {@code null}.
	 */
	private static void switchTo(IWorkbenchPage page, IFile file, List<Long> times) throws PartInitException {
		long before = System.nanoTime();
		IDE.openEditor(page, file, true);
		processEvents();
		long after = System.nanoTime();
		assertEquals("Wrong editor active after switching to " + file.getName(), file,
				page.getActiveEditor().getEditorInput().getAdapter(IFile.class));

		if (times != null) {
			times.add(after - before);
		}
	}
}

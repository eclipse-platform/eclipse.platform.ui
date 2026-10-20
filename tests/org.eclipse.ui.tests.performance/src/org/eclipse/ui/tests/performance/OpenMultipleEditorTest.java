/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
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
 * Measures how long it takes to fill the editor area with many editors and to
 * tear it down again.
 * <p>
 * The opens are reported for the start and for the end of the batch separately,
 * because the cost of an open grows with the number of editors already open and
 * a single distribution over the whole batch would hide that.
 */
@RunWith(Parameterized.class)
public class OpenMultipleEditorTest {

	/**
	 * Stays below the REUSE_EDITORS threshold, which defaults to 99. Above it the
	 * workbench recycles the oldest editor, so further opens would measure a reuse
	 * instead of an open.
	 */
	private static final int EDITOR_COUNT = 90;

	/**
	 * How many opens at each end of the batch are reported. The ones in between
	 * only interpolate.
	 */
	private static final int EDGE = 25;

	/** A closeAll round yields a single sample, so a few rounds are the minimum. */
	private static final int MIN_ROUNDS = 3;

	private static final int MAX_ROUNDS = 10;

	private static final int MAX_MEASURE_TIME_MS = 10000;

	@ClassRule
	public static final UIPerformanceTestRule uiPerformanceTestRule = new UIPerformanceTestRule();

	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private final String extension;
	private final boolean closeAll;

	@Parameters(name = "{index}: {0} - closeAll: {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "perf_basic", true }, { "perf_outline", true }, { "perf_text", true },
				{ "perf_basic", false }, { "perf_outline", false }, { "perf_text", false } });
	}

	public OpenMultipleEditorTest(String extension, boolean closeAll) {
		this.extension = extension;
		this.closeAll = closeAll;
	}

	@Test
	public void test() throws CoreException {
		IWorkbenchWindow window = openTestWindow(UIPerformanceTestRule.PERSPECTIVE1);
		final IWorkbenchPage activePage = window.getActivePage();

		// Class loading and JIT warm-up of the editor implementation would otherwise
		// end up in the reported times.
		openAndCloseAll(activePage, null);
		EditorTestHelper.calmDown(500, 30000, 500);

		Timings timings = new Timings();
		exercise(() -> openAndCloseAll(activePage, timings), MIN_ROUNDS, MAX_ROUNDS, MAX_MEASURE_TIME_MS);

		String label = "OpenMultipleEditor[" + extension + "]";
		reportTimings(label + " open 1.." + EDGE, timings.earlyOpens);
		reportTimings(label + " open " + (EDITOR_COUNT - EDGE + 1) + ".." + EDITOR_COUNT, timings.lateOpens);
		reportTimings(label + (closeAll ? " closeAll of " + EDITOR_COUNT : " close each"), timings.closes);
	}

	/**
	 * Opens an editor for every test file of the extension under test and closes
	 * them all again, recording the individual operations when the given timings
	 * are not {@code null}.
	 */
	private void openAndCloseAll(IWorkbenchPage page, Timings timings) throws PartInitException {
		for (int i = 0; i < EDITOR_COUNT; i++) {
			IFile file = getTestProject().getFile(i + "." + extension);
			assertTrue("Missing test file " + file.getName(), file.exists());

			long before = System.nanoTime();
			IEditorPart part = IDE.openEditor(page, file, true);
			processEvents();
			long elapsed = System.nanoTime() - before;
			assertNotNull("No editor opened for " + file.getName(), part);

			if (timings != null) {
				if (i < EDGE) {
					timings.earlyOpens.add(elapsed);
				} else if (i >= EDITOR_COUNT - EDGE) {
					timings.lateOpens.add(elapsed);
				}
			}
		}
		assertEquals("Not all editors stayed open, so an open measured a reuse", EDITOR_COUNT,
				page.getEditorReferences().length);

		if (closeAll) {
			long before = System.nanoTime();
			page.closeAllEditors(false);
			processEvents();
			long elapsed = System.nanoTime() - before;
			if (timings != null) {
				timings.closes.add(elapsed);
			}
		} else {
			for (IEditorReference reference : page.getEditorReferences()) {
				long before = System.nanoTime();
				page.closeEditor(reference.getEditor(false), false);
				processEvents();
				long elapsed = System.nanoTime() - before;
				if (timings != null) {
					timings.closes.add(elapsed);
				}
			}
		}
		assertEquals("Editors left open", 0, page.getEditorReferences().length);
	}

	private static final class Timings {
		final List<Long> earlyOpens = new ArrayList<>();
		final List<Long> lateOpens = new ArrayList<>();
		final List<Long> closes = new ArrayList<>();
	}
}

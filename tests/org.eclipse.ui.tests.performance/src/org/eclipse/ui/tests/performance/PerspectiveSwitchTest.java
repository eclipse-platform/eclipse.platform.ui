/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Measures switching back and forth between two perspectives, and reports each
 * switch direction separately.
 */
@RunWith(Parameterized.class)
public class PerspectiveSwitchTest {

	/**
	 * Enough switches to get the participating parts loaded and compiled. Below
	 * roughly this many, the reported times are dominated by JIT warm-up.
	 */
	private static final int WARMUP_PAIRS = 5;

	private static final int MIN_PAIRS = 5;

	private static final int MAX_PAIRS = 50;

	private static final int MAX_MEASURE_TIME_MS = 12000;

	@ClassRule
	public static final UIPerformanceTestRule uiPerformanceTestRule = new UIPerformanceTestRule();

	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private final String id1;
	private final String id2;
	private final String activeEditor;

	@Parameters(name = "{index}: {0}, {1}, editor {2}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { // Test switching between the two most commonly used perspectives in the
												// SDK
				// (this is the most important
				// perspective switch test, but it is easily affected by changes in JDT, etc.)
				{ "org.eclipse.jdt.ui.JavaPerspective", "org.eclipse.debug.ui.DebugPerspective", "1.java" },

				{ UIPerformanceTestRule.PERSPECTIVE1, UIPerformanceTestRule.PERSPECTIVE2, "1.perf_basic" },

				// Test switching between a perspective with lots of actions and a perspective
				// with none
				{ "org.eclipse.jdt.ui.JavaPerspective", "org.eclipse.ui.tests.util.EmptyPerspective", "1.perf_basic" },

				{ "org.eclipse.ui.resourcePerspective", "org.eclipse.jdt.ui.JavaPerspective", "1.java" } });
	}

	public PerspectiveSwitchTest(String id1, String id2, String activeEditor) {
		this.id1 = id1;
		this.id2 = id2;
		this.activeEditor = activeEditor;
	}

	@Test
	public void test() throws CoreException, WorkbenchException {
		// Get the two perspectives to switch between.
		final IPerspectiveRegistry registry = WorkbenchPlugin.getDefault().getPerspectiveRegistry();
		final IPerspectiveDescriptor perspective1 = registry.findPerspectiveWithId(id1);
		final IPerspectiveDescriptor perspective2 = registry.findPerspectiveWithId(id2);

		// The parameters reference JDT perspectives, which are not part of every target
		// platform. Skip visibly rather than reporting a pass for something that was
		// never measured.
		assumeTrue("Perspective not available: " + id1, perspective1 != null);
		assumeTrue("Perspective not available: " + id2, perspective2 != null);

		// Open the two perspectives and the file, in a new window.
		// Do this outside the loop so as not to include
		// the initial time to open, just switching.
		IWorkbenchWindow window = openTestWindow(id1);
		final IWorkbenchPage page = window.getActivePage();
		assertNotNull(page);
		page.setPerspective(perspective2);

		IFile aFile = getTestProject().getFile(activeEditor);
		assertTrue("Missing test file " + activeEditor + ", the fixture does not create it", aFile.exists());

		IDE.openEditor(page, aFile, true);

		// Class loading and JIT warm-up would otherwise end up in the reported times.
		for (int i = 0; i < WARMUP_PAIRS; i++) {
			switchTo(page, perspective1, null);
			switchTo(page, perspective2, null);
		}
		EditorTestHelper.calmDown(500, 30000, 500);

		List<Long> toFirst = new ArrayList<>();
		List<Long> toSecond = new ArrayList<>();

		exercise(() -> {
			switchTo(page, perspective1, toFirst);
			switchTo(page, perspective2, toSecond);
		}, MIN_PAIRS, MAX_PAIRS, MAX_MEASURE_TIME_MS);

		reportTimings("PerspectiveSwitch to [" + id1 + "]", toFirst);
		reportTimings("PerspectiveSwitch to [" + id2 + "]", toSecond);
	}

	/**
	 * Switches to the given perspective, recording the time when the given list is
	 * not {@code null}.
	 */
	private static void switchTo(IWorkbenchPage page, IPerspectiveDescriptor perspective, List<Long> times) {
		long before = System.nanoTime();
		page.setPerspective(perspective);
		processEvents();
		long after = System.nanoTime();
		assertEquals("Wrong perspective active", perspective, page.getPerspective());

		if (times != null) {
			times.add(after - before);
		}
	}
}

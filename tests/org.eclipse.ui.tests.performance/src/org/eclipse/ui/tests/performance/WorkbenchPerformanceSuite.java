/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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

import java.util.ArrayList;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.performance.layout.PerspectiveWidgetFactory;
import org.eclipse.ui.tests.performance.layout.ResizeTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.1
 */
class WorkbenchPerformanceSuite extends TestSuite {

	private static String RESOURCE_PERSPID = "org.eclipse.ui.resourcePerspective";
	// Note: to test perspective switching properly, we need perspectives with lots of
	// associated actions.
	// NOTE - do not change the order of the IDs below.  the PerspectiveSwitchTest has a
	// fingerprint test for performance that releys on this not changing.
	public static final String [] PERSPECTIVE_IDS = {
		EmptyPerspective.PERSP_ID2,
		UIPerformanceTestSetup.PERSPECTIVE1,
		RESOURCE_PERSPID,
		"org.eclipse.jdt.ui.JavaPerspective",
		"org.eclipse.debug.ui.DebugPerspective"};

	// Perspective ID to use for the resize window fingerprint test
	public static String resizeFingerprintTest = RESOURCE_PERSPID;

	public static final String [][] PERSPECTIVE_SWITCH_PAIRS = {
		// Test switching between the two most commonly used perspectives in the SDK (this is the most important
		// perspective switch test, but it is easily affected by changes in JDT, etc.)
		{"org.eclipse.jdt.ui.JavaPerspective", "org.eclipse.debug.ui.DebugPerspective", "1.java"},

		{UIPerformanceTestSetup.PERSPECTIVE1, UIPerformanceTestSetup.PERSPECTIVE2, "1.perf_basic"},

		{"org.eclipse.ui.tests.dnd.dragdrop", "org.eclipse.ui.tests.fastview_perspective", "1.perf_basic"},

		// Test switching between a perspective with lots of actions and a perspective with none
		{"org.eclipse.jdt.ui.JavaPerspective", "org.eclipse.ui.tests.util.EmptyPerspective", "1.perf_basic"},

		{RESOURCE_PERSPID, "org.eclipse.jdt.ui.JavaPerspective", "1.java"}
	};

	public static final String[] VIEW_IDS = {
		"org.eclipse.ui.views.ProblemView",
		"org.eclipse.ui.views.ResourceNavigator"
	};
	public static final int ITERATIONS = 25;

	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static Test suite() {
		return new WorkbenchPerformanceSuite();
	}

	/**
	 *
	 */
	public WorkbenchPerformanceSuite() {
		addLayoutScenarios();
		addResizeScenarios();
		addPerspectiveSwitchScenarios();
		addPerspectiveOpenCloseScenarios();
		addWindowOpenCloseScenarios();
		addContributionScenarios();
	}

	/**
	 *
	 */
	private void addContributionScenarios() {
		addTest(new ObjectContributionsPerformance(
				"large selection, limited contributors",
				ObjectContributionsPerformance.generateAdaptableSelection(
						ObjectContributionsPerformance.SEED, 5000),
				BasicPerformanceTest.NONE));
		addTest(new ObjectContributionsPerformance(
				"limited selection, limited contributors",
				ObjectContributionsPerformance.generateAdaptableSelection(
						ObjectContributionsPerformance.SEED, 50),
				BasicPerformanceTest.NONE));
	}

	/**
	 *
	 */
	private void addWindowOpenCloseScenarios() {
		for (String PERSPECTIVE_ID : PERSPECTIVE_IDS) {
			addTest(new OpenCloseWindowTest(PERSPECTIVE_ID, BasicPerformanceTest.NONE));
		}
	}

	/**
	 *
	 *
	 */
	private void addPerspectiveOpenCloseScenarios() {
		for (int i = 0; i < PERSPECTIVE_IDS.length; i++) {
			addTest(new OpenClosePerspectiveTest(PERSPECTIVE_IDS[i], i == 1 ? BasicPerformanceTest.LOCAL : BasicPerformanceTest.NONE));
		}
	}

	/**
	 *
	 */
	private void addPerspectiveSwitchScenarios() {
		for (String[] PERSPECTIVE_SWITCH_PAIR : PERSPECTIVE_SWITCH_PAIRS) {
			addTest(new PerspectiveSwitchTest(PERSPECTIVE_SWITCH_PAIR, BasicPerformanceTest.NONE));
		}
	}

	private void addLayoutScenarios() {
	}

	public static String[] getAllPerspectiveIds() {
		ArrayList<String> result = new ArrayList<>();
		IPerspectiveDescriptor[] perspectives = PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();

		for (IPerspectiveDescriptor descriptor : perspectives) {
			String id = descriptor.getId();
			result.add(id);
		}

		return result.toArray(new String[result.size()]);
	}

	/**
	 *
	 */
	private void addResizeScenarios() {
		String[] perspectiveIds = getAllPerspectiveIds();
		for (String id : perspectiveIds) {
			addTest(new ResizeTest(new PerspectiveWidgetFactory(id),
					id.equals(resizeFingerprintTest) ? BasicPerformanceTest.LOCAL : BasicPerformanceTest.NONE,
					"UI - Workbench Window Resize"));
		}
	}
}

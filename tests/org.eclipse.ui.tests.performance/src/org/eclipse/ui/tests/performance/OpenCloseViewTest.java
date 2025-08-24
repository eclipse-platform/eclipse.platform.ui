/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.waitForBackgroundJobs;

import java.util.Collection;

import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.PerformanceTestCaseJunit4;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Performance tests for showing views.
 * There are separate tests for showing a simple view (MockViewPart)
 * and a more complex view (Resource Navigator).
 * The views are shown in an empty perspective.
 */
@RunWith(Parameterized.class)
public class OpenCloseViewTest extends PerformanceTestCaseJunit4 {

	@ClassRule
	public static final UIPerformanceTestRule uiPerformanceTestRule = new UIPerformanceTestRule();

	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> data() {
		return ViewPerformanceUtil.getAllTestableViewIds().stream().map(id -> new Object[] { id }).toList();
	}

	private final String viewId;

	public OpenCloseViewTest(String viewId) {
		this.viewId = viewId;
	}

	@Test
	public void test() throws Throwable {
		IWorkbenchWindow window = openTestWindow();
		final IWorkbenchPage page = window.getActivePage();

		// prime it
		IViewPart view1 = page.showView(viewId);
		page.hideView(view1);
		waitForBackgroundJobs();
		processEvents();

		if (viewId.equals(ViewPerformanceUtil.PROJECT_EXPLORER)) {
			tagAsGlobalSummary("UI - Open/Close " + view1.getTitle(), Dimension.ELAPSED_PROCESS);
		}

		for (int j = 0; j < 100; j++) {

			startMeasuring();
			for (int i = 0; i < 5; i++) {
				IViewPart view = page.showView(viewId);
				processEvents();

				page.hideView(view);
				processEvents();
			}
			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();
	}
}

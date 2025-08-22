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

import java.util.Collection;

import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
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
public class OpenCloseViewTest extends BasicPerformanceTest {

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> data() {
		return ViewPerformanceUtil.getAllTestableViewIds().stream().map(
				id -> new Object[] { id, id.equals(ViewPerformanceUtil.PROJECT_EXPLORER) ? BasicPerformanceTest.GLOBAL
						: BasicPerformanceTest.NONE })
				.toList();
	}

	private final String viewId;

	public OpenCloseViewTest(String viewId, int tagging) {
		super("showView:" + viewId, tagging);
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

		tagIfNecessary("UI - Open/Close " + view1.getTitle(), Dimension.ELAPSED_PROCESS);
		if ("org.eclipse.ui.views.BookmarkView".equals(viewId))
			setDegradationComment("The test results are influenced by the test machine setup. See bug 340136.");

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

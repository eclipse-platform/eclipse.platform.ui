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

import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Performance tests for showing views.
 * There are separate tests for showing a simple view (MockViewPart)
 * and a more complex view (Resource Navigator).
 * The views are shown in an empty perspective.
 */
public class OpenCloseViewTest extends BasicPerformanceTest {

	private final String viewId;

	public OpenCloseViewTest(String viewId, int tagging) {
		super("showView:" + viewId, tagging);
		this.viewId = viewId;
	}

	@Override
	protected void runTest() throws Throwable {
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

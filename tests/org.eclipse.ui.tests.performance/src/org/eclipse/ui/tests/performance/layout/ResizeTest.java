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
package org.eclipse.ui.tests.performance.layout;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.waitForBackgroundJobs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.PerformanceTestCaseJunit4;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.performance.UIPerformanceTestRule;
import org.eclipse.ui.tests.performance.ViewPerformanceUtil;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Measures the time to resize the widget 10 times, including the time required
 * to redraw.
 *
 * @since 3.1
 */
@RunWith(Parameterized.class)
public class ResizeTest extends PerformanceTestCaseJunit4 {

	@ClassRule
	public static final UIPerformanceTestRule uiPerformanceTestRule = new UIPerformanceTestRule();

	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private static String RESOURCE_PERSPID = "org.eclipse.ui.resourcePerspective";
	// Note: to test perspective switching properly, we need perspectives with lots
	// of associated actions.
	// NOTE - do not change the order of the IDs below. the PerspectiveSwitchTest
	// has a fingerprint test for performance that relies on this not changing.
	private static final List<String> PERSPECTIVE_IDS = List.of( //
			EmptyPerspective.PERSP_ID2, //
			UIPerformanceTestRule.PERSPECTIVE1, //
			RESOURCE_PERSPID, //
			"org.eclipse.jdt.ui.JavaPerspective", //
			"org.eclipse.debug.ui.DebugPerspective");

	// Perspective ID to use for the resize window fingerprint test
	private static String resizeFingerprintTest = RESOURCE_PERSPID;

	private static final int xIterations = 5;

	private static final int yIterations = 5;

	private static final String tagString = "UI - Workbench Window Resize";

	private final TestWidgetFactory widgetFactory;

	private final boolean tagLocal;

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> data() {
		var configs = new ArrayList<Object[]>();
		configs.addAll(PERSPECTIVE_IDS.stream()
				.map(id -> new Object[] { new PerspectiveWidgetFactory(id), id.equals(resizeFingerprintTest) })
				.toList());
		configs.addAll(ViewPerformanceUtil.getAllTestableViewIds().stream()
				.map(id -> new Object[] { new ViewWidgetFactory(id), false }).toList());
		return configs;
	}

	/**
	 * Create a new instance of the receiver.
	 */
	public ResizeTest(TestWidgetFactory testWidgetFactory, boolean tagLocal) {
		this.widgetFactory = testWidgetFactory;
		this.tagLocal = tagLocal;
	}

	/**
	 * Run the test
	 */
	@Test
	public void test() throws CoreException, WorkbenchException {
		if (tagLocal) {
			tagAsSummary(tagString, Dimension.ELAPSED_PROCESS);
		}

		widgetFactory.init();
		final Composite widget = widgetFactory.getControl();
		Rectangle initialBounds = widget.getBounds();
		final Point maxSize = widgetFactory.getMaxSize();

		waitForBackgroundJobs();
		processEvents();
		for (int j = 0; j < 50; j++) {

			startMeasuring();
			for (int i = 0; i < 2; i++) {

				for (int xIteration = 0; xIteration < xIterations; xIteration += 5) {

					for (int yIteration = 0; yIteration < yIterations; yIteration++) {
						// Avoid giving the same x value twice in a row in order
						// to make it hard to cache
						int xSize = maxSize.x
								* ((xIteration + yIteration) % xIterations)
								/ xIterations;
						int ySize = maxSize.y * yIteration / yIterations;

						widget.setSize(xSize, ySize);

						processEvents();
					}

				}

			}
			stopMeasuring();
		}
		commitMeasurements();
		assertPerformance();

		widget.setBounds(initialBounds);
		widgetFactory.done();
	}

}

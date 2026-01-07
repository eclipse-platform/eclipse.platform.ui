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
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.performance.UIPerformanceTestRule;
import org.eclipse.ui.tests.performance.ViewPerformanceUtil;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Measures the time to resize the widget 10 times, including the time required
 * to redraw.
 *
 * @since 3.1
 */
public class ResizeTest {

	@RegisterExtension
	static UIPerformanceTestRule uiPerformanceTestRule = new UIPerformanceTestRule();

	@RegisterExtension
	CloseTestWindowsExtension closeTestWindows = new CloseTestWindowsExtension();

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

	public static Stream<Arguments> data() {
		var configs = new ArrayList<Arguments>();
		configs.addAll(PERSPECTIVE_IDS.stream()
				.map(id -> Arguments.of(new PerspectiveWidgetFactory(id), id.equals(resizeFingerprintTest)))
				.toList());
		configs.addAll(ViewPerformanceUtil.getAllTestableViewIds().stream()
				.map(id -> Arguments.of(new ViewWidgetFactory(id), false)).toList());
		return configs.stream();
	}

	/**
	 * Run the test
	 */
	@ParameterizedTest
	@MethodSource("data")
	public void test(TestWidgetFactory widgetFactory, boolean tagLocal, TestInfo testInfo) throws CoreException, WorkbenchException {
		Performance perf = Performance.getDefault();
		String scenarioId = this.getClass().getName() + "." + testInfo.getDisplayName();
		PerformanceMeter meter = perf.createPerformanceMeter(scenarioId);

		if (tagLocal) {
			perf.tagAsSummary(meter, tagString, Dimension.ELAPSED_PROCESS);
		}

		widgetFactory.init();
		final Composite widget = widgetFactory.getControl();
		Rectangle initialBounds = widget.getBounds();
		final Point maxSize = widgetFactory.getMaxSize();

		try {
			waitForBackgroundJobs();
			processEvents();
			for (int j = 0; j < 50; j++) {

				meter.start();
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
				meter.stop();
			}
			meter.commit();
			perf.assertPerformance(meter);
		} finally {
			meter.dispose();
			widget.setBounds(initialBounds);
			widgetFactory.done();
		}
	}

}

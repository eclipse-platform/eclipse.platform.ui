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
package org.eclipse.ui.tests.performance.layout;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Measures the time required to layout the widget 10 times. Does not include
 * the time required for any deferred repaints.
 *
 * @since 3.1
 */
public class LayoutTest {

	@RegisterExtension
	CloseTestWindowsExtension closeTestWindows = new CloseTestWindowsExtension();

	private final int xIterations = 100;

	private final int yIterations = 10;

	/**
	 * Run the test
	 */
	@ParameterizedTest
	@MethodSource("org.eclipse.ui.tests.performance.layout.LayoutPerformanceTestSuite#data")
	public void test(TestWidgetFactory widgetFactory, boolean flushState, TestInfo testInfo) throws CoreException, WorkbenchException {

		widgetFactory.init();
		final Composite widget = widgetFactory.getControl();
		final Point maxSize = widgetFactory.getMaxSize();
		Rectangle initialBounds = widget.getBounds();
		final Rectangle newBounds = Geometry.copy(initialBounds);

		Performance perf = Performance.getDefault();
		String scenarioId = this.getClass().getName() + "." + testInfo.getDisplayName();
		PerformanceMeter meter = perf.createPerformanceMeter(scenarioId);

		try {
			for (int xIteration = 0; xIteration < xIterations; xIteration++) {

				processEvents();

				meter.start();

				for (int yIteration = 0; yIteration < yIterations; yIteration++) {
					// Avoid giving the same x value twice in a row in order to make
					// it hard to cache
					int xSize = maxSize.x
							* ((xIteration + yIteration) % xIterations)
							/ xIterations;
					int ySize = maxSize.y * yIteration / yIterations;

					newBounds.width = xSize;
					newBounds.height = ySize;

					widget.setBounds(newBounds);
					widget.layout(flushState);
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


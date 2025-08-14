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
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;

/**
 * Measures the time required to layout the widget 10 times. Does not include
 * the time required for any deferred repaints.
 *
 * @since 3.1
 */
public class LayoutTest extends BasicPerformanceTest {

	private final TestWidgetFactory widgetFactory;

	private final int xIterations = 100;

	private final int yIterations = 10;

	private final boolean flushState;

	public LayoutTest(TestWidgetFactory widgetFactory, boolean flushState) {
		super(widgetFactory.getName() + " layout("
				+ (flushState ? "true" : "false") + ")");

		this.widgetFactory = widgetFactory;
		this.flushState = flushState;
	}

	/**
	 * Run the test
	 */
	@Override
	protected void runTest() throws CoreException, WorkbenchException {

		widgetFactory.init();
		final Composite widget = widgetFactory.getControl();
		final Point maxSize = widgetFactory.getMaxSize();
		Rectangle initialBounds = widget.getBounds();
		final Rectangle newBounds = Geometry.copy(initialBounds);

		for (int xIteration = 0; xIteration < xIterations; xIteration++) {

			processEvents();

			startMeasuring();

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

			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();

		widget.setBounds(initialBounds);
		widgetFactory.done();
	}
}


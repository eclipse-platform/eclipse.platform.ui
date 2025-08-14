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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;

/**
 * Measures the performance of a widget's computeSize method
 *
 * @since 3.1
 */
public class ComputeSizeTest extends BasicPerformanceTest {

	private final TestWidgetFactory widgetFactory;
	private final int xIterations = 10;
	private final int yIterations = 10;

	public ComputeSizeTest(TestWidgetFactory widgetFactory) {
		super(widgetFactory.getName() + " computeSize");

		this.widgetFactory = widgetFactory;
	}

	@Override
	protected void runTest() throws CoreException, WorkbenchException {

		widgetFactory.init();
		final Composite widget = widgetFactory.getControl();
		//Rectangle initialBounds = widget.getBounds();
		final Point maxSize = widgetFactory.getMaxSize();

		// Iteration counter. We increment this each pass through the loop in order to
		// generate slightly different test data each time
		final int[] counter = new int[] {0};

		for (int j = 0; j < 100; j++) {
			// This counter determines whether we're computing a width,
			// height, or fixed
			// size and whether or not we flush the cache.

			// We do things this way to avoid calling computeSize with the same (or
			// similar) values
			// twice in a row, which would be too easy to cache.
			int count = counter[0];

			startMeasuring();
			for (int i = 0; i < 200; i++) {

				for (int xIteration = 0; xIteration < xIterations; xIteration++) {

					for (int yIteration = 0; yIteration < yIterations; yIteration++) {
						// Avoid giving the same x value twice in a row in order to make it hard to cache
						int xSize = maxSize.x * ((xIteration + yIteration) % xIterations) / xIterations;
						int ySize = maxSize.y * yIteration / yIterations;

						// Alternate between flushing and not flushing the cache
						boolean flushState = (count % 2) != 0;

						// Alternate between width, height, and fixed, and default size queries
						// (note: we need to alternate in order to make the result hard to cache)
						switch(count % 4) {
							case 0: widget.computeSize(xSize, SWT.DEFAULT, flushState); break;
							case 1: widget.computeSize(SWT.DEFAULT, ySize, flushState); break;
							case 2: widget.computeSize(xSize, ySize, flushState); break;
							case 3: widget.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushState); break;
						}

						count++;
					}
				}

			}
			stopMeasuring();
			processEvents();
			counter[0]++;
		}

		commitMeasurements();
		assertPerformance();
		widgetFactory.done();
	}
}

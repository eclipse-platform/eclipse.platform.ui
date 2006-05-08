/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance.layout;

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

	private TestWidgetFactory widgetFactory;

	private int xIterations = 100;

	private int yIterations = 10;

	private boolean flushState;

	/**
	 * @param testName
	 */
	public LayoutTest(TestWidgetFactory widgetFactory, boolean flushState) {
		super(widgetFactory.getName() + " layout("
				+ (flushState ? "true" : "false") + ")");

		this.widgetFactory = widgetFactory;
		this.flushState = flushState;
	}

	/**
	 * Run the test
	 */
	protected void runTest() throws CoreException, WorkbenchException {

		widgetFactory.init();
		final Composite widget = widgetFactory.getControl();
		final Point maxSize = widgetFactory.getMaxSize();
		Rectangle initialBounds = widget.getBounds();
		final Rectangle newBounds = Geometry.copy(initialBounds);

		// This test is different now duw to trim API changes so 'gray' it...
       	setDegradationComment("<a href=https://bugs.eclipse.org/bugs/show_bug.cgi?id=129001>See Bug 129001</a> ");
		
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


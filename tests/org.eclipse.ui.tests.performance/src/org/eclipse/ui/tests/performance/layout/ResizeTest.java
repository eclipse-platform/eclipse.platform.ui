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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;

/**
 * Measures the time to resize the widget 10 times, including the time required
 * to redraw.
 * 
 * @since 3.1
 */
public class ResizeTest extends BasicPerformanceTest {

	private TestWidgetFactory widgetFactory;

	private int xIterations = 5;

	private int yIterations = 5;

	private String tagString;

	
	
	/**
	 * Create a new instance of the receiver.
	 * @param factory
	 */
	public ResizeTest(TestWidgetFactory factory) {
		this(factory, NONE, factory.getName() + " setSize");
	}

	
	
	/**
	 * Create a new instance of the receiver.
	 * @param factory
	 * @param tagging
	 * @param tag
	 */
	public ResizeTest(TestWidgetFactory factory, int tagging,
			String tag) {
		super(factory.getName() + " setSize", tagging);
		this.tagString = tag;
		this.widgetFactory = factory;
	}

	/**
	 * Run the test
	 */
	protected void runTest() throws CoreException, WorkbenchException {

		tagIfNecessary(tagString, Dimension.ELAPSED_PROCESS);

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

/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance.layout;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.test.performance.Performance;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;

/**
 * @since 3.1
 */
public class ResizeTest extends BasicPerformanceTest {

    private TestWidgetFactory widgetFactory;
    private int xIterations = 10;
    private int yIterations = 10;
    
    /**
     * @param testName
     */
    public ResizeTest(TestWidgetFactory widgetFactory) {
        super(widgetFactory.getName() + " setSize");
        
        this.widgetFactory = widgetFactory;
    }

    /**
     * Run the test
     */
    protected void runTest() throws CoreException, WorkbenchException {

        widgetFactory.init();
        Composite widget = widgetFactory.getControl();
        Rectangle initialBounds = widget.getBounds();
        Point maxSize = widgetFactory.getMaxSize();
    
        for (int xIteration = 0; xIteration < xIterations; xIteration++) {
            for (int yIteration = 0; yIteration < yIterations; yIteration++) {
                // Avoid giving the same x value twice in a row in order to make it hard to cache
                int xSize = maxSize.x * ((xIteration + yIteration) % xIterations) / xIterations;
                int ySize = maxSize.y * yIteration / yIterations;
                
                processEvents();
                
                performanceMeter.start();
                
                widget.setSize(xSize, ySize);
                // Try to ensure that the resize event wasn't deferred by asking for the bounds
                widget.getBounds();
            }
        }
        
        performanceMeter.commit();
        Performance.getDefault().assertPerformance(performanceMeter);
        
        widget.setBounds(initialBounds);
        widgetFactory.done();
    }
    
}

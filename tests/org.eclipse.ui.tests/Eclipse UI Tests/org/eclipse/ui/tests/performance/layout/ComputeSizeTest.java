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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.test.performance.Performance;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;

/**
 * @since 3.1
 */
public class ComputeSizeTest extends BasicPerformanceTest {

    private TestWidgetFactory widgetFactory;
    private int xHint;
    private int yHint;
    private boolean flushState;
    private int iterations = 100;
    
    /**
     * @param testName
     */
    public ComputeSizeTest(TestWidgetFactory widgetFactory, int xHint, int yHint, boolean flushState) {
        super(widgetFactory.getName() + " computeSize(" 
                + ((xHint == SWT.DEFAULT)? "SWT.DEFAULT" : "" + xHint) + ", "
                + ((yHint == SWT.DEFAULT)? "SWT.DEFAULT" : "" + yHint) + ", "
                + (flushState ? "true" : "false") + ")");
        
        this.widgetFactory = widgetFactory;
        this.flushState = flushState;
        this.xHint = xHint;
        this.yHint = yHint;
    }

    /**
     * Run the test
     */
    protected void runTest() throws CoreException, WorkbenchException {

        widgetFactory.init();
        Composite widget = widgetFactory.getControl();
        Point maxSize = widgetFactory.getMaxSize();
        
        for (int iteration = 0; iteration < iterations; iteration++) {
            
            processEvents();
            
            // Place some bogus size queries to reduce the chance of a cached value being returned
            widget.computeSize(100, SWT.DEFAULT, false);
            widget.computeSize(SWT.DEFAULT, 100, false);
            
            performanceMeter.start();
            
            widget.computeSize(xHint, yHint, flushState);
            
            performanceMeter.stop();                
            
        }
        
        performanceMeter.commit();
        Performance.getDefault().assertPerformance(performanceMeter);
        widgetFactory.done();
    }
}

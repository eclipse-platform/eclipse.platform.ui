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
package org.eclipse.ui.tests.performance;

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

    private String viewId;

    public OpenCloseViewTest(String viewId, int tagging) {
        super("showView:" + viewId, tagging);
        this.viewId = viewId;
    }

    protected void runTest() throws Throwable {
        IWorkbenchWindow window = openTestWindow();
        IWorkbenchPage page = window.getActivePage();
        
        // prime it
        IViewPart view = page.showView(viewId);
        page.hideView(view);
        processEvents();
        
       	tagIfNecessary("Open/Close View", new Dimension [] {Dimension.CPU_TIME, Dimension.USED_JAVA_HEAP});
                
        for (int i = 0; i < ViewPerformanceSuite.ITERATIONS; i++) {
            startMeasuring();
            view = page.showView(viewId);
            processEvents();
            stopMeasuring();
            page.hideView(view);
            processEvents();
        }
        
        commitMeasurements();
        assertPerformance();
    }
}
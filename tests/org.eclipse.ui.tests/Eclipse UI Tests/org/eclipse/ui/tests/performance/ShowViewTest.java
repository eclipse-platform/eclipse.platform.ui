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

import org.eclipse.test.performance.Performance;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.tests.api.MockViewPart;

/**
 * Performance tests for showing views.
 * There are separate tests for showing a simple view (MockViewPart)
 * and a more complex view (Resource Navigator).
 * The views are shown in an empty perspective.
 */
public class ShowViewTest extends BasicPerformanceTest {
    static final int RUNS = 20;

    public ShowViewTest(String testName) {
        super(testName);
    }

    private void measureShowView(String viewId) throws WorkbenchException {
        IWorkbenchWindow window = openTestWindow();
        IWorkbenchPage page = window.getActivePage();
        
        // prime it
        IViewPart view = page.showView(viewId);
        page.hideView(view);
        processEvents();
    	        
        for (int i = 0; i < RUNS; i++) {
            performanceMeter.start();
            view = page.showView(viewId);
            processEvents();
            performanceMeter.stop();
            page.hideView(view);
            processEvents();
        }
        performanceMeter.commit();
        Performance.getDefault().assertPerformance(performanceMeter);
   }

    /**
     * Test showing the MockViewPart.
     */
    public void testShowMockView() throws WorkbenchException {
        measureShowView(MockViewPart.ID);
    }

    /**
     * Test showing the Navigator.
     */
    public void testShowNavigator() throws WorkbenchException {
        measureShowView(IPageLayout.ID_RES_NAV);
   }
}
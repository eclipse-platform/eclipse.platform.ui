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

import org.eclipse.ui.IWorkbenchWindow;

/**
 * @since 3.1
 */
public class OpenCloseWindowTest extends BasicPerformanceTest {

    private String id;

    /**
     * @param testName
     */
    public OpenCloseWindowTest(String id) {
        super("testOpenCloseWindows:" + id);
        this.id = id;
    }
    
    protected void runTest() throws Throwable {
        for (int i = 0; i < WorkbenchPerformanceSuite.ITERATIONS; i++) {
            processEvents();
            EditorTestHelper.calmDown(500, 30000, 500);
            
            startMeasuring();
            IWorkbenchWindow window = openTestWindow(id);
            processEvents();   
            window.close();
            processEvents(); 
            stopMeasuring();
        }
        commitMeasurements();
        assertPerformance();
    }
}

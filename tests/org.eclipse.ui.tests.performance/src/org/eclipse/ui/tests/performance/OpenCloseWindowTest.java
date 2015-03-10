/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.performance;

import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * @since 3.1
 */
public class OpenCloseWindowTest extends BasicPerformanceTest {

    private String id;

    /**
     * @param tagging
     * @param testName
     */
    public OpenCloseWindowTest(String id, int tagging) {
        super("testOpenCloseWindows:" + id, tagging);
        this.id = id;
    }

    protected void runTest() throws Throwable {
    	tagIfNecessary("UI - Open/Close Window", Dimension.ELAPSED_PROCESS);

    	exercise(new TestRunnable() {
            public void run() throws Exception {
                processEvents();
                EditorTestHelper.calmDown(500, 30000, 500);

                startMeasuring();
                IWorkbenchWindow window = openTestWindow(id);
                processEvents();
                window.close();
                processEvents();
                stopMeasuring();
            }
        });

        commitMeasurements();
        assertPerformance();
    }
}

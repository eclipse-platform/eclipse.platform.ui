/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.rcp.performance;

import org.eclipse.swt.widgets.Display;
import org.eclipse.test.performance.PerformanceTestCase;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;

/**
 * @since 3.1
 */
public class PlatformUIPerfTest extends PerformanceTestCase {

    public void testCreateAndDisposeDisplayX100() {
        for (int i = 0; i < 10; ++i ) {
            startMeasuring();
            for (int j = 0; j < 100; ++j) {
            	Display display = PlatformUI.createDisplay();
                display.dispose();
                assertTrue(display.isDisposed());
            }
            stopMeasuring();
        }

        commitMeasurements();
        assertPerformance();
    }

    public void testRunAndShutdownWorkbench() {
        Display display = PlatformUI.createDisplay();

        for (int i = 0; i < 10; ++i ) {
            startMeasuring();
            int code = PlatformUI.createAndRunWorkbench(display, new WorkbenchAdvisorObserver(2));
            stopMeasuring();

            assertEquals(PlatformUI.RETURN_OK, code);
        }

        display.dispose();
        assertTrue(display.isDisposed());

        commitMeasurements();
        assertPerformance();
    }
}

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
package org.eclipse.ui.tests.rcp.performance;

import org.eclipse.swt.widgets.Display;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.test.performance.PerformanceTestCase;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.tests.rcp.util.OpenWorkbenchIntervalMonitor;
import org.eclipse.ui.tests.rcp.util.RestoreWorkbenchIntervalMonitor;

/**
 * @since 3.1
 */
public class EmptyWorkbenchPerfTest extends PerformanceTestCase {

    private static final int REPEAT_COUNT = 25;

    public void testOpen() {
        Display display = PlatformUI.createDisplay();
        Performance perf = Performance.getDefault();
        String baseScenarioId = perf.getDefaultScenarioId(this);
        PerformanceMeter startupMeter = perf.createPerformanceMeter( baseScenarioId + " [open]"); 
        PerformanceMeter shutdownMeter = perf.createPerformanceMeter( baseScenarioId + " [close]"); 

        tagAsSummary("Open RCP App", Dimension.CPU_TIME);
        for (int i = 0; i < REPEAT_COUNT; ++i ) {
        	startupMeter.start();
            int code = PlatformUI.createAndRunWorkbench(display,
            		new OpenWorkbenchIntervalMonitor(startupMeter, shutdownMeter));
            shutdownMeter.stop();
            assertEquals(PlatformUI.RETURN_OK, code);
        }

        display.dispose();
        assertTrue(display.isDisposed());
        startupMeter.commit();
        perf.assertPerformance(startupMeter);
        
        // The shutdown timer is currently < 50ms on all test machine. Due to the granularity of timers
        // and inherent Java variability, values below 100ms usually can not be interpreted.
        // Rather, check for the absolute value to be below threshold of 120ms.
        // If the test goes above it, it probably needs to be investigated.
        perf.assertPerformanceInAbsoluteBand(shutdownMeter, Dimension.CPU_TIME, 0, 120);
        
    	startupMeter.dispose();
    	shutdownMeter.dispose();
    }

    public void testRestore() {
        Display display = PlatformUI.createDisplay();
        Performance perf = Performance.getDefault();
        String baseScenarioId = perf.getDefaultScenarioId(this);
        PerformanceMeter startupMeter = perf.createPerformanceMeter( baseScenarioId + " [open]"); 
        PerformanceMeter shutdownMeter = perf.createPerformanceMeter( baseScenarioId + " [close]");
        
        // create an advisor that will just start the workbench long enough to create
        // something to be restored later
        PerformanceMeter startupMeter0 = perf.createPerformanceMeter( baseScenarioId + " [0][open]"); 
        PerformanceMeter shutdownMeter0 = perf.createPerformanceMeter( baseScenarioId + " [0][close]");
        WorkbenchAdvisor wa = new RestoreWorkbenchIntervalMonitor(startupMeter0, shutdownMeter0, true);
        int code = PlatformUI.createAndRunWorkbench(display, wa);
        assertEquals(PlatformUI.RETURN_RESTART, code);
        assertFalse(display.isDisposed());
    	startupMeter0.dispose();
    	shutdownMeter0.dispose();
       
        tagAsSummary("Restore RCP App", Dimension.CPU_TIME);
        
        // the rest is a bunch of code to restore the workbench and monitor performance
        // while doing so
        for (int i = 0; i < REPEAT_COUNT; ++i ) {
        	startupMeter.start();
            code = PlatformUI.createAndRunWorkbench(display,
                    new RestoreWorkbenchIntervalMonitor(startupMeter, shutdownMeter, false));
            shutdownMeter.stop();
            assertEquals(PlatformUI.RETURN_OK, code);
        }
        
        display.dispose();
        assertTrue(display.isDisposed());
        
        startupMeter.commit();
        perf.assertPerformance(startupMeter);
        
        // The shutdown timer is currently < 50ms on all test machine. Due to the granularity of timers
        // and inherit Java variability, values below 100ms usually can not be interpreted.
        // Rather, check for the absolute value to be below threshold of 120ms.
        // If the test goes above it, it probably needs to be investigated.
        perf.assertPerformanceInAbsoluteBand(shutdownMeter, Dimension.CPU_TIME, 0, 120);
        
    	startupMeter.dispose();
    	shutdownMeter.dispose();
    }
}

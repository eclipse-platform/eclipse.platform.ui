/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
import org.eclipse.test.performance.PerformanceTestCase;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.tests.rcp.util.IPerformanceMeterArray;
import org.eclipse.ui.tests.rcp.util.IntervalMeters;
import org.eclipse.ui.tests.rcp.util.RestoreWorkbenchIntervalMonitor;
import org.eclipse.ui.tests.rcp.util.OpenWorkbenchIntervalMonitor;

/**
 * @since 3.1
 */
public class EmptyWorkbenchPerfTest extends PerformanceTestCase {

    private static final int REPEAT_COUNT = 10;

    public void testOpen() {
        Display display = PlatformUI.createDisplay();

        Performance perf = Performance.getDefault();

        String baseScenarioId = perf.getDefaultScenarioId(this);
        IPerformanceMeterArray meters = new IntervalMeters(perf, baseScenarioId, OpenWorkbenchIntervalMonitor.intervalNames);
        tagAsSummary("Open RCP App", Dimension.CPU_TIME);
        for (int i = 0; i < REPEAT_COUNT; ++i ) {
            meters.start(OpenWorkbenchIntervalMonitor.firstInterval);
            int code = PlatformUI.createAndRunWorkbench(display,
                    new OpenWorkbenchIntervalMonitor(meters));
            meters.stop(OpenWorkbenchIntervalMonitor.finalInterval);

            assertEquals(PlatformUI.RETURN_OK, code);
        }

        display.dispose();
        assertTrue(display.isDisposed());

        meters.commit();
        meters.assertPerformance();
        meters.dispose();
    }

    public void testRestore() {
        Display display = PlatformUI.createDisplay();

        // create an advisor that will just start the workbench long enough to create
        // something to be restored later
        WorkbenchAdvisor wa = new RestoreWorkbenchIntervalMonitor();

        int code = PlatformUI.createAndRunWorkbench(display, wa);
        assertEquals(PlatformUI.RETURN_RESTART, code);
        assertFalse(display.isDisposed());

        // the rest is a bunch of code to restore the workbench and monitor performance
        // while doing so

        Performance perf = Performance.getDefault();

        String baseScenarioId = perf.getDefaultScenarioId(this);
        IPerformanceMeterArray meters = new IntervalMeters(perf, baseScenarioId, RestoreWorkbenchIntervalMonitor.intervalNames);

        tagAsSummary("Restore RCP App", Dimension.CPU_TIME);
        
        for (int i = 0; i < REPEAT_COUNT; ++i ) {
            meters.start(RestoreWorkbenchIntervalMonitor.firstInterval);
            code = PlatformUI.createAndRunWorkbench(display,
                    new RestoreWorkbenchIntervalMonitor(meters));
            meters.stop(RestoreWorkbenchIntervalMonitor.finalInterval);

            assertEquals(PlatformUI.RETURN_OK, code);
        }

        meters.commit();
        meters.assertPerformance();
        meters.dispose();

        display.dispose();
        assertTrue(display.isDisposed());
    }
}

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
package org.eclipse.ui.tests.rcp.util;

import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;

/**
 * A set of meters that should be used to check performance of intervals.  For example, this
 * collection is used to check performance between all stages of the workbench life-cycle
 * (the significant events are marked in the WorkbenchAdvisor callbacks).
 * 
 * @since 3.1
 */
public class IntervalMeters implements IPerformanceMeterArray {

    private Performance perf;
    private String baseScenarioId;

    /**
     * Clients instantiate the interval meter set as a specific size and are then responsible
     * for controlling by index.
     */
    private PerformanceMeter[] meters;

    /**
     * Create an set of performance meters using the argument array of strings in the name
     * for each one
     * @param intervals
     */
    public IntervalMeters(Performance perf, String baseScenarioId, String[] intervals) {
        this.perf = perf;
        this.baseScenarioId = baseScenarioId;

        meters = new PerformanceMeter[intervals.length];
        for (int i = 0; i < intervals.length; ++i) {
            meters[i] = perf.createPerformanceMeter(getScenarioId(intervals[i]));
        }
    }

    private String getScenarioId(String intervalName) {
        return baseScenarioId + " [" + intervalName + ']'; //$NON-NLS-1$
    }

    public void start(int meterIndex) {
        meters[meterIndex].start();
    }

    public void stop(int meterIndex) {
        meters[meterIndex].stop();
    }

    /**
     * The interval at the argument has completed.  Stop that meter and start the next.
     * @param completedIntervalIndex
     */
    public void intervalBoundary(int completedIntervalIndex) {
        meters[completedIntervalIndex].stop();
        meters[completedIntervalIndex + 1].start();
    }

    public void commit() {
        for (int i = 0; i < meters.length; ++i)
            meters[i].commit();
    }

    public void assertPerformance() {
        for (int i = 0; i < meters.length; ++i)
            perf.assertPerformance(meters[i]);
    }

    public void dispose() {
        for (int i = 0; i < meters.length; ++i)
            meters[i].dispose();
    }
}

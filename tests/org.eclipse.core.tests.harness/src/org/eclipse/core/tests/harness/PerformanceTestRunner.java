/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.harness;

import junit.framework.TestCase;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;

/**
 * Helper class for executing a performance test. Takes care of starting, stopping,
 * and commiting performance timers.
 */
public abstract class PerformanceTestRunner {

	/**
	 * Implemented by subclasses to perform the work to be measured.
	 */
	protected abstract void test();

	/**
	 * Executes the performance test the given number of times. Use the outer time
	 * to execute the test several times in order to obtain a normalized average. Use
	 * the inner loop for very fast tests that would otherwise be difficult to measure
	 * due to Java's poor timer granularity.  The inner loop is not needed for long
	 * tests that typically take more than a second to execute.
	 * 
	 * @param testCase The test that is running (used to obtain an appropriate meter)
	 * @param outer The number of repetitions of the test.
	 * @param inner The number of repetitions within the performance timer.
	 */
	public final void run(TestCase testCase, int outer, int inner) {
		Performance perf = Performance.getDefault();
		PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(testCase));
		try {
			for (int i = 0; i < outer; i++) {
				setUp();
				meter.start();
				for (int j = 0; j < inner; j++)
					test();
				meter.stop();
				tearDown();
			}
			meter.commit();
			perf.assertPerformance(meter);
		} catch (CoreException e) {
			CoreTest.fail("Failed performance test", e);
		} finally {
			meter.dispose();
		}
	}
	protected void setUp() throws CoreException{
		// subclasses to override
	}

	protected void tearDown() throws CoreException {
		// subclasses to override
	}
}
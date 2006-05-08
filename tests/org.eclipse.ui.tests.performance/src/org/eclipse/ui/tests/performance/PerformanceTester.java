/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance;

import junit.framework.TestCase;

import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;

/**
 * @since 3.1
 */
final class PerformanceTester {

	protected PerformanceMeter fPerformanceMeter;

	/**
	 * @param testCase
	 */
	public PerformanceTester(TestCase testCase) {
		Performance performance= Performance.getDefault();
		fPerformanceMeter= performance.createPerformanceMeter(performance.getDefaultScenarioId(testCase));
	}

	/**
	 * Asserts default properties of the measurements captured for this test
	 * case.
	 * 
	 * @throws RuntimeException if the properties do not hold
	 */
	public void assertPerformance() {
		Performance.getDefault().assertPerformance(fPerformanceMeter);
	}

	/**
	 * Asserts that the measurement specified by the given dimension is within a
	 * certain range with respect to some reference value. If the specified
	 * dimension isn't available, the call has no effect.
	 * 
	 * @param dim
	 *            the Dimension to check
	 * @param lowerPercentage
	 *            a negative number indicating the percentage the measured value
	 *            is allowed to be smaller than some reference value
	 * @param upperPercentage
	 *            a positive number indicating the percentage the measured value
	 *            is allowed to be greater than some reference value
	 * @throws RuntimeException
	 *            if the properties do not hold
	 */
	public void assertPerformanceInRelativeBand(Dimension dim, int lowerPercentage, int upperPercentage) {
		Performance.getDefault().assertPerformanceInRelativeBand(fPerformanceMeter, dim, lowerPercentage, upperPercentage);
	}

	public void commitMeasurements() {
		fPerformanceMeter.commit();
	}

	public void dispose() {
		fPerformanceMeter.dispose();
	}

	/**
	 * Called from within a test case immediately before the code to measure is
	 * run. It starts capturing of performance data. Must be followed by a call
	 * to {@link PerformanceTestCase#stopMeasuring()}before subsequent calls to
	 * this method or {@link PerformanceTestCase#commitMeasurements()}.
	 */
	public void startMeasuring() {
		fPerformanceMeter.start();
	}

	public void stopMeasuring() {
		fPerformanceMeter.stop();
	}

	/**
	 * Mark the scenario of this test case to be included both into the global
	 * and the local (component) performance summary. The summary shows the given dimension of the
	 * scenario and labels the scenario with the short name.
	 * 
	 * @param shortName
	 *            a short (shorter than 40 characters) descritive name of the scenario
	 * @param dimension
	 *            the dimension to show in the summary
	 */
	public void tagAsGlobalSummary(String shortName, Dimension dimension) {
		Performance.getDefault().tagAsGlobalSummary(fPerformanceMeter, shortName, new Dimension[] { dimension });
	}

	/**
	 * Mark the scenario represented by the given PerformanceMeter to be
	 * included into the global and the local (component) performance summary. The summary shows the given
	 * dimensions of the scenario and labels the scenario with the short name.
	 * 
	 * @param shortName
	 *            a short (shorter than 40 characters) descritive name of the scenario
	 * @param dimensions
	 *            an array of dimensions to show in the summary
	 */
	public void tagAsGlobalSummary(String shortName, Dimension[] dimensions) {
		Performance.getDefault().tagAsGlobalSummary(fPerformanceMeter, shortName, dimensions);
	}

	/**
	 * Mark the scenario of this test case to be included into the local (component)
	 * performance summary. The summary shows the given dimension of the
	 * scenario and labels the scenario with the short name.
	 * 
	 * @param shortName
	 *            a short (shorter than 40 characters) descriptive name of the scenario
	 * @param dimension
	 *            the dimension to show in the summary
	 */
	public void tagAsSummary(String shortName, Dimension dimension) {
		Performance.getDefault().tagAsSummary(fPerformanceMeter, shortName, new Dimension[] { dimension });
	}

	/**
	 * Mark the scenario represented by the given PerformanceMeter to be
	 * included into the local (component) performance summary. The summary shows the given
	 * dimensions of the scenario and labels the scenario with the short name.
	 * 
	 * @param shortName
	 *            a short (shorter than 40 characters) descriptive name of the scenario
	 * @param dimensions
	 *            an array of dimensions to show in the summary
	 */
	public void tagAsSummary(String shortName, Dimension[] dimensions) {
		Performance.getDefault().tagAsSummary(fPerformanceMeter, shortName, dimensions);
	}

	/**
	 * Set a degradation comment for the current meter.
	 * @param string
	 */
	public void setDegradationComment(String string) {
		Performance.getDefault().setComment(
				fPerformanceMeter, 
				Performance.EXPLAINS_DEGRADATION_COMMENT, 
				string);
		
	}
}

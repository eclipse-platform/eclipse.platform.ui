/*******************************************************************************
 *  Copyright (c) 2003, 2014 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM - Initial API and implementation
 *     Thirumala Reddy Mutchukota - Bug 432049, JobGroup API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import junit.framework.*;

/**
 * Runs all job tests
 */
public class AllTests extends TestCase {
	public AllTests() {
		super(null);
	}

	public AllTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTestSuite(YieldTest.class);
		suite.addTestSuite(IJobManagerTest.class);
		suite.addTestSuite(JobGroupTest.class);
		suite.addTestSuite(JobQueueTest.class);
		suite.addTestSuite(OrderedLockTest.class);
		suite.addTestSuite(BeginEndRuleTest.class);
		suite.addTestSuite(JobTest.class);
		suite.addTestSuite(DeadlockDetectionTest.class);
		suite.addTestSuite(Bug_129551.class);
		suite.addTestSuite(Bug_211799.class);
		suite.addTestSuite(Bug_307282.class);
		suite.addTestSuite(Bug_307391.class);
		suite.addTestSuite(MultiRuleTest.class);
		suite.addTestSuite(Bug_311756.class);
		suite.addTestSuite(Bug_311863.class);
		suite.addTestSuite(Bug_316839.class);
		suite.addTestSuite(Bug_320329.class);
		suite.addTestSuite(Bug_478634.class);
		suite.addTest(Bug_412138.suite());
		suite.addTestSuite(WorkerPoolTest.class);
		return suite;
	}
}

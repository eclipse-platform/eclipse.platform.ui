/**********************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
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
		suite.addTestSuite(IJobManagerTest.class);
		suite.addTestSuite(JobQueueTest.class);
		suite.addTestSuite(OrderedLockTest.class);
		suite.addTestSuite(BeginEndRuleTest.class);
		suite.addTestSuite(JobTest.class);
		suite.addTestSuite(DeadlockDetectionTest.class);
		return suite;
	}
}
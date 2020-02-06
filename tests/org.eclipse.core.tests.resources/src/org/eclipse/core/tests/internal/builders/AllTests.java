/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Broadcom Corp. - build configurations
 *******************************************************************************/
package org.eclipse.core.tests.internal.builders;

import junit.framework.*;

public class AllTests extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTestSuite(BuilderCycleTest.class);
		suite.addTestSuite(BuilderEventTest.class);
		suite.addTestSuite(BuilderNatureTest.class);
		suite.addTestSuite(BuilderTest.class);
		suite.addTestSuite(BuildDeltaVerificationTest.class);
		suite.addTestSuite(CustomBuildTriggerTest.class);
		suite.addTestSuite(EmptyDeltaTest.class);
		suite.addTestSuite(MultiProjectBuildTest.class);
		suite.addTestSuite(RelaxedSchedRuleBuilderTest.class);
		suite.addTestSuite(BuildConfigurationsTest.class);
		suite.addTestSuite(BuildContextTest.class);
		suite.addTestSuite(ParallelBuildChainTest.class);
		suite.addTest(new JUnit4TestAdapter(ComputeProjectOrderTest.class));
		return suite;
	}
}

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
	public AllTests() {
		super(null);
	}

	public AllTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTest(BuilderCycleTest.suite());
		suite.addTest(BuilderEventTest.suite());
		suite.addTest(BuilderNatureTest.suite());
		suite.addTest(BuilderTest.suite());
		suite.addTest(BuildDeltaVerificationTest.suite());
		suite.addTest(CustomBuildTriggerTest.suite());
		suite.addTest(EmptyDeltaTest.suite());
		suite.addTest(MultiProjectBuildTest.suite());
		suite.addTest(RelaxedSchedRuleBuilderTest.suite());
		suite.addTest(BuildConfigurationsTest.suite());
		suite.addTest(BuildContextTest.suite());
		suite.addTest(ParallelBuildChainTest.suite());
		suite.addTest(ComputeProjectOrderTest.suite());
		return suite;
	}
}

/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.session.samples;

import junit.framework.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.eclipse.core.tests.session.*;
import org.eclipse.test.performance.*;

public class MultipleRunsTest extends TestCase {
	public void testMultipleRuns() {
		// the test case to run multiple times
		TestCase test = new SampleSessionTest("testApplicationStartup");
		SessionTestRunner runner = new SessionTestRunner(EclipseWorkspaceTest.PI_HARNESS, SessionTestSuite.CORE_TEST_APPLICATION);
		// setup the command line to be passed to the multiple runs so it has the right system properties			
		Setup baseSetup = SetupManager.getInstance().getDefaultSetup();
		String[] perfCtrl = PerformanceSessionTestSuite.parsePerfCtrl();
		if (perfCtrl[0] != null)
			baseSetup.getSystemProperties().put(PerformanceSessionTestSuite.PROP_PERFORMANCE, perfCtrl[0]);
		runner.setBaseSetup(baseSetup);
		// runs the test case several times - only to collect data, won't do any assertions
		TestResult result = new TestResult();
		for (int i = 0; i < 5; i++) {
			runner.run(test, result, null);
			if (result.failureCount() > 0) {
				((TestFailure) result.failures().nextElement()).thrownException().printStackTrace();
				return;
			}
			if (result.errorCount() > 0) {
				((TestFailure) result.errors().nextElement()).thrownException().printStackTrace();
				return;
			}
		}
		// create a performance meter whose scenario id matches the one used in the test case run
		// our convention: scenario IDs are <test case class name> + '.' + <test case method name> 
		PerformanceMeter meter = Performance.getDefault().createPerformanceMeter(test.getClass().getName() + '.' + test.getName());
		// finally do the assertion
		Performance.getDefault().assertPerformanceInRelativeBand(meter, Dimension.ELAPSED_PROCESS, -50, 5);
	}
}
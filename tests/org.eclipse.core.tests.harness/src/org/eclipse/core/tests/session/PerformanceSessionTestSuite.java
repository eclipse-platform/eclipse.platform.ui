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
package org.eclipse.core.tests.session;

import junit.framework.*;
import org.eclipse.core.tests.session.SetupManager.SetupException;

/**
 * Runs perfomance test cases multiple times (if they don't fail), 
 * enabling assertions for the first run.
 */
public class PerformanceSessionTestSuite extends SessionTestSuite {

	/**
	 * This custom test result allows multiple test runs to show up as a 
	 * single run. 
	 */
	class ConsolidatedTestResult extends TestResult {
		private boolean failed;
		private int runs = 0;
		private boolean started = false;
		private TestResult target;

		public ConsolidatedTestResult(TestResult target) {
			this.target = target;
		}

		public void addError(Test test, Throwable t) {
			failed = true;
			target.addError(test, t);
		}

		public void addFailure(Test test, AssertionFailedError t) {
			failed = true;
			target.addFailure(test, t);
		}

		public void endTest(Test test) {
			runs++;
			if (!failed && runs < timesToRun)
				return;
			target.endTest(test);
		}

		public boolean shouldStop() {
			if (failed)
				return true;
			return target.shouldStop();
		}

		public void startTest(Test test) {
			// should not try to start again ater failing once
			if (failed)
				throw new IllegalStateException();
			if (started)
				return;
			started = true;
			target.startTest(test);
		}
	}

	public static final String PROP_PERFORMANCE = "perf_ctrl";

	private int timesToRun;

	/* 
	 * This method will not be needed when the performance testing API
	 * change to use multiple properties instead of perf_ctrl.
	 */
	public static String[] parsePerfCtrl() {
		String[] result = new String[2];
		String perfCtrl = System.getProperty(PROP_PERFORMANCE);
		if (perfCtrl == null || perfCtrl.trim().length() == 0)
			return result;
		int assertBeginning = perfCtrl.indexOf("assertAgainst=");
		if (assertBeginning == -1) {
			result[0] = perfCtrl;
			return result;
		}
		int assertEnding = perfCtrl.indexOf(';', assertBeginning);
		if (assertEnding == -1)
			assertEnding = perfCtrl.length();
		String assertAgainst = perfCtrl.substring(assertBeginning, assertEnding);
		StringBuffer newPerfCtrl = new StringBuffer();
		newPerfCtrl.append(perfCtrl.substring(0, assertBeginning));
		if (assertEnding + 1 < perfCtrl.length())
			newPerfCtrl.append(perfCtrl.substring(assertEnding + 1));
		result[0] = newPerfCtrl.toString();
		result[1] = assertAgainst;
		return result;
	}

	public PerformanceSessionTestSuite(String pluginId, int timesToRun) {
		super(pluginId);
		this.timesToRun = timesToRun;
	}

	public PerformanceSessionTestSuite(String pluginId, int timesToRun, Class theClass) {
		super(pluginId, theClass);
		this.timesToRun = timesToRun;
	}

	public PerformanceSessionTestSuite(String pluginId, int timesToRun, Class theClass, String name) {
		super(pluginId, theClass, name);
		this.timesToRun = timesToRun;
	}

	public PerformanceSessionTestSuite(String pluginId, int timesToRun, String name) {
		super(pluginId, name);
		this.timesToRun = timesToRun;
	}

	protected void runSessionTest(TestDescriptor descriptor, TestResult result) {
		try {
			fillTestDescriptor(descriptor);
		} catch (SetupException e) {
			result.addError(descriptor.getTest(), e.getCause());
			return;
		}
		// first component contains the property except assertAgainst=*
		// second component contains only assertAgainst=*
		String[] perfCtrl = parsePerfCtrl();
		if (perfCtrl[0] != null)
			descriptor.getSetup().setSystemProperty(PROP_PERFORMANCE, perfCtrl[0]);
		// run test cases n-1 times
		ConsolidatedTestResult consolidated = new ConsolidatedTestResult(result);
		for (int i = 0; !consolidated.shouldStop() && i < timesToRun - 1; i++)
			descriptor.run(consolidated);
		if (consolidated.shouldStop())
			return;
		// for the n-th run, enable assertions
		if (perfCtrl[1] != null)
			descriptor.getSetup().setSystemProperty(PROP_PERFORMANCE, perfCtrl[0] + ';' + perfCtrl[1]);
		descriptor.run(consolidated);
	}
}
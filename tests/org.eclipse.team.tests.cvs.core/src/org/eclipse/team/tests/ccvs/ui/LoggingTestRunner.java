/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.ui;


import junit.framework.Test;
import junit.framework.TestResult;
import junit.textui.TestRunner;

public class LoggingTestRunner extends TestRunner {
	protected LoggingTestResult logResult;
	
	protected TestResult createTestResult() {
		TestResult result = logResult;
		logResult = null;
		if (result == null) result = new LoggingTestResult(null);
		return result;
	}
	
	/**
	 * Runs a logging test suite.
	 * @param suite the test suite
	 * @param logResult the result object to use, or null to create a new one
	 * @param wait if true, pauses between test runs
	 */
	public void doRun(Test suite, LoggingTestResult logResult, boolean wait) {
		this.logResult = logResult;
		super.doRun(suite, wait);
	}
}

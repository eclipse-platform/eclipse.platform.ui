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

import java.util.Enumeration;
import junit.framework.*;

public class SessionTestSuite extends TestSuite {
	public static final String CORE_TEST_APPLICATION = "org.eclipse.pde.junit.runtime.coretestapplication"; //$NON-NLS-1$	
	public static final String UI_TEST_APPLICATION = "org.eclipse.pde.junit.runtime.uitestapplication"; //$NON-NLS-1$	
	protected String applicationId = CORE_TEST_APPLICATION;
	protected String pluginId;
	protected SessionTestRunner testRunner;

	public SessionTestSuite(String pluginId) {
		super();
		this.pluginId = pluginId;
	}

	public SessionTestSuite(String pluginId, Class theClass) {
		super(theClass);
		this.pluginId = pluginId;
	}

	public SessionTestSuite(String pluginId, Class theClass, String name) {
		super(theClass, name);
		this.pluginId = pluginId;
	}

	public SessionTestSuite(String pluginId, String name) {
		super(name);
		this.pluginId = pluginId;
	}

	public String getApplicationId() {
		return applicationId;
	}

	protected SessionTestRunner getTestRunner() {
		if (testRunner == null)
			testRunner = new SessionTestRunner(pluginId, applicationId);
		return testRunner;
	}

	public void runTest(Test test, TestResult result) {
		if (test instanceof TestCase)
			runTestCase((TestCase) test, result);
		else if (test instanceof TestSuite)
			// find and run the test cases that make up the suite
			runTestSuite((TestSuite) test, result);
		else
			// we don't support session tests for things that are not TestCases 
			// or TestSuites (e.g. TestDecorators) 
			test.run(result);
	}

	protected void runTestCase(TestCase test, TestResult result) {
		getTestRunner().run(test, result, null);
	}

	/*
	 * Traverses the test suite to find individual test cases to be run with the SessionTestRunner.
	 */
	protected void runTestSuite(TestSuite suite, TestResult result) {
		for (Enumeration e = suite.tests(); e.hasMoreElements();) {
			if (result.shouldStop())
				break;
			Test test = (Test) e.nextElement();
			runTest(test, result);
		}
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
}
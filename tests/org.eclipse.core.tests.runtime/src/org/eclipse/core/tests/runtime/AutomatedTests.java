/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import junit.framework.*;

/**
 * Runs the sniff tests for the build. All tests listed here should
 * be automated.
 */
public class AutomatedTests extends TestCase {

	public AutomatedTests() {
		super(null);
	}

	public AutomatedTests(String name) {
		super(name);
	}

	/**
	 * Add all of the AllTests suites for each package to be tested.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(org.eclipse.core.tests.runtime.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.runtime.compatibility.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.runtime.content.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.runtime.jobs.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.runtime.model.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.preferences.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.registrycache.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.runtime.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.dynamicregistry.AllTests.suite());
		return suite;
	}
}
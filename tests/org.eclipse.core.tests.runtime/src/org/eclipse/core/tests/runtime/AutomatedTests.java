/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
		TestSuite suite = new TestSuite(AutomatedTests.class.getName());

		// Moved to the top: bug 124867
		suite.addTest(org.eclipse.core.tests.internal.runtime.AllTests.suite());

		suite.addTest(org.eclipse.core.tests.runtime.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.runtime.jobs.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.preferences.AllTests.suite());

		// Moved to the top: bug 124867
		//suite.addTest(org.eclipse.core.tests.internal.runtime.AllTests.suite());

		return suite;
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.resources;

import junit.framework.*;

/**
 * The suite method for this class contains test suites for all automated tests in 
 * this test package.
 */
public class AllTests extends TestCase {
	/**
	 * Returns the test suite called by AutomatedTests and by JUnit test runners.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTest(ModelObjectReaderWriterTest.suite());
		suite.addTest(ProjectPreferencesTest.suite());
		suite.addTest(ResourceInfoTest.suite());
		suite.addTest(WorkspaceConcurrencyTest.suite());
		suite.addTest(WorkspacePreferencesTest.suite());
		return suite;
	}

	/**
	 * AllTests constructor comment.
	 */
	public AllTests() {
		super(null);
	}

	/**
	 * AllTests constructor comment.
	 * @param name java.lang.String
	 */
	public AllTests(String name) {
		super(name);
	}
}

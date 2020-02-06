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
 *     Broadcom Corp. - build configurations
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
		suite.addTestSuite(ModelObjectReaderWriterTest.class);
		suite.addTestSuite(ProjectPreferencesTest.class);
		suite.addTestSuite(ResourceInfoTest.class);
		suite.addTestSuite(WorkspaceConcurrencyTest.class);
		suite.addTestSuite(WorkspacePreferencesTest.class);
		suite.addTestSuite(ProjectReferencesTest.class);
		suite.addTestSuite(ProjectDynamicReferencesTest.class);
		suite.addTestSuite(ProjectBuildConfigsTest.class);
		suite.addTestSuite(Bug544975Test.class);
		return suite;
	}
}

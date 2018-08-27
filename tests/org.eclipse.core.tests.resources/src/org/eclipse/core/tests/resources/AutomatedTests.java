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
package org.eclipse.core.tests.resources;

import junit.framework.*;

/**
 * Runs the sniff tests for the build. All tests listed here should
 * be automated.
 */
public class AutomatedTests extends TestCase {

	public static final String PI_RESOURCES_TESTS = "org.eclipse.core.tests.resources"; //$NON-NLS-1$

	public AutomatedTests() {
		super(null);
	}

	public AutomatedTests(String name) {
		super(name);
	}

	/**
	 * Call each AllTests class from each of the test packages.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(AutomatedTests.class.getName());
		suite.addTest(org.eclipse.core.tests.filesystem.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.alias.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.builders.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.dtree.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.localstore.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.mapping.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.properties.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.propertytester.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.utils.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.watson.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.resources.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.resources.refresh.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.resources.regression.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.resources.usecase.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.resources.session.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.resources.content.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.events.AllTests.suite());
		// move last because WorkspacePreferenceTest breaks the Workspace state (https://bugs.eclipse.org/bugs/show_bug.cgi?id=525343)
		suite.addTest(org.eclipse.core.tests.internal.resources.AllTests.suite());
		return suite;
	}
}

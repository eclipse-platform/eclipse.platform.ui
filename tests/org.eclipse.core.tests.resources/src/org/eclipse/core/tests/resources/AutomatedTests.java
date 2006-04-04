/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		suite.addTest(org.eclipse.core.tests.internal.resources.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.utils.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.internal.watson.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.resources.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.resources.refresh.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.resources.regression.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.resources.usecase.AllTests.suite());
		suite.addTest(org.eclipse.core.tests.resources.session.AllTests.suite());
		return suite;
	}
}

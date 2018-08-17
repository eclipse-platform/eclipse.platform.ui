/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.core.tests.internal.registry;

import junit.framework.*;
import org.eclipse.core.tests.internal.registry.simple.AllSimpleRegistryTests;

public class AllTests extends TestCase {
	public AllTests() {
		super(null);
	}

	public AllTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTestSuite(ExtensionRegistryDynamicTest.class);
		suite.addTest(ExtensionRegistryStaticTest.suite());
		suite.addTest(NamespaceTest.suite());
		suite.addTest(AllSimpleRegistryTests.suite());
		suite.addTestSuite(StaleObjects.class);
		suite.addTestSuite(ContributorsTest.class);
		suite.addTest(ExtensionRegistryStaticTest.suite()); // test again
		suite.addTestSuite(RegistryListenerTest.class);
		suite.addTestSuite(InputErrorTest.class);
		suite.addTestSuite(MultiLanguageTest.class);
		return suite;
	}
}

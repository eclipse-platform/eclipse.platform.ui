/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		suite.addTest(ExtensionRegistryDynamicTest.suite());
		suite.addTest(ExtensionRegistryStaticTest.suite());
		suite.addTest(NamespaceTest.suite());
		suite.addTest(AllSimpleRegistryTests.suite());
		suite.addTestSuite(StaleObjects.class);
		suite.addTest(ContributorsTest.suite());
		suite.addTest(ExtensionRegistryStaticTest.suite()); // test again
		suite.addTest(RegistryListenerTest.suite());
		suite.addTest(InputErrorTest.suite());
		suite.addTest(MultiLanguageTest.suite());
		return suite;
	}
}

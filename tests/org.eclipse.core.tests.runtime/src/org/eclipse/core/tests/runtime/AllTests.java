/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.runtime;

import junit.framework.*;
import org.eclipse.core.tests.runtime.model.ConfigurationElementModelTest;


public class AllTests extends TestCase {
/**
 * AllTests constructor comment.
 * @param name java.lang.String
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
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(PathTest.suite());
	suite.addTest(PlatformTest.suite());
	suite.addTest(org.eclipse.core.tests.internal.runtime.AllTests.suite());
	suite.addTest(ConfigurationElementModelTest.suite());
	suite.addTest(org.eclipse.core.tests.internal.plugins.AllTests.suite());
	suite.addTest(org.eclipse.core.tests.internal.registrycache.AllTests.suite());
	return suite;
}
}

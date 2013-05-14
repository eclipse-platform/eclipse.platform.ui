/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import junit.framework.*;

public class AllTests extends TestCase {

	public AllTests() {
		super(null);
	}

	public AllTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTest(CoreExceptionTest.suite());
		suite.addTest(IAdapterManagerTest.suite());
		suite.addTest(IAdapterManagerServiceTest.suite());
		suite.addTest(AdapterManagerDynamicTest.suite());
		suite.addTest(OperationCanceledExceptionTest.suite());
		suite.addTest(PathTest.suite());
		suite.addTest(PlatformTest.suite());
		suite.addTest(PluginVersionIdentifierTest.suite());
		suite.addTestSuite(SubMonitorTest.class);
		suite.addTestSuite(SubProgressTest.class);
		suite.addTestSuite(SubMonitorSmallTicksTest.class);
		suite.addTest(ProgressMonitorWrapperTest.suite());
		suite.addTest(QualifiedNameTest.suite());
		suite.addTest(SafeRunnerTest.suite());
		suite.addTest(StatusTest.suite());
		suite.addTest(URIUtilTest.suite());
		suite.addTest(URLTest.suite());
		return suite;
	}
}

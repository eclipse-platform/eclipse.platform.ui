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
		suite.addTest(OperationCanceledExceptionTest.suite());
		suite.addTest(PathTest.suite());
		suite.addTest(PlatformTest.suite());
		suite.addTest(PluginVersionIdentifierTest.suite());
		suite.addTest(PreferenceExportTest.suite());
		suite.addTest(PreferenceForwarderTest.suite());
		suite.addTest(PreferencesTest.suite());
		suite.addTest(ProgressMonitorWrapperTest.suite());
		suite.addTest(QualifiedNameTest.suite());
		suite.addTest(StatusTest.suite());
		suite.addTest(URLTest.suite());
		return suite;
	}
}
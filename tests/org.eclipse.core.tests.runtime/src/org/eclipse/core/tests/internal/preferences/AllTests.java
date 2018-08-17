/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
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
package org.eclipse.core.tests.internal.preferences;

import junit.framework.*;
import org.eclipse.core.tests.runtime.*;

public class AllTests extends TestCase {
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

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTestSuite(EclipsePreferencesTest.class);
		suite.addTestSuite(PreferencesServiceTest.class);
		suite.addTestSuite(IScopeContextTest.class);
		suite.addTest(TestBug388004.suite());
		suite.addTest(TestBug380859.suite());
		suite.addTestSuite(PreferenceExportTest.class);
		suite.addTestSuite(PreferenceForwarderTest.class);
		suite.addTestSuite(PreferencesTest.class);
		return suite;
	}
}

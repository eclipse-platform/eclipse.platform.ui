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
package org.eclipse.core.tests.internal.runtime;

import junit.framework.*;

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
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTestSuite(LogSerializationTest.class);
		suite.addTestSuite(PlatformURLLocalTest.class);
		suite.addTest(PlatformURLSessionTest.suite());
		suite.addTest(FileLocatorTest.suite());
		return suite;
	}
}

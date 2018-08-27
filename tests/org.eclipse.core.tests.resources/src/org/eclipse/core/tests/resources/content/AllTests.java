/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.content;

import junit.framework.*;

/**
 * Runs all content type tests
 */
public class AllTests extends TestCase {
	public AllTests() {
		super(null);
	}

	public AllTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTest(IContentTypeManagerTest.suite());
		suite.addTest(SpecificContextTest.suite());
		suite.addTest(ContentDescriptionTest.suite());
		suite.addTest(XMLContentDescriberTest.suite());
		suite.addTest(LazyInputStreamTest.suite());
		suite.addTest(LazyReaderTest.suite());
		suite.addTest(TestBug94498.suite());
		return suite;
	}
}

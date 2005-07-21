/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.content;

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

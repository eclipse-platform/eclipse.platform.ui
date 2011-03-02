/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.webapp.service;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests utility classes and servlets used in Web Application
 */
public class AllWebappServiceTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(
				"org.eclipse.ua.tests.help.AllWebappServiceTests");
		//$JUnit-BEGIN$
		suite.addTestSuite(AdvancedSearchServiceTest.class);
		suite.addTestSuite(ContentServiceTest.class);
		suite.addTestSuite(ContextServiceTest.class);
		suite.addTestSuite(ExtensionServiceTest.class);
		suite.addTestSuite(IndexFragmentServiceTest.class);
		suite.addTestSuite(IndexServiceTest.class);
		suite.addTestSuite(SearchServiceTest.class);
		suite.addTestSuite(TocFragmentServiceTest.class);
		suite.addTestSuite(TocServiceTest.class);
		//$JUnit-END$
		return suite;
	}

}

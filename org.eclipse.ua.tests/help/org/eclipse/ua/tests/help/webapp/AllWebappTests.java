/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.webapp;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests utility classes and servlets used in Web Application
 */
public class AllWebappTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(
				"org.eclipse.ua.tests.help.AllWebappTests");
		//$JUnit-BEGIN$
		suite.addTestSuite(BrowserIdentificationTest.class);
		suite.addTestSuite(TopicPathTest.class);
		suite.addTestSuite(FilterTest.class);
		suite.addTestSuite(PluginsRootReplacement.class);
		suite.addTestSuite(UrlCoderTest.class);
		suite.addTestSuite(UrlUtilsTests.class);
		suite.addTestSuite(LocaleTest.class);
		suite.addTestSuite(PrintSubtopics.class);
		suite.addTestSuite(RestrictedTopicParameter.class);
		suite.addTestSuite(FilterExtensionTest.class);
		suite.addTestSuite(FragmentServletTest.class);
		suite.addTestSuite(HelpServerInterrupt.class);
		suite.addTestSuite(HelpServerBinding.class);
		suite.addTestSuite(HtmlCoderTest.class);
		suite.addTestSuite(TocZipTest.class);
		//$JUnit-END$
		return suite;
	}

}

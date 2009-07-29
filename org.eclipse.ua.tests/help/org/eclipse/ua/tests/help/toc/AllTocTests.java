/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.toc;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests help table of contents functionality.
 */
public class AllTocTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllTocTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllTocTests() {
		addTest(TocAssemblerTest.suite());
		addTest(EnabledTopicTest.suite());
		addTest(TocLinkChecker.suite());
		addTestSuite(TopicFinderTest.class);
		addTestSuite(TocSortingTest.class);
		addTestSuite(TopicSortingTest.class);
		addTestSuite(TocIconTest.class);
		addTestSuite(TocIconPathTest.class);
		addTestSuite(TocProviderTest.class);
		addTestSuite(HelpData.class);
	}
}

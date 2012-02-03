/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.remote;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests help keyword index functionality.
 */
public class AllRemoteTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllRemoteTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllRemoteTests() {
		addTestSuite(RemotePreferenceTest.class);	
		addTestSuite(TocServletTest.class);
		addTestSuite(SearchServletTest.class);
		addTestSuite(IndexServletTest.class);
		addTestSuite(ContentServletTest.class);
		addTestSuite(ContextServletTest.class);	
		addTestSuite(LoadTocUsingRemoteHelp.class);
		addTestSuite(SearchUsingRemoteHelp.class);
		addTestSuite(LoadIndexUsingRemoteHelp.class);
		addTest(GetContentUsingRemoteHelp.suite());
		addTestSuite(GetContextUsingRemoteHelp.class);
		addTestSuite(TocManagerTest.class);
		addTestSuite(SearchIndexCreation.class);
		addTestSuite(ParallelSearchUsingRemote.class);
		addTestSuite(ParallelSearchServletTest.class);
	}
}

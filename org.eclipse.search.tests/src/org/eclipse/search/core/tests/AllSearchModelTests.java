/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllSearchModelTests extends TestSuite {

	/*
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new AllSearchModelTests();
	}

	/**
	 * Construct the test suite.
	 */
	public AllSearchModelTests() {
		addTest(new TestSuite(QueryManagerTest.class));
		addTest(new TestSuite(TestSearchResult.class));
		addTest(new TestSuite(QueryManagerTest.class));
		addTest(new TestSuite(LineConversionTest.class));
	}

}

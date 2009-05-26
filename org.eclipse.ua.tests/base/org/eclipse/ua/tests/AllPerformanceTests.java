/*******************************************************************************
 *  Copyright (c) 2006, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ua.tests.cheatsheet.AllCheatSheetPerformanceTests;
import org.eclipse.ua.tests.help.AllHelpPerformanceTests;

/*
 * Tests all user assistance performance (automated).
 */
public class AllPerformanceTests extends TestSuite {

	/*
	 * Returns the entire performance test suite.
	 */
	public static Test suite() {
		return new AllPerformanceTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllPerformanceTests() {
		addTest(AllCheatSheetPerformanceTests.suite());
        addTest(AllHelpPerformanceTests.suite());
		
		/*
		 * Disabled due to inability to backport test to 3.2. Internal
		 * test hooks were added in 3.2.2 code base but do not exist in 3.2
		 * so the test will not be accurate.
		 */
		//addTest(AllIntroPerformanceTests.suite());

	}
}

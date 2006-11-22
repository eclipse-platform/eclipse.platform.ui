/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ua.tests.cheatsheet.performance.AllCheatSheetPerformanceTests;
import org.eclipse.ua.tests.help.performance.AllHelpPerformanceTests;
import org.eclipse.ua.tests.intro.performance.AllIntroPerformanceTests;

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
		addTest(AllHelpPerformanceTests.suite());
		addTest(AllIntroPerformanceTests.suite());
		addTest(AllCheatSheetPerformanceTests.suite());
	}
}

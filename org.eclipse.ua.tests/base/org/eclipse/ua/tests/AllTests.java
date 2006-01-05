/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

import org.eclipse.ua.tests.cheatsheet.AllCheatSheetTests;
import org.eclipse.ua.tests.intro.AllIntroTests;

/*
 * Tests all user assistance functionality (automated).
 */
public class AllTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllTests() {
		addTest(AllCheatSheetTests.suite());
		addTest(AllIntroTests.suite());
	}
}

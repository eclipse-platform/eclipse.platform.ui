/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.browser;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ua.tests.browser.external.AllExternalBrowserTests;
import org.eclipse.ua.tests.browser.other.AllOtherBrowserTests;

/*
 * Tests all cheat sheet functionality (automated).
 */
public class AllBrowserTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllBrowserTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllBrowserTests() {
		addTest(AllExternalBrowserTests.suite());
		addTest(AllOtherBrowserTests.suite());
	}
}

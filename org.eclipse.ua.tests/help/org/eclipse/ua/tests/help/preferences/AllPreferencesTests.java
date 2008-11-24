/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.preferences;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests help preferences functionality (automated).
 */
public class AllPreferencesTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllPreferencesTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllPreferencesTests() {
		addTest(ProductPreferencesTest.suite());
		addTest(HelpDataTest.suite());
		addTestSuite(CssPreferences.class);
		addTestSuite(BookmarksTest.class);
	}
}

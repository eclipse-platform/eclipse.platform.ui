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
package org.eclipse.ua.tests.cheatsheet.views;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests all cheat sheet view functionality (automated).
 */
public class AllViewTests extends TestSuite {

	/*
	 * Returns the test suite.
	 */
	public static Test suite() {
		return new AllViewTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllViewTests() {
		addTest(ActionTest.suite());
		addTest(ContentTest.suite());
		//addTest(ContextHelpTest.suite());
		addTest(SubItemsTest.suite());
		addTest(URLTest.suite());
	}
}

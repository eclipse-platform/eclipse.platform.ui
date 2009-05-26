/*******************************************************************************
 *  Copyright (c) 2008, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.intro.other;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests all intro parser functionality (automated).
 */
public class AllOtherTests extends TestSuite {

	/*
	 * Returns the test suite.
	 */
	public static Test suite() {
		return new AllOtherTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllOtherTests() {
		addTest(ReopenStateTest.suite());
		addTest(NormalizeWhitespaceTest.suite());
	}
}

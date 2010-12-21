/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.scope;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests help dynamic content functionality (automated).
 */
public class AllScopeTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllScopeTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllScopeTests() {
		addTestSuite(IntersectionTest.class);
		addTestSuite(ScopeHierarchy.class);
		addTestSuite(EmptyLeafRemoval.class);
		addTestSuite(ScopeSetManagerTest.class);
	}
}

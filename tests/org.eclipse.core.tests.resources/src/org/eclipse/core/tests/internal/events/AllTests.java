/*******************************************************************************
 * Copyright (c) 2017 Simeon Andreev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simeon Andreev <simeon.danailov.andreev@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.events;

import junit.framework.*;

public class AllTests extends TestCase {

	public AllTests() {
		this(null);
	}

	public AllTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());

		suite.addTest(BuildProjectFromMultipleJobsTest.suite());

		return suite;
	}
}

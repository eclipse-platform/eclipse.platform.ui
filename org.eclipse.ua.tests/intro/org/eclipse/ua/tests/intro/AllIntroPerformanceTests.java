/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.intro;

import org.eclipse.ua.tests.intro.performance.OpenIntroTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests help performance (automated).
 */
public class AllIntroPerformanceTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllIntroPerformanceTests();
	}

	/*
	 * Constructs a new performance test suite.
	 */
	public AllIntroPerformanceTests() {
		addTest(OpenIntroTest.suite());
	}
}

/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.performance;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Runs a test specified by the org.eclipse.ui.performance.test property.
 * This test is prepped via the UIPerformanceTestSetup test setup.
 *
 * @since 3.1
 */
public class PerformanceTestRunner extends TestSuite {

	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static Test suite() {
		return new UIPerformanceTestSetup(new PerformanceTestRunner());
	}

	public PerformanceTestRunner() {
		String className = System.getProperty("org.eclipse.ui.performance.test");
		try {
			@SuppressWarnings("unchecked")
			Class<? extends TestCase> clazz = (Class<? extends TestCase>) Class.forName(className);
			if (TestSuite.class.isAssignableFrom(clazz))
				addTest(clazz.newInstance());
			else
				addTestSuite(clazz);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

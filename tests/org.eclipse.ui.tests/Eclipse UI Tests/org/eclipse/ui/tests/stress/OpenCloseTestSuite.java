/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.stress;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test all areas of the UI API.
 */
public class OpenCloseTestSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new OpenCloseTestSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public OpenCloseTestSuite() {
		addTest(new TestSuite(OpenCloseTest.class));

	}
}

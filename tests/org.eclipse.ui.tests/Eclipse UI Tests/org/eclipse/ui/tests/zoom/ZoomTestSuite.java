/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.ui.tests.zoom;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * A test suite to test the zooming behavior of Eclipse.
 */
public class ZoomTestSuite extends TestSuite {
	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new ZoomTestSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public ZoomTestSuite() {
		addTest(new TestSuite(ZoomedViewActivateTest.class));
		addTest(new TestSuite(ZoomedEditorCloseTest.class));
		addTest(new TestSuite(ZoomedViewCloseTest.class));
		addTest(new TestSuite(ShowViewTest.class));
		addTest(new TestSuite(OpenEditorTest.class));
	}
}

/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.operations;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the platform operations support.
 */
public class OperationsTestSuite extends TestSuite {
	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static final Test suite() {
		return new OperationsTestSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public OperationsTestSuite() {
		addTest(new TestSuite(OperationsAPITest.class));
		addTest(new TestSuite(WorkbenchOperationHistoryTests.class));
		addTest(new TestSuite(MultiThreadedOperationsTests.class));
		addTest(new TestSuite(WorkbenchOperationStressTests.class));
		addTest(new TestSuite(WorkspaceOperationsTests.class));
	}
}

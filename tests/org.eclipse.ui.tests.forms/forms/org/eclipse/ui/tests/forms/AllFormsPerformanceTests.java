/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms;

import org.eclipse.ui.tests.forms.performance.FormsPerformanceTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests forms performance (automated).
 */
public class AllFormsPerformanceTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllFormsPerformanceTests();
	}

	/*
	 * Constructs a new performance test suite.
	 */
	public AllFormsPerformanceTests() {
		addTestSuite(FormsPerformanceTest.class);
	}
}

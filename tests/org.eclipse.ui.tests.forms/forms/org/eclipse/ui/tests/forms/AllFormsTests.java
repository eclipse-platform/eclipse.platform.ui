/*******************************************************************************
 * Copyright (c) 2007,2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alena Laskavaia - added ExpandableCompositeTest (Bug 481604)
 *     Ralf M Petter<ralf.petter@gmail.com> - Bug 259846
 *******************************************************************************/

package org.eclipse.ui.tests.forms;

import org.eclipse.ui.tests.forms.layout.AllLayoutTests;
import org.eclipse.ui.tests.forms.util.AllUtilityTests;
import org.eclipse.ui.tests.forms.widgets.AllWidgetsTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests all cheat sheet functionality (automated).
 */
public class AllFormsTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllFormsTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllFormsTests() {
		addTest(AllLayoutTests.suite());
		addTest(AllUtilityTests.suite());
		addTest(AllWidgetsTests.suite());
	}
}

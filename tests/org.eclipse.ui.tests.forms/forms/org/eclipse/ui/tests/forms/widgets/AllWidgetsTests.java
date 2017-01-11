/*******************************************************************************
 * Copyright (c) 2016 Ralf M Petter<ralf.petter@gmail.com> and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ralf M Petter<ralf.petter@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllWidgetsTests extends TestSuite {
	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllWidgetsTests();
	}

	/*
	 * Constructs a new form widgets test suite.
	 */
	public AllWidgetsTests() {
		addTestSuite(ExpandableCompositeTest.class);
		addTestSuite(FormTextModelTest.class);
	}
}

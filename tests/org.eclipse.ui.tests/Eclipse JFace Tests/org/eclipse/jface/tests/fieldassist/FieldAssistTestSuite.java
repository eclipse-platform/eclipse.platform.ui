/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.fieldassist;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the platform operations support.
 */
public class FieldAssistTestSuite extends TestSuite {
	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static final Test suite() {
		return new FieldAssistTestSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public FieldAssistTestSuite() {
		// disabled, see bug 275393...
		// addTest(new TestSuite(TextFieldAssistTests.class));
		// addTest(new TestSuite(ComboFieldAssistTests.class));
		addTest(new TestSuite(ControlDecorationTests.class));
		addTest(new TestSuite(FieldAssistAPITests.class));
	}
}

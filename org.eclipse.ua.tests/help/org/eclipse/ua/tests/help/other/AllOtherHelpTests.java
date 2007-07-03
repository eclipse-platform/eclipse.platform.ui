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

package org.eclipse.ua.tests.help.other;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllOtherHelpTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllOtherHelpTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllOtherHelpTests() {
		addTest(LinkUtilTest.suite());
	}
}

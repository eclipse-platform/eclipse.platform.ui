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
package org.eclipse.ui.tests.navigator;

import junit.framework.Test;
import junit.framework.TestSuite;

public final class NavigatorTestSuite extends TestSuite {

	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static final Test suite() {
		return new NavigatorTestSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public NavigatorTestSuite() {
		addTest(new TestSuite(ExtensionsTest.class));
		addTest(new TestSuite(WorkingSetTest.class));
		addTest(new TestSuite(ActivityTest.class));
		addTest(new TestSuite(OpenTest.class));
		addTest(new TestSuite(INavigatorContentServiceTests.class));
		addTest(new TestSuite(ProgrammaticOpenTest.class));
		addTest(new TestSuite(PipelineTest.class));
		addTest(new TestSuite(OverrideTest.class));
		
		// Takes too long
		//addTest(new TestSuite(CreateProjectTest.class));

	}

}

/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.services;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests general to services.
 * @since 3.3
 */
public final class ServicesTestSuite extends TestSuite {

	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static final Test suite() {
		return new ServicesTestSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public ServicesTestSuite() {
		addTest(new TestSuite(EvaluationServiceTest.class));
		addTest(ContributedServiceTest.suite());
		addTest(new TestSuite(WorkbenchSiteProgressServiceTest.class));
		addTest(new TestSuite(EditorSourceTest.class));
	}
}

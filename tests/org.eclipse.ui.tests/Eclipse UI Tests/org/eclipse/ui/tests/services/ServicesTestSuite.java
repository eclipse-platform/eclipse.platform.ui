/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474132
 *******************************************************************************/
package org.eclipse.ui.tests.services;

import org.junit.runner.RunWith;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests general to services.
 * @since 3.3
 */
@RunWith(org.junit.runners.AllTests.class)
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
		// TODO addTest(new TestSuite(EditorSourceTest.class));
	}
}

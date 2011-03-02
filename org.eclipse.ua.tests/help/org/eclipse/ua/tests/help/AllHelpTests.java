/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ua.tests.help.criteria.AllCriteriaTests;
import org.eclipse.ua.tests.help.dynamic.AllDynamicTests;
import org.eclipse.ua.tests.help.index.AllIndexTests;
import org.eclipse.ua.tests.help.other.AllOtherHelpTests;
import org.eclipse.ua.tests.help.preferences.AllPreferencesTests;
import org.eclipse.ua.tests.help.remote.AllRemoteTests;
import org.eclipse.ua.tests.help.scope.AllScopeTests;
import org.eclipse.ua.tests.help.search.AllSearchTests;
import org.eclipse.ua.tests.help.toc.AllTocTests;
import org.eclipse.ua.tests.help.webapp.AllWebappTests;
import org.eclipse.ua.tests.help.webapp.service.AllWebappServiceTests;

/*
 * Tests help functionality (automated).
 */
public class AllHelpTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllHelpTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllHelpTests() {
		addTest(AllPreferencesTests.suite());
		addTest(AllCriteriaTests.suite());
		addTest(AllDynamicTests.suite());
		addTest(AllSearchTests.suite());
		addTest(AllTocTests.suite());
		addTest(AllIndexTests.suite());
		addTest(AllWebappTests.suite());
		addTest(AllOtherHelpTests.suite());
		addTest(AllRemoteTests.suite());
		addTest(AllScopeTests.suite());
		addTest(AllWebappServiceTests.suite());
	}
}

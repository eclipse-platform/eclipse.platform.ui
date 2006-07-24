/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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

import org.eclipse.ua.tests.help.context.AllContextTests;
import org.eclipse.ua.tests.help.index.AllIndexTests;
import org.eclipse.ua.tests.help.preferences.AllPreferencesTests;
import org.eclipse.ua.tests.help.producer.AllProducerTests;
import org.eclipse.ua.tests.help.search.AllSearchTests;
import org.eclipse.ua.tests.help.toc.AllTocTests;

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
		addTest(AllContextTests.suite());
		addTest(AllPreferencesTests.suite());
		addTest(AllProducerTests.suite());
		addTest(AllSearchTests.suite());
		addTest(AllTocTests.suite());
		addTest(AllIndexTests.suite());
	}
}

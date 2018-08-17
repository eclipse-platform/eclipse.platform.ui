/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ua.tests.help;

import org.eclipse.ua.tests.help.performance.BuildHtmlSearchIndex;
import org.eclipse.ua.tests.help.performance.IndexAssemblePerformanceTest;
import org.eclipse.ua.tests.help.performance.HelpServerTest;
import org.eclipse.ua.tests.help.performance.TocAssemblePerformanceTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests help performance (automated).
 */
public class AllHelpPerformanceTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllHelpPerformanceTests();
	}

	/*
	 * Constructs a new performance test suite.
	 */
	public AllHelpPerformanceTests() {

		/*
		 * Disabled due to inability to get reliable results. Browser/SWT
		 * changes in timing of listener events no longer consistent in 3.3.
		 */

		//addTest(OpenHelpTest.suite());
		addTest(TocAssemblePerformanceTest.suite());
		addTest(IndexAssemblePerformanceTest.suite());
		addTest(BuildHtmlSearchIndex.suite());
		addTest(HelpServerTest.suite());
	}
}

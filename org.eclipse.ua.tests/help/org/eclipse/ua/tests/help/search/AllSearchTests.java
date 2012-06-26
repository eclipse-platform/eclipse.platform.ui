/*******************************************************************************
 *  Copyright (c) 2006, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.search;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests help functionality (automated).
 */
public class AllSearchTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllSearchTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllSearchTests() {
		addTest(ExtraDirTest.suite());
		addTest(BasicTest.suite());
		addTestSuite(WildcardTest.class);
		addTestSuite(LocaleTest.class);
		addTestSuite(AnalyzerTest.class);
		addTest(SearchCheatsheet.suite());
		addTest(SearchIntro.suite());
		addTest(EncodedCharacterSearch.suite());
		addTest(MetaKeywords.suite());
		addTest(SearchParticipantTest.suite());
		addTest(SearchParticipantXMLTest.suite());
		addTest(SearchRanking.suite());
		addTestSuite(WorkingSetManagerTest.class);
		addTestSuite(InfocenterWorkingSetManagerTest.class);
		addTestSuite(PrebuiltIndexCompatibility.class);
		addTestSuite(LockTest.class);
	}
}

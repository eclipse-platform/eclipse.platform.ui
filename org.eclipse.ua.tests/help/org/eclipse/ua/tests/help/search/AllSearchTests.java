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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Tests help functionality (automated).
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	ExtraDirTest.class,
	BasicTest.class,
	WildcardTest.class,
	LocaleTest.class,
	AnalyzerTest.class,
	SearchCheatsheet.class,
	SearchIntro.class,
	EncodedCharacterSearch.class,
	MetaKeywords.class,
	SearchParticipantTest.class,
	SearchParticipantXMLTest.class,
	SearchRanking.class,
	WorkingSetManagerTest.class,
	InfocenterWorkingSetManagerTest.class,
	PrebuiltIndexCompatibility.class,
	LockTest.class
})
public class AllSearchTests {
}

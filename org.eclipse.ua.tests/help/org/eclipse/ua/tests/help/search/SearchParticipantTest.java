/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.search;


import org.eclipse.help.internal.search.SearchHit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class SearchParticipantTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(SearchParticipantTest.class);
	}

	public void testSearchFirstWordInFirstDoc() {
		SearchTestUtils.searchAllLocales("jkijkijkk", new String[] { "/org.eclipse.ua.tests/participant1.xml" });
	}

	public void testSearchLastWordInFirstDoc() {
		SearchTestUtils.searchAllLocales("frgeded", new String[] { "/org.eclipse.ua.tests/participant1.xml" });
	}

	public void testSearchUsingAndInFirstDoc() {
		SearchTestUtils.searchAllLocales("jkijkijkk AND frgeded", new String[] { "/org.eclipse.ua.tests/participant1.xml" });
	}	

	public void testSearchUsingAndInSeparateDocs() {
		SearchTestUtils.searchAllLocales("jduehdye and olhoykk", new String[0]);
	}	

	public void testSearchExactMatch() {
		SearchTestUtils.searchAllLocales("\"jkijkijkk frgeded\"", new String[] { "/org.eclipse.ua.tests/participant1.xml" });
	}
	
	public void testSearchExactMatchNotFound() {
		SearchTestUtils.searchAllLocales("\"frgeded jkijkijkk\"", new String[0]);
	}	
	
	public void testSearchWordInSecondDoc() {
		SearchTestUtils.searchAllLocales("olhoykk", new String[] { "/org.eclipse.ua.tests/participant2.xml" });
	}

	public void testReturnedTitle() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("jkijkijkk", "en");
		assertEquals(hits.length,1);
		assertEquals("Title1", hits[0].getLabel());
	}
	
	public void testReturnedSummary() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("jkijkijkk", "en");
		assertEquals(hits.length,1);
		assertEquals("Summary1", hits[0].getSummary());
	}
	
}

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


public class SearchParticipantXMLTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(SearchParticipantXMLTest.class);
	}

	public void testSearchFirstWordInFirstDoc() {
		SearchTestUtils.searchAllLocales("jfplepdl", new String[] { MockSearchParticipantXML.DOC_1 });
	}

	public void testSearchLastWordInFirstDoc() {
		SearchTestUtils.searchAllLocales("memdjkemd", new String[] { MockSearchParticipantXML.DOC_1 });
	}

	public void testSearchWordFromOuterElement() {
		SearchTestUtils.searchAllLocales("odoeofoedo", new String[] { MockSearchParticipantXML.DOC_2 });
	}
	
	public void testSearchWordFromNestedElement() {
		SearchTestUtils.searchAllLocales("odkeofkeks", new String[] { MockSearchParticipantXML.DOC_2 });
	}

	public void testReturnedTitle() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("jfplepdl", "en");
		assertEquals(hits.length,1);
		assertEquals("Participant XML 1", hits[0].getLabel());
	}
	
	public void testReturnedSummary() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("jfplepdl", "en");
		assertEquals(hits.length,1);
		assertEquals("Summary for file Participant XML1", hits[0].getSummary());
	}

	public void testReturnedTitleNestedCase() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("odoeofoedo", "en");
		assertEquals(hits.length,1);
		assertEquals("Participant XML 2 - tests nesting", hits[0].getLabel());
	}
	
	public void testReturnedSummaryNestedCase() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("odoeofoedo", "en");
		assertEquals(hits.length,1);
		assertEquals("Summary for file Participant XML2", hits[0].getSummary());
	}
	
}

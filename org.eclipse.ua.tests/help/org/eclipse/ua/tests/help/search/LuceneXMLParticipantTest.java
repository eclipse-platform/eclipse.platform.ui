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


public class LuceneXMLParticipantTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(LuceneXMLParticipantTest.class);
	}

	public void testSearchFirstWordInFirstDoc() {
		SearchTestUtils.searchAllLocales("hujnjujnh", new String[] { MockLuceneXMLParticipant.DOC_1 });
	}

	public void testSearchLastWordInFirstDoc() {
		SearchTestUtils.searchAllLocales("dusudusud", new String[] { MockLuceneXMLParticipant.DOC_1 });
	}

	public void testSearchWordFromOuterElement() {
		SearchTestUtils.searchAllLocales("kfkkfjeeej", new String[] { MockLuceneXMLParticipant.DOC_2 });
	}
	
	public void testSearchWordFromNestedElement() {
		SearchTestUtils.searchAllLocales("syueuduehj", new String[] { MockLuceneXMLParticipant.DOC_2 });
	}

	public void testReturnedTitle() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("hujnjujnh", "en");
		assertEquals(hits.length,1);
		assertEquals("Lucene XML 1", hits[0].getLabel());
	}
	
	public void testReturnedSummary() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("hujnjujnh", "en");
		assertEquals(hits.length,1);
		assertEquals("Summary for file Lucene XML1", hits[0].getSummary());
	}

	public void testReturnedTitleNestedCase() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("kfkkfjeeej", "en");
		assertEquals(hits.length,1);
		assertEquals("Lucene XML 2 - tests nesting", hits[0].getLabel());
	}
	
	public void testReturnedSummaryNestedCase() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("kfkkfjeeej", "en");
		assertEquals(hits.length,1);
		assertEquals("Summary for file Lucene XML2", hits[0].getSummary());
	}
	
}

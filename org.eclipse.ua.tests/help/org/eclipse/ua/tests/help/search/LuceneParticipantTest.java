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


public class LuceneParticipantTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(LuceneParticipantTest.class);
	}

	public void testSearchFirstWordInFirstDoc() {
		SearchTestUtils.searchAllLocales("sehdtegd", new String[] { "/org.eclipse.help.base/lucene1.xml" });
	}

	public void testSearchLastWordInFirstDoc() {
		SearchTestUtils.searchAllLocales("jduehdye", new String[] { "/org.eclipse.help.base/lucene1.xml" });
	}

	public void testSearchUsingAndInFirstDoc() {
		SearchTestUtils.searchAllLocales("jduehdye AND sehdtegd", new String[] { "/org.eclipse.help.base/lucene1.xml" });
	}	

	public void testSearchUsingAndInSeparateDocs() {
		SearchTestUtils.searchAllLocales("jduehdye and nhduehrf", new String[0]);
	}	

	public void testSearchExactMatch() {
		SearchTestUtils.searchAllLocales("\"sehdtegd jduehdye\"", new String[] { "/org.eclipse.help.base/lucene1.xml" });
	}
	
	public void testSearchExactMatchNotFound() {
		SearchTestUtils.searchAllLocales("\"jduehdye sehdtegd\"", new String[0]);
	}	
	
	public void testSearchWordInSecondDoc() {
		SearchTestUtils.searchAllLocales("nhduehrf", new String[] { "/org.eclipse.help.base/lucene2.xml" });
	}
	
	public void testReturnedTitle() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("jduehdye", "en");
		assertEquals(hits.length,1);
		assertEquals("Title1", hits[0].getLabel());
	}
	
	public void testReturnedSummary() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("jduehdye", "en");
		assertEquals(hits.length,1);
		assertEquals("Summary1", hits[0].getSummary());
	}
	
}

/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.search;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.search.ISearchQuery;
import org.eclipse.help.internal.search.SearchHit;
import org.eclipse.help.internal.search.SearchQuery;
import org.eclipse.help.internal.search.SearchResults;

public class SearchRanking extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(SearchRanking.class);
	}
	
	public SearchHit[] findHits(String searchWord) {
		ISearchQuery query = new SearchQuery(searchWord, false, new ArrayList<String>(), Platform.getNL());
		SearchResults collector = new SearchResults(null, 10, Platform.getNL());
		BaseHelpSystem.getSearchManager().search(query, collector, new NullProgressMonitor());
		return collector.getSearchHits();
	}

	/**
	 * Verify that a match in a title has more weight than a match in the body
	 */
	public void testTitleBoost1() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("mjuhgt", "en");
		Arrays.sort(hits);
		assertEquals(2, hits.length);
		assertEquals("/org.eclipse.ua.tests/data/help/search/extraDir/ranking/ranktest1b.htm", getPath(hits[0]));
		assertEquals("/org.eclipse.ua.tests/data/help/search/extraDir/ranking/ranktest1a.htm", getPath(hits[1]));
	}
	
	public void testTitleBoost2() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("odrgtb", "en");
		Arrays.sort(hits);
		assertEquals(2, hits.length);
		assertEquals("/org.eclipse.ua.tests/data/help/search/extraDir/ranking/ranktest1a.htm", getPath(hits[0]));
		assertEquals("/org.eclipse.ua.tests/data/help/search/extraDir/ranking/ranktest1b.htm", getPath(hits[1]));
	}
	
	/**
	 * Verify that consecutive words raise the weight
	 */
	public void testConsecutiveWords1() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("iduhnf xaqsdab", "en");
		Arrays.sort(hits);
		assertEquals(2, hits.length);
		assertEquals("/org.eclipse.ua.tests/data/help/search/extraDir/ranking/ranktest2b.htm",
				getPath(hits[0]));
		assertEquals("/org.eclipse.ua.tests/data/help/search/extraDir/ranking/ranktest2a.htm", 
				getPath(hits[1]));
	}
	
	public void testConsecutiveWords2() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("xaqsdab iduhnf", "en");
		Arrays.sort(hits);
		assertEquals(2, hits.length);
		assertEquals("/org.eclipse.ua.tests/data/help/search/extraDir/ranking/ranktest2a.htm", 
				getPath(hits[0]));
		assertEquals("/org.eclipse.ua.tests/data/help/search/extraDir/ranking/ranktest2b.htm", 
				getPath(hits[1]));
	}
	
	
	private String getPath(SearchHit hit) {
		String href = hit.getHref();
		int query = href.indexOf('?');
		return query < 0 ? href : href.substring(0, query);
	}
	
}

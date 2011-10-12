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

public class SearchIntro extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(SearchIntro.class);
	}
	
	public SearchHit[] findHits(String searchWord) {
		ISearchQuery query = new SearchQuery(searchWord, false, new ArrayList<String>(), Platform.getNL());
		SearchResults collector = new SearchResults(null, 10, Platform.getNL());
		BaseHelpSystem.getSearchManager().search(query, collector, new NullProgressMonitor());
		return collector.getSearchHits();
	}

	public void testSearchIntroGroupLabel() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("ifirifjrnfj", "en");
		assertEquals(1, hits.length);
	}
	
	public void testSearchIntroGroupText() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("nenfhhdhhed", "en");
		assertEquals(1, hits.length);
	}
	
	public void testSearchIntroLinkLabel() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("hydefefed", "en");
		assertEquals(1, hits.length);
	}
	
	public void testSearchIntroLinkText() {
		SearchHit[] hits = SearchTestUtils.getSearchHits("hfuejfujduj", "en");
		assertEquals(1, hits.length);
	}
	
}

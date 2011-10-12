/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
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
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.search.ISearchQuery;
import org.eclipse.help.internal.search.SearchHit;
import org.eclipse.help.internal.search.SearchQuery;
import org.eclipse.help.internal.search.SearchResults;


public class MetaKeywords extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(MetaKeywords.class);
	}

	public void testKeywordInHtml() {
		SearchTestUtils.searchAllLocales("ydhaedrsc", new String[] { "/org.eclipse.ua.tests/data/help/search/testMeta.htm" });
	}
	
	public void testKeywordInXhtml() {
		SearchTestUtils.searchAllLocales("olfrgkjrifjd", new String[] { "/org.eclipse.ua.tests/data/help/search/testMeta.xhtml" });
	}

	public void testDescriptionInHtml() {
		SearchHit[] hits = getResultDescriptions("ydhaedrsc", "en");
		assertEquals(hits.length, 1);
		assertEquals("HTML Meta description", hits[0].getDescription());
	}
	
	public void testDescriptionInXhtml() {
		SearchHit[] hits = getResultDescriptions("olfrgkjrifjd", "en");
		assertEquals(hits.length, 1);
		assertEquals("XHTML Meta description", hits[0].getDescription());
	}
	
	public void testSearchDescriptionInHtml() {
		SearchTestUtils.searchAllLocales("basbanba", new String[] { "/org.eclipse.ua.tests/data/help/search/extraDir/testMeta2.htm" });
	}
	
	public void testSearchDescriptionInXhtml() {
		SearchTestUtils.searchAllLocales("mfjrudjesm", new String[] { "/org.eclipse.ua.tests/data/help/search/extraDir/testMeta2.xhtml" });
	}
	
	private SearchHit[] getResultDescriptions(String searchWord, String nl) {	
		ISearchQuery query = new SearchQuery(searchWord, false, new ArrayList<String>(), nl);
		SearchResults collector = new SearchResults(null, 500, nl);
		BaseHelpSystem.getSearchManager().search(query, collector, new NullProgressMonitor());
		return collector.getSearchHits();
	}
		
	
}

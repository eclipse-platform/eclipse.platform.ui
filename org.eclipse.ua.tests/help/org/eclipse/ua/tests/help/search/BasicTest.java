/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;
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

public class BasicTest extends TestCase {
	
	/*
	 * The test data for testXHTMLSearch(). The first string in each array
	 * is the word to search for. The rest are the expected hits' hrefs.
	 */
	private static final String[][] EXPECTED_RESULTS = {
		// sanity test
		{ "jehcyqpfjs", "/org.eclipse.ua.tests/data/help/search/test1.xhtml" },
		
		// appears in both filtered and unfiltered parts of test1.xhtml
		{ "vkrhjewiwh", "/org.eclipse.ua.tests/data/help/search/test1.xhtml" },
		
		// try OR'ing with a word that does't exist.. should find same result
		{ "vkrhjewiwh OR this_string_shouldnt_exist_in_any_doc", "/org.eclipse.ua.tests/data/help/search/test1.xhtml" },
		
		// try OR'ing with a string from another doc.. should find both
		{ "vkrhjewiwh OR rugnwjfyqj", "/org.eclipse.ua.tests/data/help/search/test1.xhtml", "/org.eclipse.ua.tests/data/help/search/test2.xhtml" },
		
		// these two words only appear next to each other in a paragraph that's filtered out - search should still find it
		{ "\"vkrhjewiwh riehguanil\"", "/org.eclipse.ua.tests/data/help/search/test1.xhtml" },
		
		// first one should be found, but second one only exists in a paragraph that's filtered out - search should still find
		{ "vkrhjewiwh AND riehguanil", "/org.eclipse.ua.tests/data/help/search/test1.xhtml" },
		
		// only exists in paragraph that's filtered out - search should find
		{ "gsdduvfqnh", "/org.eclipse.ua.tests/data/help/search/test1.xhtml" },
		
		// word is in test3.xhtml and also included by test4.xhtml, contributed
		// into test5.xhtml as an extension, and replaces a paragraph in test6.xhtml.
		{ "fuejnghqjs", "/org.eclipse.ua.tests/data/help/search/test3.xhtml", "/org.eclipse.ua.tests/data/help/search/test4.xhtml", "/org.eclipse.ua.tests/data/help/search/test5.xhtml", "/org.eclipse.ua.tests/data/help/search/test6.xhtml" },
		
		// only exists in paragraph in test6.xhtml that's replaced by another one
		{ "bheufnjefa", /* no hits */ },

		// sanity test to make sure it finds things in XHTML content in .html file
		{ "kejehrgaqm", "/org.eclipse.ua.tests/data/help/search/test7.html" },

		// same as above, but in a section that should never be filtered
		{ "opqmenhfjs", "/org.eclipse.ua.tests/data/help/search/test7.html" },

		// only exists in a paragraph in test7.html that should be filtered out
		// make sure this works for XHTML content inside .html file
		{ "hugftnhdtg", "/org.eclipse.ua.tests/data/help/search/test7.html" },
		
		// this doc is listed in TOC several times, using slightly different paths
		{ "rqfedajhtg", "/org.eclipse.ua.tests/data/help/search/test9.htm" },
	};
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(BasicTest.class);
	}

	public void testSearch() throws Exception {
		for (int i=0;i<EXPECTED_RESULTS.length;++i) {
			String searchWord = EXPECTED_RESULTS[i][0];
			final Set hrefsToFind = new HashSet();
			final Set unexpectedHrefs = new HashSet();
			
			String[] hrefs = new String[EXPECTED_RESULTS[i].length - 1];
			System.arraycopy(EXPECTED_RESULTS[i], 1, hrefs, 0, hrefs.length);
			hrefsToFind.addAll(Arrays.asList(hrefs));
			
			ISearchQuery query = new SearchQuery(searchWord, false, new ArrayList(), Platform.getNL());
			SearchResults collector = new SearchResults(null, 500, Platform.getNL());
			BaseHelpSystem.getSearchManager().search(query, collector, new NullProgressMonitor());
			SearchHit[] hits = collector.getSearchHits();
			for (int j=0;j<hits.length;++j) {
				String href = hits[j].getHref();
				// ignore query params
				int index = href.indexOf('?');
				if (index != -1) {
					href = href.substring(0, index);
				}
				if (hrefsToFind.contains(href)) {
					hrefsToFind.remove(href);
				}
				else {
					unexpectedHrefs.add(href);
				}
			}
			
			if (!hrefsToFind.isEmpty() || !unexpectedHrefs.isEmpty()) {
				StringBuffer buf = new StringBuffer();
				buf.append("While searching for: " + searchWord + ",\n");
				if (!hrefsToFind.isEmpty()) {
					buf.append("Some of the expected results were not found:\n");
					Iterator iter = hrefsToFind.iterator();
					while (iter.hasNext()) {
						String missedHref = (String)iter.next();
						buf.append(missedHref + "\n");
					}
				}
				if (!unexpectedHrefs.isEmpty()) {
					if (!hrefsToFind.isEmpty()) {
						buf.append("\nAlso,\n");
					}
					buf.append("Found some unexpected search results:\n");
					Iterator iter = unexpectedHrefs.iterator();
					while (iter.hasNext()) {
						String unexpectedHref = (String)iter.next();
						buf.append(unexpectedHref + "\n");
					}
				}
				Assert.fail(buf.toString());
			}
		}
	}
}

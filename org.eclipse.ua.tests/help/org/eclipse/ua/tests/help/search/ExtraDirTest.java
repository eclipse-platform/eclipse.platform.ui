/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

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
import org.eclipse.help.internal.workingset.AdaptableHelpResource;
import org.eclipse.help.internal.workingset.WorkingSet;
import org.eclipse.help.internal.workingset.WorkingSetManager;

public class ExtraDirTest extends TestCase {
	
	/*
	 * The test data for testExtraDirSearch(). The first item is the search
	 * phrase, then a comma-separated list of tocs for the search scope (or
	 * null for default scope), then comma-separated list of expected search
	 * results.
	 */
	private static final String[][] EXPECTED_RESULTS = {
		// try without search scope
		{ "iusazemhdv", null, "/org.eclipse.ua.tests/data/help/search/test8.htm" },

		// try with containing toc's search scope
		{ "iusazemhdv", "/org.eclipse.ua.tests/data/help/toc/root.xml", "/org.eclipse.ua.tests/data/help/search/test8.htm" },

		// try with another toc's search scope; shouldn't find it
		{ "iusazemhdv", "/org.eclipse.platform.doc.user/toc.xml", null },

		// extradir doc, no search scope
		{ "xzopsujjae", null, "/org.eclipse.ua.tests/data/help/search/extraDir/extraDoc1.htm" },

		// extradir doc, with correct search scope
		{ "xzopsujjae", "/org.eclipse.ua.tests/data/help/toc/root.xml", "/org.eclipse.ua.tests/data/help/search/extraDir/extraDoc1.htm" },

		// extradir doc, with incorrect search scope
		{ "xzopsujjae", "/org.eclipse.platform.doc.user/toc.xml", null },

		// extradir doc (in subdir), no search scope
		{ "mrendiqwja", null, "/org.eclipse.ua.tests/data/help/search/extraDir/subDir/extraDoc2.htm" },

		// extradir doc (in subdir), with correct search scope
		{ "mrendiqwja", "/org.eclipse.ua.tests/data/help/toc/root.xml", "/org.eclipse.ua.tests/data/help/search/extraDir/subDir/extraDoc2.htm" },

		// extradir doc (in subdir), with incorrect search scope
		{ "mrendiqwja", "/org.eclipse.platform.doc.user/toc.xml", null },

		// extradir doc link_to'ed from another toc (toc3.xml), with incorrect scope
		{ "kleoiujfpn", "/org.eclipse.platform.doc.user/toc.xml", null },

		// extradir doc linked into toc2.xml from toc4.xml, no scope
		{ "fuqnejwmfh", null, "/org.eclipse.ua.tests/data/help/search/extraDir3/extraDoc.htm" },

		// extradir doc linked into toc2.xml from toc4.xml, correct scope
		{ "fuqnejwmfh", "/org.eclipse.ua.tests/data/help/toc/root.xml", "/org.eclipse.ua.tests/data/help/search/extraDir3/extraDoc.htm" },

		// extradir doc linked into toc2.xml from toc4.xml, incorrect scope
		{ "fuqnejwmfh", "/org.eclipse.platform.doc.user/toc.xml", null },
	};
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(ExtraDirTest.class);
	}

	public void testExtraDirSearch() throws Exception {
		for (int i=0;i<EXPECTED_RESULTS.length;++i) {
			// search word
			String searchWord = EXPECTED_RESULTS[i][0];
			
			// search scope
			WorkingSet[] workingSets = null;
			if (EXPECTED_RESULTS[i][1] != null) {
				WorkingSetManager wsm = BaseHelpSystem.getWorkingSetManager();
				List tocs = new ArrayList();
				StringTokenizer tok = new StringTokenizer(EXPECTED_RESULTS[i][1], ", \t\n");
				while (tok.hasMoreTokens()) {
					tocs.add(wsm.getAdaptableToc(tok.nextToken()));
				}
				workingSets = new WorkingSet[] { wsm.createWorkingSet("testWorkingSet", (AdaptableHelpResource[])tocs.toArray(new AdaptableHelpResource[tocs.size()])) };
			}
			
			// expected hits
			final Set hrefsToFind = new HashSet();
			if (EXPECTED_RESULTS[i][2] != null) {
				StringTokenizer tok = new StringTokenizer(EXPECTED_RESULTS[i][2], ", \t\n");
				while (tok.hasMoreTokens()) {
					hrefsToFind.add(tok.nextToken());
				}
			}

			// run test
			final Set unexpectedHrefs = new HashSet();
			ISearchQuery query = new SearchQuery(searchWord, false, new ArrayList(), Platform.getNL());
			SearchResults collector = new SearchResults(workingSets, 500, Platform.getNL());
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

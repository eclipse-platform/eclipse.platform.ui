/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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

public class SearchCheatsheet extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(SearchCheatsheet.class);
	}
	
	public SearchHit[] findHits(String searchWord) {
		ISearchQuery query = new SearchQuery(searchWord, false, new ArrayList<String>(), Platform.getNL());
		SearchResults collector = new SearchResults(null, 10, Platform.getNL());
		BaseHelpSystem.getSearchManager().search(query, collector, new NullProgressMonitor());
		return collector.getSearchHits();
	}

	public void testCheatSheetTitleSearch() {
		SearchHit[] hits = findHits("CSTitle_AhB4U8");
		checkForCheatSheetMatch(hits);
	}

	public void testCheatSheetIntroSearch() {
		SearchHit[] hits = findHits("CSIntro_AhB4U8");
		checkForCheatSheetMatch(hits);
	}
	
	public void testCheatSheetItemSearch() {
		SearchHit[] hits = findHits("CSItem_AhB4U8");
		checkForCheatSheetMatch(hits);
		hits = findHits("CSItemDesc_AhB4U8");
		checkForCheatSheetMatch(hits);
		hits = findHits("CSItemCompletion_AhB4U8");
		checkForCheatSheetMatch(hits);
	}
	
	public void testCheatSheetSubitemSearch() {
		SearchHit[] hits = findHits("SubItem1_AhB4U8");
		checkForCheatSheetMatch(hits);
		hits = findHits("SubItem2_AhB4U8");
		checkForCheatSheetMatch(hits);
		hits = findHits("ConditionalSubItem_AhB4U8");
		checkForCheatSheetMatch(hits);	
	}

	public void testCompositeTitleSearch() {
		SearchHit[] hits = findHits("CompositeName_AhB4U8");
		checkForCompositeMatch(hits);
	}

	public void testCompositeTaskSearch() {
		SearchHit[] hits = findHits("TaskName_AhB4U8");
		checkForCompositeMatch(hits);
		hits = findHits("TaskIntro_AhB4U8");
		checkForCompositeMatch(hits);
		hits = findHits("TaskCompletion_AhB4U8");
		checkForCompositeMatch(hits);
		// Matches in task ids should not be hits
		hits = findHits("TaskId_AhB4U8");
		assertEquals(0, hits.length);
	}
	
	public void testCompositeTaskGroupSearch() {
		SearchHit[] hits = findHits("TaskGroup_AhB4U8");
		checkForCompositeMatch(hits);
		hits = findHits("TaskGroupIntro_AhB4U8");
		checkForCompositeMatch(hits);
		hits = findHits("TaskGroupCompletion_AhB4U8");
		checkForCompositeMatch(hits);
	}

	/*
	 * Chech that there was one match, the 
	 */
	private void checkForCheatSheetMatch(SearchHit[] hits) {
		assertEquals(1, hits.length);
		assertEquals("/org.eclipse.ua.tests/data/cheatsheet/search/CSSearchTest.xml", 
				ignoreQuery(hits[0].getHref()));
		assertTrue(hits[0].getDescription().startsWith("CSIntro_AhB4U8 This cheat sheet is used to test search."));
		assertEquals("org.eclipse.ui.cheatsheets/org.eclipse.ua.tests.cheatsheet.searchTest", hits[0].getId());
		}
	
	private void checkForCompositeMatch(SearchHit[] hits) {
		assertEquals(1, hits.length);
		assertEquals("/org.eclipse.ua.tests/data/cheatsheet/search/CompositeSearchTest.xml", 
				ignoreQuery(hits[0].getHref()));
		assertTrue(hits[0].getDescription().startsWith("Intro text TaskGroupIntro_AhB4U8"));
		assertEquals("org.eclipse.ui.cheatsheets/org.eclipse.ua.tests.composite.searchTest", 
				hits[0].getId());
	}

	private String ignoreQuery(String href) {
		int index = href.indexOf('?');
		if (index != -1) {
			return href.substring(0, index);
		}
		return href;
	}
	
}

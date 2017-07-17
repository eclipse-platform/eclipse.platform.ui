/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.core.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchEvent;

public class TestSearchResult {

	@Test
	public void testAddMatch() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 0, 0);
		result.addMatch(match1);
		assertEquals(result.getMatchCount(), 1);
		Match match2= new Match(object, 0, 0);
		result.addMatch(match2);
		assertEquals(result.getMatchCount(), 2);
		result.addMatch(match1);
		assertEquals(result.getMatchCount(), 2);
	}

	@Test
	public void testAddMatchDifferentStart() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 2, 0);
		result.addMatch(match1);
		assertEquals(result.getMatchCount(), 1);
		Match match2= new Match(object, 1, 1);
		result.addMatch(match2);
		Match match3= new Match(object, 0, 2);
		result.addMatch(match3);
		Match[] matches= result.getMatches(object);
		assertTrue("matches[0]", matches[0] == match3);
		assertTrue("matches[1]", matches[1] == match2);
		assertTrue("matches[2]", matches[2] == match1);
	}

	@Test
	public void testAddMatchDifferentStartInOrder() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 0, 0);
		result.addMatch(match1);
		assertEquals(result.getMatchCount(), 1);
		Match match2= new Match(object, 1, 1);
		result.addMatch(match2);
		Match match3= new Match(object, 2, 2);
		result.addMatch(match3);
		Match[] matches= result.getMatches(object);
		assertTrue("matches[0]", matches[0] == match1);
		assertTrue("matches[1]", matches[1] == match2);
		assertTrue("matches[2]", matches[2] == match3);
	}

	@Test
	public void testAddMatchDifferentLength() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 1, 1);
		result.addMatch(match1);
		assertEquals(result.getMatchCount(), 1);
		Match match2= new Match(object, 1, 0);
		result.addMatch(match2);
		Match[] matches= result.getMatches(object);
		assertTrue("matches[0]", matches[0] == match2);
		assertTrue("matches[1]", matches[1] == match1);
	}

	@Test
	public void testAddMatchOrderPreserving() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 1, 0);
		result.addMatch(match1);
		assertEquals(result.getMatchCount(), 1);
		Match match2= new Match(object, 1, 0);
		result.addMatch(match2);
		Match[] matches= result.getMatches(object);
		assertTrue("matches[0]", matches[0] == match1);
		assertTrue("matches[1]", matches[1] == match2);
	}

	@Test
	public void testAddMatches() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 0, 0);
		Match match2= new Match(object, 0, 0);
		result.addMatches(new Match[] { match1, match2 });
		assertEquals(result.getMatchCount(), 2);
		result.addMatch(match1);
		assertEquals(result.getMatchCount(), 2);
	}

	@Test
	public void testRemoveMatch() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 0, 0);
		result.addMatch(match1);
		Match match2= new Match(object, 0, 0);
		result.addMatch(match2);
		assertEquals(result.getMatchCount(), 2);

		result.removeMatch(match1);
		assertEquals(result.getMatchCount(), 1);
		result.removeMatch(match1);
		assertEquals(result.getMatchCount(), 1);

	}

	@Test
	public void testRemoveMatches() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 0, 0);
		Match match2= new Match(object, 0, 0);
		result.addMatches(new Match[] { match1, match2 });
		assertEquals(result.getMatchCount(), 2);

		result.removeMatches(new Match[] { match1, match2 });
		assertEquals(result.getMatchCount(), 0);

	}

	@Test
	public void testMatchEvent() {
		final boolean [] wasAdded= { false };
		final boolean [] wasRemoved= { false };

		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		result.addListener(new ISearchResultListener() {
			@Override
			public void searchResultChanged(SearchResultEvent e) {
				if (e instanceof MatchEvent) {
					MatchEvent evt= (MatchEvent) e;
					if (evt.getKind() == MatchEvent.ADDED) {
						wasAdded[0]= true;
					} else {
						wasRemoved[0]= true;
					}
				}
			}
		});

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 0, 0);
		result.addMatch(match1);
		assertTrue(wasAdded[0]);
		wasAdded[0]= false;
		result.addMatch(match1);
		assertFalse(wasAdded[0]);

		Match match2= new Match(object, 0, 0);
		result.addMatch(match2);
		assertTrue(wasAdded[0]);
		wasAdded[0]= false;

		result.removeMatch(match2);
		assertTrue(wasRemoved[0]);
		wasRemoved[0]= false;

		result.removeMatch(match2);
		assertFalse(wasRemoved[0]);
	}

	@Test
	public void testBatchedMatchEvent() {
		final boolean [] wasAdded= { false };
		final boolean [] wasRemoved= { false };

		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		result.addListener(new ISearchResultListener() {
			@Override
			public void searchResultChanged(SearchResultEvent e) {
				if (e instanceof MatchEvent) {
					MatchEvent evt= (MatchEvent) e;
					if (evt.getKind() == MatchEvent.ADDED) {
						wasAdded[0]= true;
					} else {
						wasRemoved[0]= true;
					}
				}
			}
		});

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 0, 0);
		result.addMatches(new Match[] { match1 });
		assertTrue(wasAdded[0]);
		wasAdded[0]= false;
		result.addMatches(new Match[] { match1 });
		assertFalse(wasAdded[0]);

		Match match2= new Match(object, 0, 0);
		result.addMatches(new Match[] { match2 });
		assertTrue(wasAdded[0]);
		wasAdded[0]= false;

		result.removeMatches(new Match[] { match2 });
		assertTrue(wasRemoved[0]);
		wasRemoved[0]= false;

		result.removeMatches(new Match[] { match2 });
		assertFalse(wasRemoved[0]);
	}
}

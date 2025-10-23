/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.core.tests;




import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.eclipse.search.ui.ISearchQuery;
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
		assertEquals(1, result.getMatchCount());
		Match match2= new Match(object, 0, 0);
		result.addMatch(match2);
		assertEquals(2, result.getMatchCount());
		result.addMatch(match1);
		assertEquals(2, result.getMatchCount());
	}

	@Test
	public void testAddMatchDifferentStart() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 2, 0);
		result.addMatch(match1);
		assertEquals(1, result.getMatchCount());
		Match match2= new Match(object, 1, 1);
		result.addMatch(match2);
		Match match3= new Match(object, 0, 2);
		result.addMatch(match3);
		Match[] matches= result.getMatches(object);
		assertSame(matches[0], match3, "matches[0]");
		assertSame(matches[1], match2, "matches[1]");
		assertSame(matches[2], match1, "matches[2]");
	}

	@Test
	public void testAddMatchDifferentStartInOrder() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 0, 0);
		result.addMatch(match1);
		assertEquals(1, result.getMatchCount());
		Match match2= new Match(object, 1, 1);
		result.addMatch(match2);
		Match match3= new Match(object, 2, 2);
		result.addMatch(match3);
		Match[] matches= result.getMatches(object);
		assertSame(matches[0], match1, "matches[0]");
		assertSame(matches[1], match2, "matches[1]");
		assertSame(matches[2], match3, "matches[2]");
	}

	@Test
	public void testAddMatchDifferentLength() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 1, 1);
		result.addMatch(match1);
		assertEquals(1, result.getMatchCount());
		Match match2= new Match(object, 1, 0);
		result.addMatch(match2);
		Match[] matches= result.getMatches(object);
		assertSame(matches[0], match2, "matches[0]");
		assertSame(matches[1], match1, "matches[1]");
	}

	@Test
	public void testAddMatches() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 0, 0);
		Match match2= new Match(object, 0, 0);
		result.addMatches(new Match[] { match1, match2 });
		assertEquals(2, result.getMatchCount());
		result.addMatch(match1);
		assertEquals(2, result.getMatchCount());
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
		assertEquals(2, result.getMatchCount());

		result.removeMatch(match1);
		assertEquals(1, result.getMatchCount());
		result.removeMatch(match1);
		assertEquals(1, result.getMatchCount());

	}

	@Test
	public void testRemoveMatches() {
		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		String object= "object"; //$NON-NLS-1$

		Match match1= new Match(object, 0, 0);
		Match match2= new Match(object, 0, 0);
		result.addMatches(new Match[] { match1, match2 });
		assertEquals(2, result.getMatchCount());

		result.removeMatches(new Match[] { match1, match2 });
		assertEquals(0, result.getMatchCount());

	}

	@Test
	public void testMatchEvent() {
		final boolean [] wasAdded= { false };
		final boolean [] wasRemoved= { false };

		ISearchQuery query= new NullQuery();
		AbstractTextSearchResult result= (AbstractTextSearchResult) query.getSearchResult();

		result.addListener(e -> {
			if (e instanceof MatchEvent) {
				MatchEvent evt= (MatchEvent) e;
				if (evt.getKind() == MatchEvent.ADDED) {
					wasAdded[0]= true;
				} else {
					wasRemoved[0]= true;
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

		result.addListener(e -> {
			if (e instanceof MatchEvent) {
				MatchEvent evt= (MatchEvent) e;
				if (evt.getKind() == MatchEvent.ADDED) {
					wasAdded[0]= true;
				} else {
					wasRemoved[0]= true;
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

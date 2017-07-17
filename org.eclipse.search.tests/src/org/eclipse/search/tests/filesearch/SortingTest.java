/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.Match;

public class SortingTest {
	FileSearchQuery fQuery1;

	@ClassRule
	public static JUnitSourceSetup fgJUnitSource= new JUnitSourceSetup();

	@Before
	public void setUp() throws Exception {
		String[] fileNamePatterns= { "*.java" };
		FileTextSearchScope scope= FileTextSearchScope.newWorkspaceScope(fileNamePatterns, false);

		fQuery1= new FileSearchQuery("Test", false, true, scope);
	}

	@Test
	public void testSorted() throws Exception {
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(null, fQuery1);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		int originalMatchCount= result.getMatchCount();
		List<Match> allMatches= new ArrayList<>(originalMatchCount);

		// first, collect all matches
		Object[] elements= result.getElements();
		for (int i= 0; i < elements.length; i++) {
			Match[] matches= result.getMatches(elements[i]);
			for (int j= 0; j < matches.length; j++) {
				allMatches.add(matches[j]);
			}
		}
		// now remove them and readd them in reverse order
		result.removeAll();
		assertTrue("removed all matches", result.getMatchCount() == 0);

		for (int i= allMatches.size()-1; i >= 0; i--) {
			result.addMatch(allMatches.get(i));
		}

		assertEquals("Test that all matches have been added again", result.getMatchCount(), originalMatchCount);

		// now check that they're ordered by position.
		for (int i= 0; i < elements.length; i++) {
			Match[] matches= result.getMatches(elements[i]);
			assertTrue("has matches", matches.length > 0);
			for (int j= 1; j < matches.length; j++) {
				assertTrue("order problem", isLessOrEqual(matches[j-1], matches[j]));
			}
		}
	}

	private boolean isLessOrEqual(Match match, Match match2) {
		int diff= match2.getOffset() - match.getOffset();
		if (diff > 0)
			return true;
		else if (diff < 0)
			return false;
		else {
			// equal offset, have to look at the length.
			diff= match2.getLength() - match.getLength();
			if (diff >= 0)
				return true;
			return false;
		}
	}

}

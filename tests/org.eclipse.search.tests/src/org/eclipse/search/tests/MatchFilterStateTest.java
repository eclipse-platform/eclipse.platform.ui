/*******************************************************************************
 * Copyright (c) 2026 Andrey Loskutov <loskutov@gmx.de> and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;

/**
 * Tests that the filter state of the matches of an
 * {@link AbstractTextSearchResult} is computed when a match is added.
 */
public class MatchFilterStateTest {

	/**
	 * If the filters don't change, the match must be evaluated only once.
	 */
	@Test
	public void testMatchIsEvaluatedOnce() {
		TestResult result= new TestResult();
		FilterAll filter= new FilterAll();
		result.setActiveMatchFilters(new MatchFilter[] { filter });

		Match match= new Match("element", 0, 1);
		result.addMatch(match);

		assertTrue(match.isFiltered());
		assertEquals(1, filter.evaluations);
	}

	private static final class FilterAll extends TestFilter {

		volatile int evaluations;

		@Override
		public boolean filters(Match match) {
			evaluations++;
			return true;
		}
	}

	private abstract static class TestFilter extends MatchFilter {

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public String getDescription() {
			return getName();
		}

		@Override
		public String getActionLabel() {
			return getName();
		}

		@Override
		public String getID() {
			return getClass().getName();
		}
	}

	private static final class TestResult extends AbstractTextSearchResult {

		@Override
		public String getLabel() {
			return "test";
		}

		@Override
		public String getTooltip() {
			return getLabel();
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public ISearchQuery getQuery() {
			return null;
		}

		@Override
		public IEditorMatchAdapter getEditorMatchAdapter() {
			return null;
		}

		@Override
		public IFileMatchAdapter getFileMatchAdapter() {
			return null;
		}
	}
}

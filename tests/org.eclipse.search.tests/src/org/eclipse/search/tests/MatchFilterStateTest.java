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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
 * {@link AbstractTextSearchResult} always reflects the active match filters,
 * even if the filters are changed while matches are added concurrently.
 */
public class MatchFilterStateTest {

	private static final long TIMEOUT_SECONDS= 30;

	/**
	 * The filter state of a match is evaluated before the match is added, and
	 * {@link AbstractTextSearchResult#setActiveMatchFilters(MatchFilter[])} only
	 * updates the matches that are already added. A match that is evaluated with
	 * the previous filters and added after the filters have been changed must not
	 * keep the outdated state.
	 */
	@Test
	public void testFilterChangeDuringMatchEvaluation() throws Exception {
		TestResult result= new TestResult();
		BlockingFilter previousFilter= new BlockingFilter();
		result.setActiveMatchFilters(new MatchFilter[] { previousFilter });

		Match match= new Match("element", 0, 1);
		AtomicReference<Throwable> failure= new AtomicReference<>();
		Thread searchThread= new Thread(() -> {
			try {
				result.addMatch(match);
			} catch (Throwable e) {
				failure.set(e);
			}
		}, "search thread");
		searchThread.start();
		try {
			assertTrue(previousFilter.evaluating.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
					"the match must be evaluated with the previous filter");

			// the match is not added yet: it can't be updated by the filter change
			result.setActiveMatchFilters(new MatchFilter[] { new FilterAll() });
		} finally {
			previousFilter.proceed.countDown();
			searchThread.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
		}

		assertFalse(searchThread.isAlive(), "the match must be added");
		assertNull(failure.get(), "the match must be added without errors");
		assertEquals(1, result.getMatchCount());
		assertTrue(match.isFiltered(), "the match must be evaluated with the active filter");
	}

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

	/**
	 * Returns <code>false</code> (doesn't filter) but blocks the first evaluation
	 * until it is allowed to proceed.
	 */
	private static final class BlockingFilter extends TestFilter {

		final CountDownLatch evaluating= new CountDownLatch(1);

		final CountDownLatch proceed= new CountDownLatch(1);

		@Override
		public boolean filters(Match match) {
			if (evaluating.getCount() > 0) {
				evaluating.countDown();
				try {
					proceed.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			return false;
		}
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

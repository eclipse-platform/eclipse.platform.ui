/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.search.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;

public class AbstractTextSearchResultMatchCountTest {

	private static final class TestResult extends AbstractTextSearchResult {
		@Override
		public String getLabel() {
			return "";
		}

		@Override
		public String getTooltip() {
			return "";
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

	/** Element whose next hashCode() call parks the calling thread until released. */
	private static final class BlockingElement {
		final CountDownLatch entered = new CountDownLatch(1);
		final CountDownLatch release = new CountDownLatch(1);
		volatile boolean armed = true;

		@Override
		public int hashCode() {
			if (armed) {
				armed = false;
				entered.countDown();
				try {
					release.await(10, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			return 42;
		}
	}

	@Test
	public void testMatchCountIsNotStaleWhenReadWhileMatchIsAdded() throws Exception {
		TestResult result = new TestResult();
		result.addMatch(new Match("element", 0, 5));
		result.addMatch(new Match("element", 10, 5));

		BlockingElement blocking = new BlockingElement();
		Thread adder = new Thread(() -> result.addMatch(new Match(blocking, 0, 5)));
		adder.setDaemon(true);
		adder.start();
		try {
			assertTrue(blocking.entered.await(10, TimeUnit.SECONDS));
			// Count while the add is in flight, so the count gets cached before the match lands
			assertEquals(2, result.getMatchCount());
		} finally {
			blocking.release.countDown();
			adder.join(10_000);
		}
		assertFalse(adder.isAlive());
		assertEquals(3, result.getMatchCount());
	}
}

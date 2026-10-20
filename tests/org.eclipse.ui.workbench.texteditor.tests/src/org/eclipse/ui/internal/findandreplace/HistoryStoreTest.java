/*******************************************************************************
 * Copyright (c) 2025 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.eclipse.jface.dialogs.IDialogSettings;

public class HistoryStoreTest {

	/**
	 * Returns a minimal {@link IDialogSettings} stub that stores and retrieves
	 * {@code String[]} values in memory, keyed by section name. All other
	 * {@code IDialogSettings} methods are left as Mockito no-ops / default returns.
	 */
	private IDialogSettings createInMemoryDialogSettings() {
		Map<String, String[]> store = new HashMap<>();
		IDialogSettings settings = mock(IDialogSettings.class);
		when(settings.getArray(anyString())).thenAnswer(inv -> store.get(inv.getArgument(0)));
		doAnswer(inv -> {
			store.put(inv.getArgument(0), inv.getArgument(1));
			return null;
		}).when(settings).put(anyString(), any(String[].class));
		return settings;
	}

	@Test
	public void testConstructorThrowsOnNullSectionName() {
		assertThrows(IllegalStateException.class,
				() -> new HistoryStore(createInMemoryDialogSettings(), null, 5));
	}

	@Test
	public void testNewStoreIsEmpty() {
		HistoryStore store = new HistoryStore(createInMemoryDialogSettings(), "section", 5);
		assertTrue(store.isEmpty());
		assertEquals(0, store.size());
	}

	@Test
	public void testAddSingleItem() {
		HistoryStore store = new HistoryStore(createInMemoryDialogSettings(), "section", 5);

		store.add("item");

		assertFalse(store.isEmpty());
		assertEquals(1, store.size());
		assertEquals("item", store.get(0));
	}

	@Test
	public void testAddNullIsIgnored() {
		// Null items must not be stored; history stays empty.
		HistoryStore store = new HistoryStore(createInMemoryDialogSettings(), "section", 5);

		store.add(null);

		assertTrue(store.isEmpty());
	}

	@Test
	public void testAddEmptyStringIsIgnored() {
		// Empty strings must not be stored; an empty search term is not useful history.
		HistoryStore store = new HistoryStore(createInMemoryDialogSettings(), "section", 5);

		store.add("");

		assertTrue(store.isEmpty());
	}

	@Test
	public void testMostRecentlyAddedItemIsFirst() {
		// The most recently added item should appear at index 0 so that history
		// navigation reaches recent searches first.
		HistoryStore store = new HistoryStore(createInMemoryDialogSettings(), "section", 5);

		store.add("first");
		store.add("second");
		store.add("third");

		assertEquals("third", store.get(0));
		assertEquals("second", store.get(1));
		assertEquals("first", store.get(2));
		assertEquals(3, store.size());
	}

	@Test
	public void testAddingExistingItemMovesItToFront() {
		// Re-adding a term already in history should move it to index 0 without
		// creating a duplicate. This mirrors how every search toolbar works: the most
		// recently used term comes first.
		HistoryStore store = new HistoryStore(createInMemoryDialogSettings(), "section", 5);
		store.add("first");
		store.add("second");

		store.add("first");

		assertEquals(2, store.size());
		assertEquals("first", store.get(0));
		assertEquals("second", store.get(1));
	}

	@Test
	public void testAddingItemAlreadyAtFrontKeepsItThere() {
		HistoryStore store = new HistoryStore(createInMemoryDialogSettings(), "section", 5);
		store.add("item");

		store.add("item");

		assertEquals(1, store.size());
		assertEquals("item", store.get(0));
	}

	@Test
	public void testHistorySizeLimitDropsOldestEntries() {
		// Once the capacity is reached, the oldest (highest-index) entry must be
		// dropped to make room for the new one.
		HistoryStore store = new HistoryStore(createInMemoryDialogSettings(), "section", 3);
		store.add("first");
		store.add("second");
		store.add("third");

		store.add("fourth");

		assertEquals(3, store.size());
		assertEquals("fourth", store.get(0));
		assertEquals("third", store.get(1));
		assertEquals("second", store.get(2));
	}

	@Test
	public void testRemoveExistingItem() {
		HistoryStore store = new HistoryStore(createInMemoryDialogSettings(), "section", 5);
		store.add("first");
		store.add("second");

		store.remove("first");

		assertEquals(1, store.size());
		assertEquals("second", store.get(0));
	}

	@Test
	public void testRemoveItemAtFront() {
		HistoryStore store = new HistoryStore(createInMemoryDialogSettings(), "section", 5);
		store.add("first");
		store.add("second");

		store.remove("second");

		assertEquals(1, store.size());
		assertEquals("first", store.get(0));
	}

	@Test
	public void testRemoveNonExistentItemIsNoOp() {
		// Removing a term that was never stored must not alter the history.
		HistoryStore store = new HistoryStore(createInMemoryDialogSettings(), "section", 5);
		store.add("item");

		store.remove("nonexistent");

		assertEquals(1, store.size());
		assertEquals("item", store.get(0));
	}

	@Test
	public void testIndexOfReturnsCorrectPositions() {
		HistoryStore store = new HistoryStore(createInMemoryDialogSettings(), "section", 5);
		store.add("first");
		store.add("second");
		store.add("third");

		assertEquals(0, store.indexOf("third"));
		assertEquals(1, store.indexOf("second"));
		assertEquals(2, store.indexOf("first"));
	}

	@Test
	public void testIndexOfReturnsMinusOneForAbsentItem() {
		HistoryStore store = new HistoryStore(createInMemoryDialogSettings(), "section", 5);
		store.add("item");

		assertEquals(-1, store.indexOf("nonexistent"));
	}

	@Test
	public void testGetIterableReturnsItemsNewestFirst() {
		HistoryStore store = new HistoryStore(createInMemoryDialogSettings(), "section", 5);
		store.add("first");
		store.add("second");

		List<String> items = new ArrayList<>();
		for (String item : store.get()) {
			items.add(item);
		}

		assertEquals(List.of("second", "first"), items);
	}

	@Test
	public void testHistoryPersistedAcrossInstances() {
		// Two HistoryStore instances pointing to the same IDialogSettings section must
		// share the same data, modelling persistence across workbench sessions.
		IDialogSettings sharedSettings = createInMemoryDialogSettings();
		HistoryStore store1 = new HistoryStore(sharedSettings, "section", 5);
		store1.add("first");
		store1.add("second");

		HistoryStore store2 = new HistoryStore(sharedSettings, "section", 5);

		assertEquals(2, store2.size());
		assertEquals("second", store2.get(0));
		assertEquals("first", store2.get(1));
	}

	@Test
	public void testDistinctSectionsAreIndependent() {
		// Two stores sharing the same IDialogSettings but using different section names
		// must not interfere with each other (models separate find/replace histories).
		IDialogSettings sharedSettings = createInMemoryDialogSettings();
		HistoryStore findHistory = new HistoryStore(sharedSettings, "findhistory", 5);
		HistoryStore replaceHistory = new HistoryStore(sharedSettings, "replacehistory", 5);
		findHistory.add("findterm");

		assertTrue(replaceHistory.isEmpty());
	}

}

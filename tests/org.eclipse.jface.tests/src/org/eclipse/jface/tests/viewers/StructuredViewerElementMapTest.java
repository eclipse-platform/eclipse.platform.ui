/*******************************************************************************
 * Copyright (c) 2026 Vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the hash table a viewer keeps to map elements to their items, reached
 * through the comparer, which is the only way a client controls how elements
 * hash.
 */
public class StructuredViewerElementMapTest {

	/**
	 * The capacity a viewer's element map starts with. Slots are the hash modulo
	 * this, so hashes differing by a multiple of it collide until the table grows.
	 */
	private static final int INITIAL_CAPACITY = 13;

	private Shell shell;

	private TableViewer viewer;

	private static class Element {
		final String name;
		final int hash;

		Element(String name, int hash) {
			this.name = name;
			this.hash = hash;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * Takes the hash from the element, so a test can choose its slot, and compares
	 * by name, so equal but distinct elements are recognized.
	 */
	private static final IElementComparer COMPARER = new IElementComparer() {

		@Override
		public boolean equals(Object a, Object b) {
			if (a instanceof Element first && b instanceof Element second) {
				return first.name.equals(second.name);
			}
			return a == null ? b == null : a.equals(b);
		}

		@Override
		public int hashCode(Object element) {
			return element instanceof Element typed ? typed.hash : element.hashCode();
		}
	};

	@AfterEach
	public void tearDown() {
		if (shell != null) {
			shell.dispose();
			shell = null;
		}
		viewer = null;
	}

	private void createViewer(List<Element> input) {
		shell = new Shell();
		shell.setSize(500, 500);
		viewer = new TableViewer(shell);
		viewer.setComparer(COMPARER);
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider());
		viewer.setInput(input);
		shell.open();
	}

	private void assertMapsToItemFor(Element element) {
		Widget item = viewer.testFindItem(element);
		assertNotNull(item, "no item mapped for " + element);
		assertSame(element, item.getData(), "wrong item mapped for " + element);
	}

	/**
	 * Keys with identical hashes share a slot and have to be told apart by the
	 * comparer.
	 */
	@Test
	public void testFindsElementsWithIdenticalHashes() {
		List<Element> input = List.of(new Element("a", 7), new Element("b", 7), new Element("c", 7));
		createViewer(input);

		for (Element element : input) {
			assertMapsToItemFor(element);
		}
		assertNull(viewer.testFindItem(new Element("absent", 7)), "an absent element was found");
	}

	/**
	 * Keys whose hashes differ but share a slot must not be confused.
	 */
	@Test
	public void testFindsElementsWithCollidingSlots() {
		List<Element> input = List.of(new Element("a", 1), new Element("b", 1 + INITIAL_CAPACITY),
				new Element("c", 1 + 2 * INITIAL_CAPACITY));
		createViewer(input);

		for (Element element : input) {
			assertMapsToItemFor(element);
		}
		// Same slot, no matching hash, and no matching name.
		assertNull(viewer.testFindItem(new Element("absent", 1 + 3 * INITIAL_CAPACITY)),
				"an absent element sharing a slot was found");
	}

	/**
	 * Growing past the initial capacity redistributes every key, and all of them
	 * have to stay findable.
	 */
	@Test
	public void testFindsElementsAfterTableGrowth() {
		List<Element> input = new ArrayList<>();
		for (int i = 0; i < 200; i++) {
			// Spread over the slots and force several rounds of growth.
			input.add(new Element("e" + i, i * 31));
		}
		createViewer(input);

		for (Element element : input) {
			assertMapsToItemFor(element);
		}
	}

	/**
	 * After a refresh hands the viewer equal but distinct objects, the map has to
	 * find them and the items have to hold the new ones.
	 */
	@Test
	public void testFindsEqualButDistinctElementsAfterRefresh() {
		List<Element> input = new ArrayList<>(
				Arrays.asList(new Element("a", 3), new Element("b", 3), new Element("c", 9)));
		createViewer(input);

		List<Element> replacements = new ArrayList<>();
		for (Element element : input) {
			replacements.add(new Element(element.name, element.hash));
		}
		input.clear();
		input.addAll(replacements);
		viewer.refresh();

		for (Element replacement : replacements) {
			assertMapsToItemFor(replacement);
		}
		assertEquals(replacements.size(), viewer.getTable().getItemCount());
	}

	/**
	 * A removed element must not be reachable through the map any more.
	 */
	@Test
	public void testRemovedElementIsNoLongerFound() {
		List<Element> input = new ArrayList<>(
				Arrays.asList(new Element("a", 5), new Element("b", 5), new Element("c", 5)));
		createViewer(input);

		Element removed = input.remove(1);
		viewer.remove(removed);

		assertNull(viewer.testFindItem(removed), "the removed element is still mapped");
		for (Element element : input) {
			assertMapsToItemFor(element);
		}
	}

}

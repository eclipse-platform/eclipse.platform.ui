/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
 *     Matthew Hall - bug 226216
 *******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.junit.Test;

/**
 * @since 1.1
 */
public class Diffs_ListDiffTests {
	@Test
	public void testListDiffEntryToStringDoesNotThrowNPEForNullListDiffEntry() {
		ListDiffEntry<?> entry = new ListDiffEntry<Object>() {
			@Override
			public Object getElement() {
				return null;
			}

			@Override
			public int getPosition() {
				return 0;
			}

			@Override
			public boolean isAddition() {
				return false;
			}
		};

		entry.toString();
	}

	@Test
	public void testListDiffToStringDoesNotThrowNPEForNullListDiff() {
		ListDiff<?> diff = new ListDiff<Object>() {
			@Override
			public ListDiffEntry<Object>[] getDifferences() {
				return null;
			}
		};

		diff.toString();
	}

	@Test
	public void testListDiffToStringDoesNotThrowNPEForNullListDiffEntry() {
		ListDiff<?> diff = new ListDiff<Object>() {
			@SuppressWarnings("unchecked")
			@Override
			public ListDiffEntry<Object>[] getDifferences() {
				return new ListDiffEntry[1];
			}
		};

		diff.toString();
	}

	@Test
	public void testDiffScenario1() throws Exception {
		ListDiff<?> diff = diff(null, null);
		assertEquals(0, diff.getDifferences().length);
	}

	private ListDiff<?> diff(String[] oldArray, String[] newArray) {
		List<String> a = Arrays.asList((oldArray != null) ? oldArray : new String[] {});
		List<String> b = Arrays.asList((newArray != null) ? newArray : new String[] {});

		return Diffs.computeListDiff(a, b);
	}

	@Test
	public void testDiffScenario2() throws Exception {
		ListDiff<?> diff = diff(new String[] { "a" }, null);
		assertEquals(1, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], false, 0, "a");
	}

	@Test
	public void testDiffScenario3() throws Exception {
		ListDiff<?> diff = diff(null, new String[] { "a" });
		assertEquals(1, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], true, 0, "a");
	}

	@Test
	public void testDiffScenario4() throws Exception {
		ListDiff<?> diff = diff(new String[] { "a" }, new String[] { "a" });
		assertEquals(0, diff.getDifferences().length);
	}

	@Test
	public void testDiffScenario5() throws Exception {
		ListDiff<?> diff = diff(new String[] { "a" }, new String[] { "b" });
		assertEquals(2, diff.getDifferences().length);

		assertEntry(diff.getDifferences()[0], true, 0, "b");
		assertEntry(diff.getDifferences()[1], false, 1, "a");
	}

	@Test
	public void testDiffScenario6() throws Exception {
		ListDiff<?> diff = diff(new String[] { "a" }, new String[] { "a", "b" });

		assertEquals(1, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], true, 1, "b");
	}

	@Test
	public void testDiffScenario7() throws Exception {
		ListDiff<?> diff = diff(new String[] { "a" }, new String[] { "b", "a" });

		assertEquals(1, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], true, 0, "b");
	}

	@Test
	public void testDiffScenario8() throws Exception {
		ListDiff<?> diff = diff(new String[] { "a" }, new String[] { "b", "b" });

		assertEquals(3, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], true, 0, "b");
		assertEntry(diff.getDifferences()[1], true, 1, "b");
		assertEntry(diff.getDifferences()[2], false, 2, "a");
	}

	@Test
	public void testDiffScenario9() throws Exception {
		ListDiff<?> diff = diff(new String[] { "a" }, new String[] { "a", "b", "c" });

		assertEquals(2, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], true, 1, "b");
		assertEntry(diff.getDifferences()[1], true, 2, "c");
	}

	@Test
	public void testDiffScenario10() throws Exception {
		ListDiff<?> diff = diff(new String[] { "b" }, new String[] { "a", "b", "c" });

		assertEquals(2, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], true, 0, "a");
		assertEntry(diff.getDifferences()[1], true, 2, "c");
	}

	@Test
	public void testDiffScenario11() throws Exception {
		ListDiff<?> diff = diff(new String[] { "c" }, new String[] { "a", "b", "c" });

		assertEquals(2, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], true, 0, "a");
		assertEntry(diff.getDifferences()[1], true, 1, "b");
	}

	@Test
	public void testDiffScenario12() throws Exception {
		ListDiff<?> diff = diff(new String[] { "a", "b", "c" }, new String[] { "a", "b", "c" });

		assertEquals(0, diff.getDifferences().length);
	}

	@Test
	public void testDiffScenario13() throws Exception {
		ListDiff<?> diff = diff(new String[] { "a", "b", "c" }, new String[] { "b", "c" });

		assertEquals(1, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], false, 0, "a");
	}

	@Test
	public void testDiffScenarios14() throws Exception {
		ListDiff<?> diff = diff(new String[] { "a", "b", "c" }, new String[] { "a", "c" });

		assertEquals(1, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], false, 1, "b");
	}

	@Test
	public void testDiffScenarios15() throws Exception {
		ListDiff<?> diff = diff(new String[] { "a", "b", "c" }, new String[] { "a", "b" });

		assertEquals(1, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], false, 2, "c");
	}

	@Test
	public void testDiffScenarios16() throws Exception {
		ListDiff<?> diff = diff(new String[] { "a", "b", "c" }, new String[] { "c", "b", "a" });

		assertEquals(4, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], false, 2, "c");
		assertEntry(diff.getDifferences()[1], true, 0, "c");
		assertEntry(diff.getDifferences()[2], false, 2, "b");
		assertEntry(diff.getDifferences()[3], true, 1, "b");
	}

	@Test
	public void testDiffScenarios17() throws Exception {
		ListDiff<?> diff = diff(new String[] { "a", "b", "c" }, new String[] { "c", "b" });

		assertEquals(3, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], false, 0, "a");
		assertEntry(diff.getDifferences()[1], false, 1, "c");
		assertEntry(diff.getDifferences()[2], true, 0, "c");
	}

	private static void assertEntry(ListDiffEntry<?> entry, boolean addition, int position, String element) {
		assertEquals("addition", addition, entry.isAddition());
		assertEquals("position", position, entry.getPosition());
		assertEquals("element", element, entry.getElement());
	}

	@Test
	public void testComputeListDiff_SingleInsert() {
		checkComputedListDiff(List.of("a", "c"), List.of("a", "b", "c"));
	}

	@Test
	public void testComputeListDiff_SingleAppend() {
		checkComputedListDiff(List.of("a", "b"), List.of("a", "b", "c"));
	}

	@Test
	public void testComputeListDiff_SingleRemove() {
		checkComputedListDiff(List.of("a", "b", "c"), List.of("a", "b"));
		checkComputedListDiff(List.of("a", "b", "c"), List.of("a", "c"));
		checkComputedListDiff(List.of("a", "b", "c"), List.of("b", "c"));
	}

	@Test
	public void testComputeListDiff_MoveDown1() {
		checkComputedListDiff(List.of("a", "b"), List.of("b", "a"));
	}

	@Test
	public void testComputeListDiff_MoveDown2() {
		checkComputedListDiff(List.of("a", "b", "c"), List.of("b", "c", "a"));
	}

	@Test
	public void testComputeListDiff_MoveUp1() {
		checkComputedListDiff(List.of("a", "b"), List.of("b", "a"));
	}

	@Test
	public void testComputeListDiff_MoveUp2() {
		checkComputedListDiff(List.of("a", "b", "c"), List.of("c", "a", "b"));
	}

	private static void checkComputedListDiff(List<Object> oldList, List<Object> newList) {
		ListDiff<?> diff = Diffs.computeListDiff(oldList, newList);

		final List<Object> list = new ArrayList<>(oldList);
		diff.accept(new ListDiffVisitor<Object>() {
			@Override
			public void handleAdd(int index, Object element) {
				list.add(index, element);
			}

			@Override
			public void handleRemove(int index, Object element) {
				assertEquals(element, list.remove(index));
			}

			@Override
			public void handleReplace(int index, Object oldElement, Object newElement) {
				assertEquals(oldElement, list.set(index, newElement));
			}
		});

		assertEquals("Applying diff to old list should make it equal to new list", newList, list);
	}
}

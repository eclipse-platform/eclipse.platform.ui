/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 226216
 *******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;

/**
 * @since 1.1
 */
public class Diffs_ListDiffTests extends TestCase {
	public void testListDiffEntryToStringDoesNotThrowNPEForNullListDiffEntry() {
		ListDiffEntry entry = new ListDiffEntry() {
			public Object getElement() {
				return null;
			}

			public int getPosition() {
				return 0;
			}

			public boolean isAddition() {
				return false;
			}
		};

		try {
			entry.toString();
			assertTrue(true);
		} catch (NullPointerException e) {
			fail("NPE was thrown.");
		}
	}

	public void testListDiffToStringDoesNotThrowNPEForNullListDiff() {
		ListDiff diff = new ListDiff() {
			public ListDiffEntry[] getDifferences() {
				return null;
			}
		};

		try {
			diff.toString();
			assertTrue(true);
		} catch (NullPointerException e) {
			fail("NPE was thrown.");
		}
	}

	public void testListDiffToStringDoesNotThrowNPEForNullListDiffEntry() {
		ListDiff diff = new ListDiff() {
			public ListDiffEntry[] getDifferences() {
				return new ListDiffEntry[1];
			}
		};

		try {
			diff.toString();
			assertTrue(true);
		} catch (NullPointerException e) {
			fail("NPE was thrown.");
		}
	}

	public void testDiffScenario1() throws Exception {
		ListDiff diff = diff(null, null);
		assertEquals(0, diff.getDifferences().length);
	}
	
	private ListDiff diff(String[] oldArray, String[] newArray) {
		List a = Arrays.asList((oldArray != null) ? oldArray : new String[] {});
		List b = Arrays.asList((newArray != null) ? newArray : new String[] {});
		
		return Diffs.computeListDiff(a, b);
	}
	
	public void testDiffScenario2() throws Exception {
		ListDiff diff = diff(new String[] {"a"}, null);
		assertEquals(1, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], false, 0, "a");
	}
	
	public void testDiffScenario3() throws Exception {
		ListDiff diff = diff(null, new String[] {"a"});
		assertEquals(1, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], true, 0, "a");
	}
	
	public void testDiffScenario4() throws Exception {
		ListDiff diff = diff(new String[] {"a"}, new String[] {"a"});
		
		assertEquals(0, diff.getDifferences().length);
	}
	
	public void testDiffScenario5() throws Exception {
		ListDiff diff = diff(new String[] {"a"}, new String[] {"b"});
		assertEquals(2, diff.getDifferences().length);
		
		assertEntry(diff.getDifferences()[0], true, 0, "b");
		assertEntry(diff.getDifferences()[1], false, 1, "a");
	}
	
	public void testDiffScenario6() throws Exception {
		ListDiff diff = diff(new String[] { "a" }, new String[] { "a", "b" });
		
		assertEquals(1, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], true, 1, "b");
	}
	
	public void testDiffScenario7() throws Exception {
		ListDiff diff = diff(new String[] { "a" }, new String[] { "b", "a" });
		
		assertEquals(1, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], true, 0, "b");
	}
	
	public void testDiffScenario8() throws Exception {
		ListDiff diff = diff(new String[] { "a" }, new String[] { "b", "b" });
		
		assertEquals(3, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], true, 0, "b");
		assertEntry(diff.getDifferences()[1], true, 1, "b");
		assertEntry(diff.getDifferences()[2], false, 2, "a");
	}
	
	public void testDiffScenario9() throws Exception {
		ListDiff diff = diff(new String[] { "a" }, new String[] { "a", "b", "c" });
		
		assertEquals(2, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], true, 1, "b");
		assertEntry(diff.getDifferences()[1], true, 2, "c");
	}
	
	public void testDiffScenario10() throws Exception {
		ListDiff diff = diff(new String[] { "b" }, new String[] { "a", "b", "c" });
		
		assertEquals(2, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], true, 0, "a");
		assertEntry(diff.getDifferences()[1], true, 2, "c");
	}
	
	public void testDiffScenario11() throws Exception {
		ListDiff diff = diff(new String[] { "c" }, new String[] { "a", "b", "c" });
		
		assertEquals(2, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], true, 0, "a");
		assertEntry(diff.getDifferences()[1], true, 1, "b");		
	}
	
	public void testDiffScenario12() throws Exception {
		ListDiff diff = diff(new String[] { "a", "b", "c" }, new String[] { "a", "b", "c" });
		
		assertEquals(0, diff.getDifferences().length);
	}
	
	public void testDiffScenario13() throws Exception {
		ListDiff diff = diff(new String[] { "a", "b", "c" }, new String[] { "b", "c" });
		
		assertEquals(1, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], false, 0, "a");
	}
	
	public void testDiffScenarios14() throws Exception {
		ListDiff diff = diff(new String[] { "a", "b", "c" }, new String[] { "a", "c" });
		
		assertEquals(1, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], false, 1, "b");
	}
	
	public void testDiffScenarios15() throws Exception {
		ListDiff diff = diff(new String[] { "a", "b", "c" }, new String[] { "a", "b" });
		
		assertEquals(1, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], false, 2, "c");
	}
	
	public void testDiffScenarios16() throws Exception {
		ListDiff diff = diff(new String[] { "a", "b", "c" }, new String[] { "c", "b", "a" });
		
		assertEquals(4, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], false, 2, "c");
		assertEntry(diff.getDifferences()[1], true, 0, "c");
		assertEntry(diff.getDifferences()[2], false, 2, "b");
		assertEntry(diff.getDifferences()[3], true, 1, "b");
	}
	
	public void testDiffScenarios17() throws Exception {
		ListDiff diff = diff(new String[] { "a", "b", "c" }, new String[] { "c", "b" });
		
		assertEquals(3, diff.getDifferences().length);
		assertEntry(diff.getDifferences()[0], false, 0, "a");
		assertEntry(diff.getDifferences()[1], false, 1, "c");
		assertEntry(diff.getDifferences()[2], true, 0, "c");
	}
	
	private static void assertEntry(ListDiffEntry entry, boolean addition, int position, String element) {
		assertEquals("addition", addition, entry.isAddition());
		assertEquals("position", position, entry.getPosition());
		assertEquals("element", element, entry.getElement());
	}

	public void testComputeListDiff_SingleInsert() {
		checkComputedListDiff(Arrays.asList(new Object[] { "a", "c" }), Arrays
				.asList(new Object[] { "a", "b", "c" }));
	}

	public void testComputeListDiff_SingleAppend() {
		checkComputedListDiff(Arrays.asList(new Object[] { "a", "b" }), Arrays
				.asList(new Object[] { "a", "b", "c" }));
	}

	public void testComputeListDiff_SingleRemove() {
		checkComputedListDiff(Arrays.asList(new Object[] { "a", "b", "c" }),
				Arrays.asList(new Object[] { "a", "b" }));
		checkComputedListDiff(Arrays.asList(new Object[] { "a", "b", "c" }),
				Arrays.asList(new Object[] { "a", "c" }));
		checkComputedListDiff(Arrays.asList(new Object[] { "a", "b", "c" }),
				Arrays.asList(new Object[] { "b", "c" }));
	}

	public void testComputeListDiff_MoveDown1() {
		checkComputedListDiff(Arrays.asList(new Object[] { "a", "b" }), Arrays
				.asList(new Object[] { "b", "a" }));
	}

	public void testComputeListDiff_MoveDown2() {
		checkComputedListDiff(Arrays.asList(new Object[] { "a", "b", "c" }),
				Arrays.asList(new Object[] { "b", "c", "a" }));
	}

	public void testComputeListDiff_MoveUp1() {
		checkComputedListDiff(Arrays.asList(new Object[] { "a", "b" }), Arrays
				.asList(new Object[] { "b", "a" }));
	}

	public void testComputeListDiff_MoveUp2() {
		checkComputedListDiff(Arrays.asList(new Object[] { "a", "b", "c" }),
				Arrays.asList(new Object[] { "c", "a", "b" }));
	}

	private static void checkComputedListDiff(List oldList, List newList) {
		ListDiff diff = Diffs.computeListDiff(oldList, newList);

		final List list = new ArrayList(oldList);
		diff.accept(new ListDiffVisitor() {
			public void handleAdd(int index, Object element) {
				list.add(index, element);
			}

			public void handleRemove(int index, Object element) {
				assertEquals(element, list.remove(index));
			}

			public void handleReplace(int index, Object oldElement,
					Object newElement) {
				assertEquals(oldElement, list.set(index, newElement));
			}
		});

		assertEquals(
				"Applying diff to old list should make it equal to new list",
				newList, list);
	}
}

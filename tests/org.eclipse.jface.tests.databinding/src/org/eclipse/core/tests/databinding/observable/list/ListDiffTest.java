/*******************************************************************************
 * Copyright (c) 2007 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 208858)
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.list;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;

import junit.framework.TestCase;

/**
 * Tests for ListDiff class
 * 
 * @since 1.1
 */
public class ListDiffTest extends TestCase {
	ListDiffVisitorStub visitor;

	protected void setUp() throws Exception {
		super.setUp();
		visitor = new ListDiffVisitorStub();
	}

	public void testAccept_Add() {
		createListDiff(add(0, "element")).accept(visitor);
		assertEquals("add(0,element)", visitor.log);
	}

	public void testAccept_Remove() {
		createListDiff(remove(0, "element")).accept(visitor);
		assertEquals("remove(0,element)", visitor.log);
	}

	public void testAccept_Move() {
		createListDiff(remove(0, "element"), add(1, "element")).accept(visitor);
		assertEquals("move(0,1,element)", visitor.log);
	}

	public void testAccept_Replace() {
		createListDiff(remove(0, "element0"), add(0, "element1")).accept(
				visitor);
		assertEquals("replace(0,element0,element1)", visitor.log);
	}

	public void testAccept_AllPatterns() {
		createListDiff(new ListDiffEntry[] {
				// Replace
				remove(0, "element0"), add(0, "element1"),
				// Remove
				remove(1, "element2"),
				// Add
				add(2, "element3"),
				// Move
				remove(3, "element4"), add(4, "element4") }).accept(visitor);
		assertEquals(
				"replace(0,element0,element1), remove(1,element2), add(2,element3), move(3,4,element4)",
				visitor.log);
	}

	public void testAccept_MoveDetectionUsesEqualityNotSameness() {
		Object element0 = new String("element");
		Object element1 = new String("element");
		assertNotSame(element0, element1);
		assertEquals(element0, element1);

		createListDiff(remove(0, element0), add(1, element1)).accept(visitor);
		assertEquals("move(0,1,element)", visitor.log);
	}

	private ListDiffEntry add(int index, Object element) {
		return Diffs.createListDiffEntry(index, true, element);
	}

	private ListDiffEntry remove(int index, Object element) {
		return Diffs.createListDiffEntry(index, false, element);
	}

	private ListDiff createListDiff(ListDiffEntry difference) {
		return createListDiff(new ListDiffEntry[] { difference });
	}

	private ListDiff createListDiff(ListDiffEntry first, ListDiffEntry second) {
		return createListDiff(new ListDiffEntry[] { first, second });
	}

	private ListDiff createListDiff(ListDiffEntry[] differences) {
		return Diffs.createListDiff(differences);
	}

	class ListDiffVisitorStub extends ListDiffVisitor {
		String log = "";

		public void handleAdd(int index, Object element) {
			log("add(" + index + "," + element + ")");
		}

		public void handleRemove(int index, Object element) {
			log("remove(" + index + "," + element + ")");
		}

		public void handleMove(int oldIndex, int newIndex, Object element) {
			log("move(" + oldIndex + "," + newIndex + "," + element + ")");
		}

		public void handleReplace(int index, Object oldElement,
				Object newElement) {
			log("replace(" + index + "," + oldElement + "," + newElement + ")");
		}

		private void log(String message) {
			if (log.length() > 0)
				log += ", ";
			log += message;
		}
	}
}

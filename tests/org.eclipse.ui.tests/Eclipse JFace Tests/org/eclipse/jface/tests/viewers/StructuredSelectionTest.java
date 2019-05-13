/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Mikael Barbero (Eclipse Foundation) - Bug 254570
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.StructuredSelection;

import junit.framework.TestCase;

public class StructuredSelectionTest extends TestCase {

	static final IElementComparer JAVA_LANG_OBJECT_COMPARER = new IElementComparer() {
		@Override
		public int hashCode(Object element) {
			return element.hashCode() + 71; // arbitrary prime number different from the
											// one used in IDENTITY_COMPARER
		}

		@Override
		public boolean equals(Object a, Object b) {
			return a.equals(b);
		}
	};

	static final IElementComparer IDENTITY_COMPARER = new IElementComparer() {
		@Override
		public int hashCode(Object element) {
			return element.hashCode() + 97; // arbitrary prime number different from the
											// one used in JAVA_LANG_OBJECT_COMPARER
		}

		@Override
		public boolean equals(Object a, Object b) {
			return a == b;
		}
	};

	public StructuredSelectionTest(String name) {
		super(name);
	}

	public static void main(String args[]) {
		junit.textui.TestRunner.run(StructuredSelectionTest.class);
	}

	public void testEquals() {
		String element = "A selection";
		StructuredSelection sel1 = new StructuredSelection(element);
		StructuredSelection sel2 = new StructuredSelection(element);

		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(sel1, sel2);
	}

	public void testEquals2() {
		String element1 = "A selection";
		String element2 = "A selection";
		String element3 = "Other";
		StructuredSelection sel1 = new StructuredSelection(element1);
		StructuredSelection sel2 = new StructuredSelection(element2);
		StructuredSelection sel3 = new StructuredSelection(element3);

		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(sel1, sel2);
		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(sel1, sel3);
	}

	public void testEquals3() { // two empty selections
		StructuredSelection empty1 = new StructuredSelection();
		StructuredSelection empty2 = new StructuredSelection();

		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(empty1, empty2);
	}

	public void testEquals4() { // empty selection with non-empty selection
		StructuredSelection sel = new StructuredSelection("A selection");
		StructuredSelection empty = new StructuredSelection();

		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(sel, empty);
	}

	public void testEquals5() { // equality is order-dependent
		List<String> l1 = new ArrayList<>();
		l1.add("element 1");
		l1.add("element 2");

		List<String> l2 = new ArrayList<>();
		l2.add("element 2");
		l2.add("element 1");

		StructuredSelection sel1 = new StructuredSelection(l1);
		StructuredSelection sel2 = new StructuredSelection(l2);

		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(sel1, sel2);
	}

	public void testEquals6() { // different selections
		List<String> l1 = new ArrayList<>();
		l1.add("element 1");
		l1.add("element 2");

		List<String> l2 = new ArrayList<>();
		l2.add("element 2");
		l2.add("element 3");
		l2.add("element 1");

		StructuredSelection sel1 = new StructuredSelection(l1);
		StructuredSelection sel2 = new StructuredSelection(l2);

		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(sel1, sel2);
	}

	/**
	 * Empty selections via different constructors.
	 * Regression test for bug 40245.
	 */
	public void testEquals7() {
		StructuredSelection empty1 = new StructuredSelection();
		StructuredSelection empty2 = new StructuredSelection(new Object[0]);

		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(empty1, empty2);
	}

	public void testEqualsWithComparer1() {
		doTestEqualsWithComparer1(JAVA_LANG_OBJECT_COMPARER);
		doTestEqualsWithComparer1(IDENTITY_COMPARER);
	}

	private void doTestEqualsWithComparer1(IElementComparer comparer) {
		StructuredSelection sel1 = new StructuredSelection(Arrays.asList("A selection"), comparer);
		StructuredSelection sel2 = new StructuredSelection(Arrays.asList("A selection"), comparer);

		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(sel1, sel2);
	}

	public void testEqualsWithComparer2() {
		doTestEqualsWithComparer2(JAVA_LANG_OBJECT_COMPARER);
		doTestEqualsWithComparer2(IDENTITY_COMPARER);
	}

	private void doTestEqualsWithComparer2(IElementComparer comparer) {
		StructuredSelection sel1 = new StructuredSelection(Arrays.asList("element 1", "element 2"), comparer);
		StructuredSelection sel2 = new StructuredSelection(Arrays.asList("element 1", "element 2"), comparer);

		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(sel1, sel2);
	}

	public void testEqualsWithComparer3() {
		doTestEqualsWithComparer3(JAVA_LANG_OBJECT_COMPARER);
		doTestEqualsWithComparer3(IDENTITY_COMPARER);
	}

	private void doTestEqualsWithComparer3(IElementComparer comparer) {
		StructuredSelection sel1 = new StructuredSelection(Arrays.asList("element 1", "element 2"), comparer);
		StructuredSelection sel2 = new StructuredSelection(Arrays.asList("element 2", "element 1"), comparer);

		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(sel1, sel2);
	}

	public void testEqualsWithComparer4() {
		doTestEqualsWithComparer4(JAVA_LANG_OBJECT_COMPARER);
		doTestEqualsWithComparer4(IDENTITY_COMPARER);
	}

	private void doTestEqualsWithComparer4(IElementComparer comparer) {
		StructuredSelection sel1 = new StructuredSelection(new ArrayList<>(), comparer);
		StructuredSelection sel2 = new StructuredSelection(Arrays.asList("element 1"), comparer);

		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(sel1, sel2);
	}

	public void testEqualsWithComparer5() {
		doTestEqualsWithComparer5(JAVA_LANG_OBJECT_COMPARER);
		doTestEqualsWithComparer5(IDENTITY_COMPARER);
	}

	private void doTestEqualsWithComparer5(IElementComparer comparer) {
		StructuredSelection sel1 = new StructuredSelection(new ArrayList<>(), comparer);
		StructuredSelection sel2 = new StructuredSelection(new ArrayList<>(), comparer);

		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(sel1, sel2);
	}

	static class EqualsHashCodeContractTestHelper {
		private static final String HASHCODE_CONSISTENCY_MSG = "Whenever it is invoked on the same object more than once during an execution of a Java application, the hashCode method must consistently return the same integer, provided no information used in equals comparisons on the object is modified.";
		private static final String EQUALS_IMPLIES_SAME_HASHCODE_MSG = "If two objects are equal according to the equals(Object) method, then calling the hashCode method on each of the two objects must produce the same integer result.";
		private static final String REFLEXIVITY_MSG = "For any non-null reference value x, x.equals(x) should return true.";
		private static final String SYMMETRICITY_MSG = "For any non-null reference values x and y, x.equals(y) should return true if and only if y.equals(x) returns true.";
		private static final String EQUALS_CONSITENCY_MSG = "For any non-null reference values x and y, multiple invocations of x.equals(y) consistently return true or consistently return false, provided no information used in equals comparisons on the objects is modified.";
		private static final String NOT_EQUALS_NULL_MSG = "For any non-null reference value x, x.equals(null) should return false.";
		private static final int CONSISTENCY_THRESHOLD = 10;

		public static void testExpectedEqualsObjects(Object o1, Object o2) {
			// reflexive
			assertTrue(REFLEXIVITY_MSG, o1.equals(o1));
			assertTrue(REFLEXIVITY_MSG, o2.equals(o2));
			// symmetric
			assertTrue(SYMMETRICITY_MSG, o1.equals(o2));
			assertTrue(SYMMETRICITY_MSG, o2.equals(o1));
			// consistent
			for (int i = 0; i < CONSISTENCY_THRESHOLD; i++) {
				assertTrue(EQUALS_CONSITENCY_MSG, o1.equals(o2));
				assertTrue(EQUALS_CONSITENCY_MSG, o2.equals(o1));
			}
			assertFalse(NOT_EQUALS_NULL_MSG, o1.equals(null));
			assertFalse(NOT_EQUALS_NULL_MSG, o2.equals(null));

			// a.equals(b) => a.hashCode() == b.hashCode()
			assertTrue(EQUALS_IMPLIES_SAME_HASHCODE_MSG, o1.hashCode() == o2.hashCode());
			for (int i = 0; i < CONSISTENCY_THRESHOLD; i++) {
				assertTrue(HASHCODE_CONSISTENCY_MSG, o1.hashCode() == o1.hashCode());
				assertTrue(HASHCODE_CONSISTENCY_MSG, o2.hashCode() == o2.hashCode());
			}
		}

		public static void testExpectedNotEqualsObjects(Object o1, Object o2) {
			// symmetric
			assertFalse(o1.equals(o2));
			assertFalse(o2.equals(o1));

			// Not required: assertFalse(o1.hashCode() == o2.hashCode())
			// It is not required that if two objects are unequal according to
			// the
			// java.lang.Object.equals(java.lang.Object) method, then calling
			// the hashCode
			// method on each of the two objects must produce distinct integer
			// results.
			// However, the programmer should be aware that producing distinct
			// integer
			// results for unequal objects may improve the performance of hash
			// tables.
		}
	}
}

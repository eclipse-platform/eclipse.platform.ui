/*******************************************************************************
 * Copyright (c) 2014, 2015 Manumitting Technologies Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brian de Alwis (MTI) - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.e4.ui.internal.workbench.TopologicalSort;
import org.junit.Test;

/**
 * A set of tests for the {@link TopologicalSort} class
 */
public class TopoSortTests {
	/** Should this sorter configure as requirements, dependencies, or both */
	enum Type {
		REQUIREMENTS, DEPENDENCIES, BOTH
	}

	/**
	 * A simple topological sorter that uses the test data from
	 * http://www.cs.sunysb.edu/~algorith/files/topological-sorting.shtml we
	 * interpret arrows as A &rarr; B means B is a requirement of A, or A is a
	 * dependency of B. The input data is 1 through 10.
	 */
	static class TestSorter extends TopologicalSort<Integer, Integer> {
		Type type = Type.REQUIREMENTS;

		/** The test data */
		public Integer[] getTestData() {
			return new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		}

		@Override
		protected Collection<Integer> getRequirements(Integer value) {
			if (type == Type.DEPENDENCIES) {
				return Collections.emptyList();
			}

			switch (value) {
			case 1:
				return Collections.emptyList();
			case 2:
			case 3:
			case 5:
			case 7:
				return Collections.singleton(1);
			case 4:
				return Collections.singleton(2);
			case 6:
				return Arrays.asList(2, 3);
			case 8:
				return Collections.singleton(4);
			case 9:
				return Collections.singleton(3);
			case 10:
				return Arrays.asList(2, 5);
			default:
				throw new IllegalArgumentException(value.toString());
			}
		}

		@Override
		protected Collection<Integer> getDependencies(Integer value) {
			if (type == Type.REQUIREMENTS) {
				return Collections.emptyList();
			}
			// this uses the example from
			// <http://www.cs.sunysb.edu/~algorith/files/topological-sorting.shtml>
			// we interpret arrows as A -> B means A is the successor of B, or A
			// depends on B
			switch (value) {
			case 1:
				return Arrays.asList(2, 3, 5, 7);
			case 2:
				return Arrays.asList(4, 6, 10);
			case 3:
				return Arrays.asList(6, 9);
			case 4:
				return Arrays.asList(8);
			case 5:
				return Arrays.asList(10);
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
				return Collections.emptyList();
			default:
				throw new IllegalArgumentException(value.toString());
			}
		}

		@Override
		protected Integer getId(Integer o) {
			return o;
		}
	}

	@Test
	public void testTopoSorter() {
		TestSorter ts = new TestSorter();
		for (Type type : Type.values()) {
			ts.type = type;

			List<Integer> results = Arrays.asList(ts.sort(ts.getTestData()));
			assertTrue(results.indexOf(1) < results.indexOf(2));
			assertTrue(results.indexOf(1) < results.indexOf(3));
			assertTrue(results.indexOf(1) < results.indexOf(5));
			assertTrue(results.indexOf(1) < results.indexOf(7));
			assertTrue(results.indexOf(2) < results.indexOf(4));
			assertTrue(results.indexOf(2) < results.indexOf(6));
			assertTrue(results.indexOf(2) < results.indexOf(10));
			assertTrue(results.indexOf(3) < results.indexOf(6));
			assertTrue(results.indexOf(3) < results.indexOf(9));
			assertTrue(results.indexOf(4) < results.indexOf(8));
			assertTrue(results.indexOf(5) < results.indexOf(10));
		}
	}

	/**
	 * Test cycles: A &rarr; B, B &rarr; C, C &rarr; A, D &rarr; A. All nodes
	 * have out-degree 1, but A has in-degree 2. A, B, C must be output before
	 * D.
	 */
	static class CycleTestSorter extends TopologicalSort<String, String> {
		Type type = Type.REQUIREMENTS;

		/** The test data */
		public String[] getTestData() {
			return new String[] { "A", "B", "C", "D" };
		}

		@Override
		protected String getId(String o) {
			return o;
		}

		@Override
		protected Collection<String> getRequirements(String id) {
			if (type == Type.DEPENDENCIES) {
				return null;
			}

			if (id.equals("A")) {
				return Collections.singleton("B");
			} else if (id.equals("B")) {
				return Collections.singleton("C");
			} else if (id.equals("C")) {
				return Collections.singleton("A");
			} else if (id.equals("D")) {
				return Collections.singleton("A");
			}
			throw new IllegalArgumentException(id);
		}

		@Override
		protected Collection<String> getDependencies(String id) {
			if (type == Type.REQUIREMENTS) {
				return null;
			}

			if (id.equals("A")) {
				return Arrays.asList("C", "D");
			} else if (id.equals("B")) {
				return Collections.singleton("A");
			} else if (id.equals("C")) {
				return Collections.singleton("B");
			} else if (id.equals("D")) {
				return null;
			}
			throw new IllegalArgumentException(id);
		}
	}

	@Test
	public void testCycles() {
		CycleTestSorter ts = new CycleTestSorter();
		for (Type type : Type.values()) {
			ts.type = type;

			List<String> results = Arrays.asList(ts.sort(ts.getTestData()));
			assertTrue(results.indexOf("A") < results.indexOf("D"));
			assertTrue(results.indexOf("B") < results.indexOf("D"));
			assertTrue(results.indexOf("C") < results.indexOf("D"));
			assertTrue(results.indexOf("A") < results.indexOf("D"));
		}
	}

}

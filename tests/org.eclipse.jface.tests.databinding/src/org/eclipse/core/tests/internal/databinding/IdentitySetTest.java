/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 215531)
 *         (through ViewerElementSetTest.java)
 *     Matthew Hall - bug 262269
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.core.internal.databinding.identity.IdentitySet;
import org.eclipse.jface.internal.databinding.viewers.ViewerElementSet;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.2
 */
public class IdentitySetTest {
	IdentitySet<Object> set;

	@Before
	public void setUp() throws Exception {
		set = new IdentitySet<>();
	}

	@Test
	public void testConstructor_NullComparer() {
		try {
			new ViewerElementSet(null);
			fail("Constructor should throw exception when null comparer passed in");
		} catch (RuntimeException expected) {
		}
	}

	@Test
	public void testConstructorWithCollection_NullCollection() {
		try {
			new ViewerElementSet(null);
			fail("Constructor should throw exception when null collection passed in");
		} catch (RuntimeException expected) {
		}
	}

	@Test
	public void testConstructorWithCollection_AddsAllElements() {
		Collection<Object> toCopy = Collections.singleton(new Object());
		set = new IdentitySet<>(toCopy);
		assertTrue(set.containsAll(toCopy));
	}

	@Test
	public void testAdd_ContainsHonorsComparer() {
		Object o1 = new String("string");
		Object o2 = new String("string"); // distinct instances
		assertTrue(o1.equals(o2));
		assertNotSame(o1, o2);

		assertTrue(set.add(o1));

		assertTrue(set.contains(o1));
		assertFalse(set.contains(o2));
	}

	@Test
	public void testAdd_FilterDuplicateElements() {
		Object o = new Object();

		assertTrue(set.add(o));
		assertFalse(set.add(o)); // no change--element already in set

		assertEquals(1, set.size());
		assertTrue(set.contains(o));
	}

	@Test
	public void testAddAll_ContainsAllHonorsComparer() {
		String o1 = new String("o1");
		String o2 = new String("o2");
		Collection<Object> items = Arrays.asList(new Object[] { o1, o2 });
		assertTrue(set.addAll(items));

		assertTrue(set.containsAll(items));
		assertFalse(set.containsAll(Collections.singleton(new String("o1"))));
		assertFalse(set.containsAll(Collections.singleton(new String("o2"))));
	}

	@Test
	public void testAddAll_FiltersDuplicateElements() {
		Object o = new Object();
		set.add(o);

		assertFalse(set.addAll(Collections.singleton(o)));
	}

	@Test
	public void testClear() {
		set.add(new Object());
		assertEquals(1, set.size());

		set.clear();
		assertEquals(0, set.size());
	}

	@Test
	public void testIsEmpty() {
		assertTrue(set.isEmpty());
		set.add(new Object());
		assertFalse(set.isEmpty());
	}

	@Test
	public void testIterator() {
		Object o = new Object();
		set.add(o);

		Iterator<Object> iterator = set.iterator();
		assertTrue(iterator.hasNext());
		assertSame(o, iterator.next());

		assertTrue(set.contains(o));
		iterator.remove();
		assertFalse(set.contains(o));

		assertFalse(iterator.hasNext());
	}

	@Test
	public void testRemove() {
		Object o = new Object();
		assertFalse(set.remove(o));

		assertTrue(set.add(o));
		assertTrue(set.contains(o));
		assertTrue(set.remove(o));

		assertFalse(set.contains(o));
	}

	@Test
	public void testRemoveAll() {
		assertFalse(set.removeAll(Collections.EMPTY_SET));

		Object o1 = new Object();
		Object o2 = new Object();
		set.addAll(Arrays.asList(new Object[] { o1, o2 }));

		assertTrue(set.removeAll(Collections.singleton(o1)));
		assertFalse(set.contains(o1));
		assertFalse(set.removeAll(Collections.singleton(o1)));

		assertTrue(set.removeAll(Arrays.asList(new Object[] { o2, "some", "other", "objects" })));
		assertFalse(set.contains(o2));
	}

	@Test
	public void testRetainAll() {
		Object o1 = new Object();
		Object o2 = new Object();
		set.add(o1);
		set.add(o2);

		assertFalse(set.retainAll(Arrays.asList(new Object[] { o1, o2 }))); // no
		// change

		assertTrue(set.contains(o2));
		assertTrue(set.retainAll(Collections.singleton(o1)));
		assertFalse(set.contains(o2));

		assertTrue(set.contains(o1));
		assertTrue(set.retainAll(Collections.EMPTY_SET));
		assertFalse(set.contains(o1));
	}

	@Test
	public void testSize() {
		assertEquals(0, set.size());

		Object o = new Object();
		set.add(o);
		assertEquals(1, set.size());

		set.remove(o);
		assertEquals(0, set.size());
	}

	@Test
	public void testToArray() {
		assertEquals(0, set.toArray().length);

		Object o = new Object();
		set.add(o);
		assertTrue(Arrays.equals(new Object[] { o }, set.toArray()));
	}

	@Test
	public void testToArrayWithObjectArray() {
		Object o = new String("unique");
		set.add(o);

		String[] array = set.toArray(new String[0]);
		assertEquals(1, array.length);
		assertSame(o, array[0]);
	}

	@Test
	public void testEquals() {
		assertTrue(set.equals(set));
		assertFalse(set.equals(null));
		assertFalse(set.equals(new Object()));

		assertTrue(set.equals(Collections.EMPTY_SET));

		Object o = new String("string");
		Object distinct = new String("string");
		set.add(o);
		assertTrue(set.equals(Collections.singleton(o)));
		assertFalse(set.equals(Collections.singleton(distinct)));
	}

	@Test
	public void testHashCode() {
		// Hash code implementation is mandated
		assertEquals(0, set.hashCode());

		Object o = new Object();
		set.add(o);
		int hash = o.hashCode();
		assertEquals(hash, set.hashCode());

		Object o2 = new Object();
		set.add(o2);
		hash += o2.hashCode();
		assertEquals(hash, set.hashCode());
	}
}

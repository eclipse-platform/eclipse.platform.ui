/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 215531)
 *         (through ViewerElementSetTest.java)
 *     Matthew Hall - bug 262269
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.internal.databinding.identity.IdentitySet;
import org.eclipse.jface.internal.databinding.viewers.ViewerElementSet;

/**
 * @since 1.2
 */
public class IdentitySetTest extends TestCase {
	IdentitySet set;

	protected void setUp() throws Exception {
		super.setUp();
		set = new IdentitySet();
	}

	public void testConstructor_NullComparer() {
		try {
			new ViewerElementSet(null);
			fail("Constructor should throw exception when null comparer passed in");
		} catch (RuntimeException expected) {
		}
	}

	public void testConstructorWithCollection_NullCollection() {
		try {
			new ViewerElementSet(null);
			fail("Constructor should throw exception when null collection passed in");
		} catch (RuntimeException expected) {
		}
	}

	public void testConstructorWithCollection_AddsAllElements() {
		Collection toCopy = Collections.singleton(new Object());
		set = new IdentitySet(toCopy);
		assertTrue(set.containsAll(toCopy));
	}

	public void testAdd_ContainsHonorsComparer() {
		Object o1 = new String("string");
		Object o2 = new String("string"); // distinct instances
		assertTrue(o1.equals(o2));
		assertNotSame(o1, o2);

		assertTrue(set.add(o1));

		assertTrue(set.contains(o1));
		assertFalse(set.contains(o2));
	}

	public void testAdd_FilterDuplicateElements() {
		Object o = new Object();

		assertTrue(set.add(o));
		assertFalse(set.add(o)); // no change--element already in set

		assertEquals(1, set.size());
		assertTrue(set.contains(o));
	}

	public void testAddAll_ContainsAllHonorsComparer() {
		String o1 = new String("o1");
		String o2 = new String("o2");
		Collection items = Arrays.asList(new Object[] { o1, o2 });
		assertTrue(set.addAll(items));

		assertTrue(set.containsAll(items));
		assertFalse(set.containsAll(Collections.singleton(new String("o1"))));
		assertFalse(set.containsAll(Collections.singleton(new String("o2"))));
	}

	public void testAddAll_FiltersDuplicateElements() {
		Object o = new Object();
		set.add(o);

		assertFalse(set.addAll(Collections.singleton(o)));
	}

	public void testClear() {
		set.add(new Object());
		assertEquals(1, set.size());

		set.clear();
		assertEquals(0, set.size());
	}

	public void testIsEmpty() {
		assertTrue(set.isEmpty());
		set.add(new Object());
		assertFalse(set.isEmpty());
	}

	public void testIterator() {
		Object o = new Object();
		set.add(o);

		Iterator iterator = set.iterator();
		assertTrue(iterator.hasNext());
		assertSame(o, iterator.next());

		assertTrue(set.contains(o));
		iterator.remove();
		assertFalse(set.contains(o));

		assertFalse(iterator.hasNext());
	}

	public void testRemove() {
		Object o = new Object();
		assertFalse(set.remove(o));

		assertTrue(set.add(o));
		assertTrue(set.contains(o));
		assertTrue(set.remove(o));

		assertFalse(set.contains(o));
	}

	public void testRemoveAll() {
		assertFalse(set.removeAll(Collections.EMPTY_SET));

		Object o1 = new Object();
		Object o2 = new Object();
		set.addAll(Arrays.asList(new Object[] { o1, o2 }));

		assertTrue(set.removeAll(Collections.singleton(o1)));
		assertFalse(set.contains(o1));
		assertFalse(set.removeAll(Collections.singleton(o1)));

		assertTrue(set.removeAll(Arrays.asList(new Object[] { o2, "some",
				"other", "objects" })));
		assertFalse(set.contains(o2));
	}

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

	public void testSize() {
		assertEquals(0, set.size());

		Object o = new Object();
		set.add(o);
		assertEquals(1, set.size());

		set.remove(o);
		assertEquals(0, set.size());
	}

	public void testToArray() {
		assertEquals(0, set.toArray().length);

		Object o = new Object();
		set.add(o);
		assertTrue(Arrays.equals(new Object[] { o }, set.toArray()));
	}

	public void testToArrayWithObjectArray() {
		Object o = new String("unique");
		set.add(o);

		String[] array = (String[]) set.toArray(new String[0]);
		assertEquals(1, array.length);
		assertSame(o, array[0]);
	}

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

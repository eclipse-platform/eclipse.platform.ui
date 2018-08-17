/*******************************************************************************
 * Copyright (c) 2008, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 233306)
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.map.BidiObservableMap;
import org.eclipse.core.databinding.observable.map.BidirectionalMap;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class BidiObservableMapTest extends AbstractDefaultRealmTestCase {
	private IObservableMap wrappedMap;
	private BidiObservableMap bidiMap;
	private Object key1;
	private Object key2;
	private Object value1;
	private Object value2;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		wrappedMap = new WritableMap();
		bidiMap = new BidiObservableMap(wrappedMap);
		key1 = new Object();
		key2 = new Object();
		value1 = new Object();
		value2 = new Object();
	}

	@Test
	public void testConstructor_NullArgument() {
		try {
			new BidirectionalMap(null);
			fail("Expected NullPointerException");
		} catch (NullPointerException expected) {
		}
	}

	public void withAndWithoutListeners(Runnable runnable) throws Exception {
		// assuming setUp() beforehand

		// without listeners
		runnable.run();

		tearDown();
		setUp();

		// with listeners
		ChangeEventTracker.observe(wrappedMap);
		runnable.run();

		// assuming tearDown() afterward
	}

	@Test
	public void testGetKeys_Empty() throws Exception {
		withAndWithoutListeners(() -> assertEquals(Collections.EMPTY_SET, bidiMap.getKeys(value1)));
	}

	@Test
	public void testGetKeys_NullKey() throws Exception {
		withAndWithoutListeners(() -> {
			wrappedMap.put(null, value1);
			assertEquals(Collections.singleton(null), bidiMap.getKeys(value1));
		});
	}

	@Test
	public void testGetKeys_NullValue() throws Exception {
		withAndWithoutListeners(() -> {
			wrappedMap.put(key1, null);
			assertEquals(Collections.singleton(key1), bidiMap.getKeys(null));
		});
	}

	@Test
	public void testGetKeys_SinglePut() throws Exception {
		withAndWithoutListeners(() -> {
			wrappedMap.put(key1, value1);
			assertEquals(Collections.singleton(key1), bidiMap.getKeys(value1));
		});
	}

	@Test
	public void testGetKeys_ReplaceValue() throws Exception {
		withAndWithoutListeners(() -> {
			wrappedMap.put(key1, value1);
			assertEquals(Collections.singleton(key1), bidiMap.getKeys(value1));
			assertEquals(Collections.EMPTY_SET, bidiMap.getKeys(value2));
			wrappedMap.put(key1, value2);
			assertEquals(Collections.EMPTY_SET, bidiMap.getKeys(value1));
			assertEquals(Collections.singleton(key1), bidiMap.getKeys(value2));
		});
	}

	@Test
	public void testGetKeys_MultipleKeysWithSameValue() throws Exception {
		withAndWithoutListeners(() -> {
			wrappedMap.put(key1, value1);
			wrappedMap.put(key2, value1);

			Set expected = new HashSet();
			expected.add(key1);
			expected.add(key2);
			assertEquals(expected, bidiMap.getKeys(value1));
		});
	}

	@Test
	public void testContainsValue_PutAndRemove() throws Exception {
		withAndWithoutListeners(() -> {
			assertFalse(bidiMap.containsValue(value1));
			wrappedMap.put(key1, value1);
			assertTrue(bidiMap.containsValue(value1));
			wrappedMap.put(key2, value1);
			assertTrue(bidiMap.containsValue(value1));
			wrappedMap.remove(key1);
			assertTrue(bidiMap.containsValue(value1));
			wrappedMap.remove(key2);
			assertFalse(bidiMap.containsValue(value1));
		});
	}
}

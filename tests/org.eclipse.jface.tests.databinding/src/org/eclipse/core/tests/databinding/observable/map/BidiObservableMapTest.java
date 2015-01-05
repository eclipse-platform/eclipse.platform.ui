/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 233306)
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.map;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.map.BidiObservableMap;
import org.eclipse.core.databinding.observable.map.BidirectionalMap;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

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
	protected void setUp() throws Exception {
		super.setUp();
		wrappedMap = new WritableMap();
		bidiMap = new BidiObservableMap(wrappedMap);
		key1 = new Object();
		key2 = new Object();
		value1 = new Object();
		value2 = new Object();
	}

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

	public void testGetKeys_Empty() throws Exception {
		withAndWithoutListeners(new Runnable() {
			@Override
			public void run() {
				assertEquals(Collections.EMPTY_SET, bidiMap.getKeys(value1));
			}
		});
	}

	public void testGetKeys_NullKey() throws Exception {
		withAndWithoutListeners(new Runnable() {
			@Override
			public void run() {
				wrappedMap.put(null, value1);
				assertEquals(Collections.singleton(null), bidiMap
						.getKeys(value1));
			}
		});
	}

	public void testGetKeys_NullValue() throws Exception {
		withAndWithoutListeners(new Runnable() {
			@Override
			public void run() {
				wrappedMap.put(key1, null);
				assertEquals(Collections.singleton(key1), bidiMap.getKeys(null));
			}
		});
	}

	public void testGetKeys_SinglePut() throws Exception {
		withAndWithoutListeners(new Runnable() {
			@Override
			public void run() {
				wrappedMap.put(key1, value1);
				assertEquals(Collections.singleton(key1), bidiMap
						.getKeys(value1));
			}
		});
	}

	public void testGetKeys_ReplaceValue() throws Exception {
		withAndWithoutListeners(new Runnable() {
			@Override
			public void run() {
				wrappedMap.put(key1, value1);
				assertEquals(Collections.singleton(key1), bidiMap
						.getKeys(value1));
				assertEquals(Collections.EMPTY_SET, bidiMap.getKeys(value2));
				wrappedMap.put(key1, value2);
				assertEquals(Collections.EMPTY_SET, bidiMap.getKeys(value1));
				assertEquals(Collections.singleton(key1), bidiMap
						.getKeys(value2));
			}
		});
	}

	public void testGetKeys_MultipleKeysWithSameValue() throws Exception {
		withAndWithoutListeners(new Runnable() {
			@Override
			public void run() {
				wrappedMap.put(key1, value1);
				wrappedMap.put(key2, value1);

				Set expected = new HashSet();
				expected.add(key1);
				expected.add(key2);
				assertEquals(expected, bidiMap.getKeys(value1));
			}
		});
	}

	public void testContainsValue_PutAndRemove() throws Exception {
		withAndWithoutListeners(new Runnable() {
			@Override
			public void run() {
				assertFalse(bidiMap.containsValue(value1));
				wrappedMap.put(key1, value1);
				assertTrue(bidiMap.containsValue(value1));
				wrappedMap.put(key2, value1);
				assertTrue(bidiMap.containsValue(value1));
				wrappedMap.remove(key1);
				assertTrue(bidiMap.containsValue(value1));
				wrappedMap.remove(key2);
				assertFalse(bidiMap.containsValue(value1));
			}
		});
	}
}
